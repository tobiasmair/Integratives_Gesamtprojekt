package com.gesamtprojekt.application.repositories;

import com.gesamtprojekt.application.model.MeetingRoom;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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

    // Query für calendar view: verfügbare Räume mit bestimmten Eigenschaften
    @EntityGraph(attributePaths = "equipment")
    @Query("SELECT DISTINCT r FROM MeetingRoom r " +
            "WHERE r.isActive = true AND r.status = 'ACTIVE' " +
            "AND (:building = 'All Buildings' OR r.location = :building) " +
            "AND (:floor IS NULL OR r.floor = :floor) " +
            "AND (:minCap = 0 OR r.capacity >= :minCap) " +
            "AND (:equipmentCount = 0 OR " +
            "    (SELECT COUNT(e) FROM r.equipment e WHERE e.description IN :equipmentSet) = :equipmentCount) " +
            "AND r.roomId NOT IN (" +
            "    SELECT b.meetingRoom.roomId FROM Booking b " +
            "    WHERE b.isActive = true AND b.startTime < :endTime AND b.endTime > :startTime" +
            ")")
    List<MeetingRoom> findFilteredAvailableRooms(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("building") String building,
            @Param("floor") Integer floor,
            @Param("minCap") int minCap,
            @Param("equipmentSet") Set<String> equipmentSet,
            @Param("equipmentCount") long equipmentCount);

    // Standard-Filter: Räume nach Status (z. B. ACTIVE)
    @EntityGraph(attributePaths = "equipment")
    List<MeetingRoom> findByIsActiveTrueAndStatus(String status);

    // Zähler nach Status
    long countByIsActiveTrueAndStatus(String status);

    // Suche mit mehreren Filtern: Suche, Gebäude, Status
    @EntityGraph(attributePaths = "equipment")
    @Query("select r from MeetingRoom r " +
            "where (" +
            "   (:status = '' and r.status = 'ACTIVE') " +
            "   or (:status <> '' and r.status = :status)" +
            ") " +
            "and (:building = '' or r.location = :building) " +
            "and (:searchTerm = '' or lower(r.name) like lower(concat('%', :searchTerm, '%'))) " +
            "and r.isActive = true")
    List<MeetingRoom> searchByFilters(
            @Param("searchTerm") String searchTerm,
            @Param("building") String building,
            @Param("status") String status
    );

    @EntityGraph(attributePaths = {"equipment", "roomUser"})
    Optional<MeetingRoom> findWithEquipmentByRoomId(Long roomId);

    @Override
    @EntityGraph(attributePaths = "equipment")
    List<MeetingRoom> findAll();

    // Query für Browse Mode: Alle Räume ohne Verfügbarkeitsprüfung
    @EntityGraph(attributePaths = "equipment")
    @Query("SELECT DISTINCT r FROM MeetingRoom r " +
            "WHERE r.isActive = true AND r.status = 'ACTIVE' " +
            "AND (:building = 'All Buildings' OR r.location = :building) " +
            "AND (:floor IS NULL OR r.floor = :floor) " +
            "AND (:minCap = 0 OR r.capacity >= :minCap) " +
            "AND (:equipmentCount = 0 OR " +
            "    (SELECT COUNT(e) FROM r.equipment e WHERE e.description IN :equipmentSet) = :equipmentCount)")
    List<MeetingRoom> findFilteredRooms(
            @Param("building") String building,
            @Param("floor") Integer floor,
            @Param("minCap") int minCap,
            @Param("equipmentSet") Set<String> equipmentSet,
            @Param("equipmentCount") long equipmentCount);

    // Findet den Raum, der einem bestimmten Client-Account zugeordnet ist
    Optional<MeetingRoom> findByRoomUser_UserId(Long userId);

    @Query("select r from MeetingRoom r where r.isActive = true and r.name = :name")
    Optional<MeetingRoom> findActiveByExactName(@Param("name") String name);




}
