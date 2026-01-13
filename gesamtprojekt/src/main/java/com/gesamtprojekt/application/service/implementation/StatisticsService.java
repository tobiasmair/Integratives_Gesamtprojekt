package com.gesamtprojekt.application.service.implementation;

import com.gesamtprojekt.application.repositories.BookingRepository;
import com.gesamtprojekt.application.repositories.ClientRepository;
import com.gesamtprojekt.application.repositories.EquipmentRepository;
import com.gesamtprojekt.application.repositories.MeetingRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final BookingRepository bookingRepository;
    private final MeetingRoomRepository meetingRoomRepository;
    private final ClientRepository clientRepository;
    private final EquipmentRepository equipmentRepository;

    public long getTotalBookings() {
        return bookingRepository.countByIsActiveTrue();
    }

    public long getActiveRooms() {
        // aktive Datensätze + Status ACTIVE (damit INACTIVE nicht als aktiv zählt)
        return meetingRoomRepository.findByIsActiveTrue().stream()
                .filter(r -> "ACTIVE".equalsIgnoreCase(r.getStatus()))
                .count();
    }

    public int getTotalCapacity() {
        return meetingRoomRepository.findByIsActiveTrue().stream()
                .filter(r -> "ACTIVE".equalsIgnoreCase(r.getStatus()))
                .map(r -> r.getCapacity() == null ? 0 : r.getCapacity())
                .reduce(0, Integer::sum);
    }

    public long getBuildingsCount() {
        return meetingRoomRepository.findByIsActiveTrue().stream()
                .filter(r -> r.getLocation() != null && !r.getLocation().isBlank())
                .map(r -> r.getLocation().trim().toLowerCase())
                .distinct()
                .count();
    }

    public long getActiveUsers() {
        // Achtung: Methode heißt in deinem Repo countByisActiveTrue() (kleines i)
        return clientRepository.countByisActiveTrue();
    }

    public long getEquipmentCount() {
        return equipmentRepository.count();
    }

    public long getBookedRoomsToday() {
        LocalDate today = LocalDate.now();
        LocalDateTime from = today.atStartOfDay();
        LocalDateTime to = today.plusDays(1).atStartOfDay();
        return bookingRepository.countDistinctRoomsBookedBetween(from, to);
    }

    public long getBookedRoomsThisWeek() {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        LocalDateTime from = weekStart.atStartOfDay();
        LocalDateTime to = weekStart.plusWeeks(1).atStartOfDay();
        return bookingRepository.countDistinctRoomsBookedBetween(from, to);
    }

    public long getBookedRoomsThisMonth() {
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDateTime from = monthStart.atStartOfDay();
        LocalDateTime to = monthStart.plusMonths(1).atStartOfDay();
        return bookingRepository.countDistinctRoomsBookedBetween(from, to);
    }

}
