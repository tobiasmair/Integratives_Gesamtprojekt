package com.gesamtprojekt.application.service.implementation;

import com.gesamtprojekt.application.model.MeetingRoom;
import com.gesamtprojekt.application.repositories.MeetingRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MeetingRoomService {

    private final MeetingRoomRepository meetingRoomRepository;

    // Aktive Räume finden
    public List<MeetingRoom> findAvailableRooms() {
        return meetingRoomRepository.findByIsActiveTrue();
    }

    // Aktive Räume in Zeitabschnitt
    public List<MeetingRoom> findAvailableRoomsInTimeframe(
            LocalDateTime startTime,
            LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            return findAvailableRooms();
        }
        return meetingRoomRepository.findAvailableRoomsInTimeframe(startTime, endTime);
    }

    public List<MeetingRoom> findAvailableRoomsExcludingBooking(
            LocalDateTime startTime,
            LocalDateTime endTime,
            Long excludeBookingId) {
        if (startTime == null || endTime == null) {
            return findAvailableRooms();
        }
        return meetingRoomRepository.findAvailableRoomsExcludingBooking(startTime, endTime, excludeBookingId);
import com.gesamtprojekt.application.service.MeetingRoomServiceInterface;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class MeetingRoomService implements MeetingRoomServiceInterface {

    private final MeetingRoomRepository meetingRoomRepository;

    public MeetingRoomService(MeetingRoomRepository meetingRoomRepository) {
        this.meetingRoomRepository = meetingRoomRepository;
    }

    public MeetingRoom findRoomForEdit(Long roomId) {
        return meetingRoomRepository.findWithEquipmentByRoomId(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found: " + roomId));
    }

    // Filter: entweder Default ACTIVE, oder searchByFilters
    @Override
    public List<MeetingRoom> findAllRooms(String search, String building, String status) {
        String b = (building != null && !building.equals("All Buildings")) ? building : "";
        String s = (status != null && !status.equals("All Status")) ? status : "";
        String q = (search != null) ? search : "";

        if (q.isEmpty() && b.isEmpty() && s.isEmpty()) {
            // Default: nur ACTIVE Räume
            return meetingRoomRepository.findByStatus("ACTIVE");
        }
        // Angepasste Suche mit Query
        return meetingRoomRepository.searchByFilters(q, b, s);
    }

    // Raum erstellen (Default-Status setzen wenn leer)
    @Override
    public MeetingRoom createRoom(MeetingRoom room) {
        if (room.getStatus() == null || room.getStatus().isBlank()) {
            room.setStatus("ACTIVE");
        }
        return meetingRoomRepository.save(room);
    }

    // Raum aktualisieren (save)
    @Override
    public void updateRoom(MeetingRoom room) {
        meetingRoomRepository.save(room);
    }

    // Soft delete
    @Override
    public void deleteRoom(MeetingRoom room) {
        room.setStatus("INACTIVE");
        meetingRoomRepository.save(room);
    }

    // Statistik: Anzahl Räume
    @Override
    public long countRooms() {
        return meetingRoomRepository.countByStatus("ACTIVE");
    }

    // Statistik: Gesamtkapazität
    @Override
    public int sumCapacity() {
        return meetingRoomRepository.findByStatus("ACTIVE").stream()
                .map(MeetingRoom::getCapacity)
                .filter(Objects::nonNull)
                .mapToInt(i -> i)
                .sum();
    }

    // Statistik: Anzahl unterschiedlicher Gebäude
    @Override
    public long countBuildings() {
        return meetingRoomRepository.findByStatus("ACTIVE").stream()
                .map(MeetingRoom::getLocation)
                .filter(Objects::nonNull)
                .distinct()
                .count();
    }

    @Transactional(readOnly = true)
    public List<MeetingRoom> findRoomsForCalendar() {
        // z.B. nur ACTIVE, inkl. equipment (weil findByStatus jetzt @EntityGraph hat)
        return meetingRoomRepository.findByStatus("ACTIVE");
    }

}
