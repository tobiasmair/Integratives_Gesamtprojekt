package com.gesamtprojekt.application.service.implementation;

import com.gesamtprojekt.application.model.Booking;
import com.gesamtprojekt.application.model.Client;
import com.gesamtprojekt.application.model.MeetingRoom;
import com.gesamtprojekt.application.model.Notification;
import com.gesamtprojekt.application.repositories.BookingRepository;
import com.gesamtprojekt.application.repositories.MeetingRoomRepository;
import com.gesamtprojekt.application.service.BookingServiceInterface;
import com.gesamtprojekt.application.service.dto.NotificationType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.awt.print.Book;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService implements BookingServiceInterface {

    private final BookingRepository bookingRepository;
    private final NotificationService notificationService;

    @Transactional
    public void createBooking(Booking booking) {
        // Auf Überschneidung prüfen
        boolean conflict = bookingRepository.existsOverlappingBooking(
                booking.getMeetingRoom().getRoomId(),
                booking.getStartTime(), booking.getEndTime()
        );

        if (conflict) {
            throw new RuntimeException("Booking conflict detected for the selected room and time.");
        }
        bookingRepository.save(booking);

        // Notification
        notificationService.createNotification(
                booking.getClient(),
                booking.getMeetingRoom(),
                NotificationType.CONFIRMATION
        );

        // Notification für kurzfristige Buchunen
        LocalDateTime now = LocalDateTime.now();
        if (booking.getStartTime().isBefore(now.plusMinutes(15)) &&
            booking.getStartTime().isAfter(now)) {

            notificationService.createNotification(
                    booking.getClient(),
                    booking.getMeetingRoom(),
                    NotificationType.REMINDER_START
            );
        }
    }

    // Buchungen eines Clients finden
    public List<Booking> findBookingByClientId(Long clientId) {
        return bookingRepository.findBookingByClientId(clientId);
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
            throw new RuntimeException("Booking conflict detected for the selected room and time.");
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

}
