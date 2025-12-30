package com.gesamtprojekt.application.service.implementation;

import com.gesamtprojekt.application.repositories.BookingRepository;
import com.gesamtprojekt.application.service.BookingServiceInterface;
import org.springframework.stereotype.Service;

@Service
public class BookingService implements BookingServiceInterface {

    private final BookingRepository bookingRepository;

    public BookingService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

}
