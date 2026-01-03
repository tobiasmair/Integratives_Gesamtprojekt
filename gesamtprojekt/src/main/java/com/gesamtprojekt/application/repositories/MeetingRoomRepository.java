package com.gesamtprojekt.application.repositories;

import com.gesamtprojekt.application.model.MeetingRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MeetingRoomRepository extends JpaRepository<MeetingRoom, Long> {

    // Standard-Filter: Räume nach Status (z. B. ACTIVE)
    List<MeetingRoom> findByStatus(String status);

    // Zähler nach Status
    long countByStatus(String status);

    // Suche mit mehreren Filtern: Suche, Gebäude, Status
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
}
