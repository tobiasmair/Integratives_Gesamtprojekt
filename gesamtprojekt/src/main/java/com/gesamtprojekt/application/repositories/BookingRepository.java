package com.gesamtprojekt.application.repositories;

import com.gesamtprojekt.application.model.Booking;
import com.gesamtprojekt.application.model.Client;
import com.gesamtprojekt.application.model.MeetingRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Zeitraum überlappende Buchungen prüfen
    @Query("SELECT COUNT(b) > 0 FROM Booking b " +
            "WHERE b.isActive = true " +
            "AND b.meetingRoom.roomId = :roomId " +
            "AND b.startTime < :endTime " +
            "AND b.endTime > :startTime")
    boolean existsOverlappingBooking(
            @Param("roomId") Long roomId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    // Zeitraum prüfen ohne bestimmte Buchung
    @Query("SELECT COUNT(b) > 0 FROM Booking b " +
            "WHERE b.isActive = true " +
            "AND b.meetingRoom.roomId = :roomId " +
            "AND b.startTime < :endTime " +
            "AND b.endTime > :startTime " +
            "AND b.bookingId <> :excludeBookingId")
    boolean existsOverlappingBookingExcludingId(
            @Param("roomId") Long roomId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("excludeBookingId") Long excludeBookingId
    );

    // Buchungen eines Clients finden
    @Query("SELECT b FROM Booking b WHERE b.isActive = true AND b.client.userId = :clientId")
    List<Booking> findBookingByClientId (@Param("clientId") Long clientId);

}
