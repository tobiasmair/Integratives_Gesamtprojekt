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

    long countByIsActiveTrue();

    @Query("SELECT COUNT(DISTINCT b.meetingRoom.roomId) " +
            "FROM Booking b " +
            "WHERE b.isActive = true " +
            "AND b.startTime >= :from " +
            "AND b.startTime < :to")
    long countDistinctRoomsBookedBetween(@Param("from") LocalDateTime from,
                                         @Param("to") LocalDateTime to);

    // Zeitraum: alle aktiven Buchungen, die den Zeitraum überlappen
    @Query("""
    SELECT b FROM Booking b
    WHERE b.isActive = true
      AND b.startTime < :end
      AND b.endTime > :start
""")
    List<Booking> findActiveBookingsOverlapping(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // Top Räume nach Anzahl Buchungen im Zeitraum
    @Query("""
    SELECT b.meetingRoom.roomId, b.meetingRoom.name, b.meetingRoom.location, COUNT(b)
    FROM Booking b
    WHERE b.isActive = true
      AND b.startTime < :end
      AND b.endTime > :start
    GROUP BY b.meetingRoom.roomId, b.meetingRoom.name, b.meetingRoom.location
    ORDER BY COUNT(b) DESC
""")
    List<Object[]> topRoomsByBookingCount(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // Buchungen pro Monat (für ein Jahr)
    @Query("""
    SELECT extract(month from b.startTime), COUNT(b)
    FROM Booking b
    WHERE b.isActive = true
      AND extract(year from b.startTime) = :year
    GROUP BY extract(month from b.startTime)
    ORDER BY extract(month from b.startTime)
""")
    List<Object[]> bookingsPerMonth(@Param("year") int year);


    // Counts für Today/Week/Month (einfach über StartTime)
    long countByIsActiveTrueAndStartTimeBetween(LocalDateTime start, LocalDateTime end);


    // Anzahl der aktiven Buchungen zählen
    long countByIsActiveTrueAndBookingStatusAndEndTimeAfter(String status, LocalDateTime currentTime);

    // Zählt aktive Buchungen für einen bestimmten Client
    long countByClient_UserIdAndIsActiveTrueAndBookingStatusAndEndTimeAfter(Long clientId, String status, LocalDateTime currentTime);

    // Für Notification Task
    List<Booking> findByStartTimeAndIsActiveTrue(LocalDateTime startTime);

    List<Booking> findByEndTimeAndIsActiveTrue(LocalDateTime startTime);

}
