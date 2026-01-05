package com.gesamtprojekt.application.repositories;

import com.gesamtprojekt.application.model.MeetingRoom;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MeetingRoomRepository extends JpaRepository<MeetingRoom, Long> {

    // Standard-Filter: Räume nach Status (z. B. ACTIVE)
    @EntityGraph(attributePaths = "equipment")
    List<MeetingRoom> findByStatus(String status);

    // Zähler nach Status
    long countByStatus(String status);

    // Suche mit mehreren Filtern: Suche, Gebäude, Status
    @EntityGraph(attributePaths = "equipment")
    @Query("select r from MeetingRoom r " +
            "where (" +
            "   (:status = '' and r.status = 'ACTIVE') " +
            "   or (:status <> '' and r.status = :status)" +
            ") " +
            "and (:building = '' or r.location = :building) " +
            "and (:searchTerm = '' or lower(r.name) like lower(concat('%', :searchTerm, '%')))")
    List<MeetingRoom> searchByFilters(
            @Param("searchTerm") String searchTerm,
            @Param("building") String building,
            @Param("status") String status
    );

    @EntityGraph(attributePaths = "equipment")
    Optional<MeetingRoom> findWithEquipmentByRoomId(Long roomId);

    @Override
    @EntityGraph(attributePaths = "equipment")
    List<MeetingRoom> findAll();
}
