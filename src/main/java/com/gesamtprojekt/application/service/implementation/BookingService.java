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
import com.gesamtprojekt.application.repositories.ExitRepository;
import com.gesamtprojekt.application.service.dto.NotificationType;
import com.gesamtprojekt.application.exceptions.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.awt.print.Book;
import java.time.LocalDateTime;
import java.util.List;
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
        validateBookingTime(booking);

        if (isBookingWithinOneHour(booking)) {
            if (startExitId.isEmpty()) {
                List<Exit> availableExits = exitRepository.findAllByIsActiveTrue(); // Controller erstellt Label building + exit name
                throw new MissingStartExitException(availableExits);
            }

            validateTravelTime(booking, startExitId.get());
        }

        booking.setBookingCode(generateRandomBookingCode());
        bookingRepository.save(booking);

        createNotifications(booking);
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
        long currentTimeInSeconds = System.currentTimeMillis() / 1000;
        long bookingTimeInSeconds = booking.getStartTime().toEpochSecond(java.time.ZoneOffset.UTC);
        return (bookingTimeInSeconds - currentTimeInSeconds) <= 3600;
    }

    private void validateTravelTime(Booking booking, Long startExitId) {
        Exit startExit = exitRepository.findByIdAndIsActiveTrue(startExitId)
                .orElseThrow(() -> new BookingValidationException("Start exit not found."));
        MeetingRoom meetingRoom = meetingRoomRepository.findById(booking.getMeetingRoom().getRoomId())
                .orElseThrow(() -> new BookingValidationException("Meeting room not found."));

        Integer timeToNearestExit = meetingRoom.getTimeToNearestExit();
        Integer timeBetweenExits = exitDistanceRepository.findTimeBetweenExits(startExit.getId(), meetingRoom.getNearestExit().getId());

        if (timeToNearestExit == null || timeBetweenExits == null) {
            throw new BookingValidationException("Travel time data is missing for the selected room or exits.");
        }

        int totalTravelTime = timeToNearestExit + timeBetweenExits;
        long currentTimeInSeconds = System.currentTimeMillis() / 1000;
        long bookingTimeInSeconds = booking.getStartTime().toEpochSecond(java.time.ZoneOffset.UTC);

        if ((bookingTimeInSeconds - currentTimeInSeconds) < totalTravelTime) {
            throw new BookingValidationException("Booking not possible. Travel time exceeds booking time.");
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
    public void updateBooking(Booking booking) {
        boolean conflict = bookingRepository.existsOverlappingBookingExcludingId(
                booking.getMeetingRoom().getRoomId(),
                booking.getStartTime(), booking.getEndTime(),
                booking.getBookingId()
        );
        if (conflict) {
            throw new BookingValidationException("Booking conflict detected for the selected room and time.");
        }

        bookingRepository.save(booking);
    }

    // Anzahl der aktiven Buchungen zählen
    public long countActiveBookings() {
        return bookingRepository.countByIsActiveTrueAndBookingStatusAndEndTimeAfter("CONFIRMED", java.time.LocalDateTime.now());
    }

    // Zählt aktive Buchungen für einen bestimmten Client
    public long countByClient_UserIdAndIsActiveTrueAndBookingStatusAndEndTimeAfter(Long clientId) {
        return bookingRepository.countByClient_UserIdAndIsActiveTrueAndBookingStatusAndEndTimeAfter(clientId, "CONFIRMED", java.time.LocalDateTime.now());
    }

    // Diese Methode wird von der RoomServiceView aufgerufen
    public List<Booking> findAllActiveBookingsForRoom(Long roomId) {
        return bookingRepository.findByMeetingRoom_RoomIdAndIsActiveTrueOrderByStartTimeAsc(roomId);
    }

    public List<Booking> getBookingsForDoorDisplayByRoomName(String roomName) {
        LocalDateTime from = LocalDateTime.now();
        LocalDateTime to = from.plusHours(24);
        return bookingRepository.findActiveBookingsForRoomNameBetween(roomName, from, to);
    }

}
