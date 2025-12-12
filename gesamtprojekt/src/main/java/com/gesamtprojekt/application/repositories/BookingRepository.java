package com.gesamtprojekt.application.repositories;

import com.gesamtprojekt.application.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {
}
