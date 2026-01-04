package com.gesamtprojekt.application.service.implementation;

import com.gesamtprojekt.application.model.Booking;
import com.gesamtprojekt.application.model.Client;
import com.gesamtprojekt.application.model.MeetingRoom;
import com.gesamtprojekt.application.repositories.BookingRepository;
import com.gesamtprojekt.application.repositories.MeetingRoomRepository;
import com.gesamtprojekt.application.service.BookingServiceInterface;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.awt.print.Book;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService implements BookingServiceInterface {

    private final BookingRepository bookingRepository;

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
        bookingRepository.save(booking);
    }

}
