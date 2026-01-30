package com.gesamtprojekt.application.service.implementation;

import com.gesamtprojekt.application.model.Client;
import com.gesamtprojekt.application.model.Equipment;
import com.gesamtprojekt.application.model.MeetingRoom;
import com.gesamtprojekt.application.repositories.MeetingRoomRepository;
import com.gesamtprojekt.application.service.MeetingRoomServiceInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MeetingRoomService implements MeetingRoomServiceInterface {

    private final MeetingRoomRepository meetingRoomRepository;
    private final ClientService clientService;

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
        // Auserhalb Öffnungszeiten leere Liste
        if (startTime.toLocalTime().isBefore(LocalTime.of(7, 0)) ||
                endTime.toLocalTime().isAfter(LocalTime.of(22, 0))) {
            return Collections.emptyList();
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
            return meetingRoomRepository.findByIsActiveTrue();
        }
        // Angepasste Suche mit Query
        return meetingRoomRepository.searchByFilters(q, b, s);
    }

    // Raum erstellen (Default-Status setzen wenn leer)
    @Override
    public MeetingRoom createRoom(MeetingRoom room) {
        if (room.getRoomUser() != null && room.getRoomUser().getUserId() == null) {
            Client tempUser = room.getRoomUser();

            Client persistedUser = clientService.createClient(
                    tempUser.getUsername(),
                    tempUser.getPassword(),
                    tempUser.getUsername() + "@system.local", // Dummy Email
                    tempUser.getDepartment(),
                    tempUser.getUserType(),
                    tempUser.getRole()
            );

            room.setRoomUser(persistedUser);
        }

        if (room.getStatus() == null || room.getStatus().isBlank()) {
            room.setStatus("ACTIVE");
        }
        syncSmartFlags(room);   // Steuerungs-Flags setzen
        return meetingRoomRepository.save(room);
    }

    // Raum aktualisieren (save)
    @Override
    @Transactional
    public void updateRoom(MeetingRoom room) {
        if (room.getRoomUser() != null) {
            Client user = room.getRoomUser();
            String currentInput = user.getPassword();

            // Passwort nur bei Änderung effektiv aktualisieren
            if (currentInput != null && !currentInput.equals("********") && !currentInput.isEmpty()) {
                clientService.updateClientWithPassword(user, currentInput);
            }
        }
        syncSmartFlags(room);   // Steuerungs-Flags setzen
        meetingRoomRepository.save(room);
    }

    // Soft delete (isActive false)
    @Override
    @Transactional
    public void deleteRoom(MeetingRoom room) {
        // Raum mit User aus DB aktuell laden
        MeetingRoom persistentRoom = meetingRoomRepository.findById(room.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found: " + room.getRoomId()));

        // Raum deaktivieren
        persistentRoom.setIsActive(false);

        // Verknüpften User deaktivieren
        if (persistentRoom.getRoomUser() != null) {
            clientService.deleteClient(persistentRoom.getRoomUser());
        }

        meetingRoomRepository.save(persistentRoom);
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

    public List<MeetingRoom> findAllRoomsByFilters(
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

        return meetingRoomRepository.findFilteredRooms(
                building,
                floor,
                minCap,
                equipmentSet,
                equipmentCount);
    }
    // Steuerungs Flags syncen
    private void syncSmartFlags(MeetingRoom room) {
        Set<Equipment> eq = room.getEquipment();

        // Wird Equipment mit Bezeichnung hinterlegt, wird Flag gesetzt
        room.setHasBlindControl(hasEquipment(eq, "Blind Control"));
        room.setHasLightControl(hasEquipment(eq, "Light Control"));
        room.setHasVentilationControl(hasEquipment(eq, "Ventilation Control"));
        room.setHasBeamerControl(hasEquipment(eq, "Beamer Control"));
        room.setHasVacuumRobot(hasEquipment(eq, "Vacuum Robot"));
    }

    private boolean hasEquipment(Set<Equipment> equipmentSet, String name) {
        return equipmentSet.stream()
                .anyMatch(e -> e.getDescription().equalsIgnoreCase(name));
    }
    public Optional<MeetingRoom> findRoomByClient(Client client) {
        return meetingRoomRepository.findByRoomUser_UserId(client.getUserId());
    }


}
