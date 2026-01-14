package com.gesamtprojekt.application.service.implementation;

import com.gesamtprojekt.application.model.MeetingRoom;
import com.gesamtprojekt.application.repositories.MeetingRoomRepository;
import com.gesamtprojekt.application.service.MeetingRoomServiceInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MeetingRoomService implements MeetingRoomServiceInterface {

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

    // Aktive Räume in Zeitabschnitt, aktuelle Buchung ausschließen
    public List<MeetingRoom> findAvailableRoomsExcludingBooking(
            LocalDateTime startTime,
            LocalDateTime endTime,
            Long excludeBookingId) {
        if (startTime == null || endTime == null) {
            return findAvailableRooms();
        }
        return meetingRoomRepository.findAvailableRoomsExcludingBooking(startTime, endTime, excludeBookingId);
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
            return meetingRoomRepository.findByIsActiveTrueAndStatus("ACTIVE");
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

    // Soft delete (isActive false)
    @Override
    public void deleteRoom(MeetingRoom room) {
        room.setIsActive(false);
        meetingRoomRepository.save(room);
    }

    // Statistik: Anzahl Räume
    @Override
    public long countRooms() {
        return meetingRoomRepository.countByIsActiveTrueAndStatus("ACTIVE");
    }

    // Statistik: Gesamtkapazität
    @Override
    public int sumCapacity() {
        return meetingRoomRepository.findByIsActiveTrueAndStatus("ACTIVE").stream()
                .map(MeetingRoom::getCapacity)
                .filter(Objects::nonNull)
                .mapToInt(i -> i)
                .sum();
    }

    // Statistik: Anzahl unterschiedlicher Gebäude
    @Override
    public long countBuildings() {
        return meetingRoomRepository.findByIsActiveTrueAndStatus("ACTIVE").stream()
                .map(MeetingRoom::getLocation)
                .filter(Objects::nonNull)
                .distinct()
                .count();
    }

    @Transactional(readOnly = true)
    public List<MeetingRoom> findRoomsForCalendar() {
        // z.B. nur ACTIVE, inkl. equipment (weil findByStatus jetzt @EntityGraph hat)
        return meetingRoomRepository.findByIsActiveTrueAndStatus("ACTIVE");
    }

    public List<MeetingRoom> findCalendarRooms(
            LocalDateTime startTime,
            LocalDateTime endTime,
            String building,
            String floorStr,
            String minCapStr,
            Set<String> equipmentSet) {

        // Floor-String zu Integer konvertieren
        Integer floor = null;
        if (floorStr != null && !floorStr.equals("Any Floor")) {
            try {
                floor = Integer.valueOf(floorStr);
            } catch (NumberFormatException e) {
                floor = null;
            }
        }

        // Min capacity Logik
        int minCap = 0;
        if (minCapStr != null && minCapStr.endsWith("+")) {
            minCap = Integer.parseInt(minCapStr.replace("+", ""));
        }

        // Equipment Count für Query
        long equipmentCount = (equipmentSet != null && !equipmentSet.isEmpty()) ? equipmentSet.size() : 0;

        return meetingRoomRepository.findFilteredAvailableRooms(
                startTime,
                endTime,
                building,
                floor,
                minCap,
                equipmentSet,
                equipmentCount);
    }

}
