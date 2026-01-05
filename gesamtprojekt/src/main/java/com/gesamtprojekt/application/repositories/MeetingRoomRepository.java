package com.gesamtprojekt.application.repositories;

import com.gesamtprojekt.application.model.MeetingRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MeetingRoomRepository extends JpaRepository<MeetingRoom, Long> {

    List<MeetingRoom> findByIsActiveTrue();

    // Alle freien Räume in einem bestimmten Zeitraum finden
    @Query("SELECT r FROM MeetingRoom r WHERE r.isActive = true AND r.roomId NOT IN (" +
            "SELECT b.meetingRoom.roomId FROM Booking b " +
            "WHERE b.isActive = true AND b.startTime < :endTime AND b.endTime > :startTime)")
    List<MeetingRoom> findAvailableRoomsInTimeframe(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    @Query("SELECT r FROM MeetingRoom r WHERE r.isActive = true AND r.roomId NOT IN (" +
            "SELECT b.meetingRoom.roomId FROM Booking b " +
            "WHERE b.isActive = true " +
            "AND b.startTime < :endTime " +
            "AND b.endTime > :startTime " +
            "AND (:excludeBookingId IS NULL OR b.bookingId <> :excludeBookingId))")
    List<MeetingRoom> findAvailableRoomsExcludingBooking(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("excludeBookingId") Long excludeBookingId
    );

}
