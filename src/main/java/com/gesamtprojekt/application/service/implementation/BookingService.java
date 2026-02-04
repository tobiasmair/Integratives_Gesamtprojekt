package com.gesamtprojekt.application.service.implementation;

import com.gesamtprojekt.application.model.Booking;
import com.gesamtprojekt.application.model.Client;
import com.gesamtprojekt.application.model.Exit;
import com.gesamtprojekt.application.model.MeetingRoom;
import com.gesamtprojekt.application.model.Notification;
import com.gesamtprojekt.application.repositories.BookingRepository;
import com.gesamtprojekt.application.repositories.MeetingRoomRepository;
import com.gesamtprojekt.application.repositories.ExitDistanceRepository;
import com.gesamtprojekt.application.service.BookingServiceInterface;
import com.gesamtprojekt.application.service.dto.NotificationType;
import com.gesamtprojekt.application.repositories.ExitRepository;
import com.gesamtprojekt.application.exceptions.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.LocalTime;

import java.util.Collections;
import java.awt.print.Book;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookingService implements BookingServiceInterface {

    private final BookingRepository bookingRepository;
    private final DefaultNavigationService navigationService;
    private final ExitService exitService;
    private final NotificationService notificationService;
    private final MeetingRoomRepository meetingRoomRepository;
    private final ExitRepository exitRepository;
    private final ExitDistanceRepository exitDistanceRepository;

    @Transactional
    public void createBooking(Booking booking, Optional<Long> startExitId) {

        validateOpeningTimes(booking);
        validateBookingTime(booking);

        // Innerhalb 1 Stunde
        if (isBookingWithinOneHour(booking)) {
            if (startExitId.isEmpty()) {
                List<Exit> availableExits = exitRepository.findAllByIsActiveTrue();
                throw new MissingStartExitException(availableExits);
            }

            // Exit aus DB laden
            Exit startExit = exitRepository.findById(startExitId.get())
                    .orElseThrow(() -> new BookingValidationException("Selected start exit not found."));

            // Validierung der Reisezeit
            validateTravelTime(booking, startExit);

            // Startpunkt und berechnete Zeit in der Buchung speichern
            int time = navigationService.calculateTravelTime(startExit, booking.getMeetingRoom());
            booking.setStartExit(startExit);
            booking.setCalculatedTravelTime(time);
        }

        booking.setBookingCode(generateRandomBookingCode());
        bookingRepository.save(booking);
        createNotifications(booking);
    }

    // Öffnungszeiten überprüfen
    private void validateOpeningTimes(Booking booking) {
        LocalTime start = booking.getStartTime().toLocalTime();
        LocalTime end = booking.getEndTime().toLocalTime();

        LocalTime opensAt = LocalTime.of(7, 0);
        LocalTime closesAt = LocalTime.of(22, 0);

        if (start.isBefore(opensAt) || end.isAfter(closesAt)) {
            throw new RuntimeException("The building is closed. Bookings are only allowed between 07:00 and 22:00.");
        }
    }

    private void validateBookingTime(Booking booking) {
        boolean conflict = bookingRepository.existsOverlappingBooking(
                booking.getMeetingRoom().getRoomId(),
                booking.getStartTime(), booking.getEndTime()
        );

        if (conflict) {
            throw new BookingValidationException("Booking conflict detected for the selected room and time.");
        }
    }

    private boolean isBookingWithinOneHour(Booking booking) {
        LocalDateTime now = LocalDateTime.now();
        // Dauer zwishen jetzt und Buchungsstart
        java.time.Duration duration = java.time.Duration.between(now, booking.getStartTime());

        return !duration.isNegative() && duration.toMinutes() <= 60;
    }

    private void validateTravelTime(Booking booking, Exit startExit) {
        MeetingRoom meetingRoom = booking.getMeetingRoom();

        int totalTravelTimeSeconds = navigationService.calculateTravelTime(startExit, meetingRoom);

        if (totalTravelTimeSeconds == Integer.MAX_VALUE) {
            throw new BookingValidationException("No travel path defined between your start point and the room.");
        }

        LocalDateTime now = LocalDateTime.now();
        // Zeit bis Meeting start berechnen
        long secondsUntilStart = java.time.Duration.between(now, booking.getStartTime()).getSeconds();

        if (secondsUntilStart < totalTravelTimeSeconds) {
            int minutesNeeded = (int) Math.ceil(totalTravelTimeSeconds / 60.0);
            throw new BookingValidationException("Booking not possible. You need at least " +
                    minutesNeeded + " minutes to reach the room, but the meeting starts sooner.");
        }
    }

    private void createNotifications(Booking booking) {
        // Confirmation notification
        notificationService.createNotification(
                booking,
                NotificationType.CONFIRMATION
        );

        // Notification für kurzfristige Buchunen
        LocalDateTime now = LocalDateTime.now();
        if (booking.getStartTime().isBefore(now.plusMinutes(15)) &&
                booking.getStartTime().isAfter(now)) {

            notificationService.createNotification(
                    booking,
                    NotificationType.REMINDER_START
            );
        }
    }


    // Buchungs Code generieren
    private String generateRandomBookingCode() {
        java.util.Random random = new java.util.Random();
        // zwei 4-stellige Zahlen
        int part1 = 1000 + random.nextInt(9000);
        int part2 = 1000 + random.nextInt(9000);
        return part1 + "-" + part2;
    }

    // Buchungen eines Clients finden
    public List<Booking> findBookingByClientId(Long clientId) {
        return bookingRepository.findBookingByClientId(clientId);
    }

    // Buchungen eines Raumes in einem Zeitraum finden
    public List<Booking> findBookingsByRoomAndTimeRange(Long roomId, LocalDateTime start, LocalDateTime end) {
        return bookingRepository.findByRoomAndTimeRange(roomId, start, end);
    }

    // Booking löschen (isActive Flag setzen)
    public void deleteBooking(Booking booking) {
        booking.setIsActive(false);
        bookingRepository.save(booking);
    }

    // Booking updaten
    @Transactional
    public void updateBooking(Booking booking) {
        try {
            // check for conflicting bookings
            boolean conflict = bookingRepository.existsOverlappingBookingExcludingId(
                    booking.getMeetingRoom().getRoomId(),
                    booking.getStartTime(), booking.getEndTime(),
                    booking.getBookingId()
            );
            if (conflict) {
                throw new BookingValidationException("Booking conflict detected for the selected room and time");
            }

            // save in database
            bookingRepository.save(booking);

            // update cache after successful update to avoid having old data in the cache
            refreshCacheForRoom(booking.getMeetingRoom().getRoomId());

        } catch (Exception e) {
            throw e;
        }
    }

    // helper method to avoid redundancy in cached data
    private void refreshCacheForRoom(Long roomId) {
        try {
            List<Booking> fresh = bookingRepository.findByMeetingRoom_RoomIdAndIsActiveTrueOrderByStartTimeAsc(roomId);
            roomCache.put(roomId, fresh);
        } catch (Exception ignored) {
            // if database dies during refresh, keep old data
        }
    }

    // Anzahl der aktiven Buchungen zählen
    public long countActiveBookings() {
        return bookingRepository.countByIsActiveTrueAndBookingStatusAndEndTimeAfter("CONFIRMED", java.time.LocalDateTime.now());
    }

    // Zählt aktive Buchungen für einen bestimmten Client
    public long countByClient_UserIdAndIsActiveTrueAndBookingStatusAndEndTimeAfter(Long clientId) {
        return bookingRepository.countByClient_UserIdAndIsActiveTrueAndBookingStatusAndEndTimeAfter(clientId, "CONFIRMED", java.time.LocalDateTime.now());
    }

    // In-Memory Cache für Bookings
    private final Map<Long, List<Booking>> roomCache = new ConcurrentHashMap<>();

    // Diese Methode wird von der RoomServiceView aufgerufen
    public List<Booking> findAllActiveBookingsForRoom(Long roomId) {
        List<Booking> bookings = bookingRepository.findByMeetingRoom_RoomIdAndIsActiveTrueOrderByStartTimeAsc(roomId);
        roomCache.put(roomId, bookings);
        return bookings;
    }

    public List<Booking> getCachedBookings(Long roomId) {
        return roomCache.getOrDefault(roomId, Collections.emptyList());
    }

    public List<Booking> getBookingsForDoorDisplayByRoomName(String roomName) {
        LocalDateTime from = LocalDateTime.now();
        LocalDateTime to = from.plusHours(24);
        return bookingRepository.findActiveBookingsForRoomNameBetween(roomName, from, to);
    }
}
