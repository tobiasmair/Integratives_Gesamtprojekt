package com.gesamtprojekt.application.service.implementation;

import com.gesamtprojekt.application.model.Booking;
import com.gesamtprojekt.application.model.MeetingRoom;
import com.gesamtprojekt.application.repositories.BookingRepository;
import com.gesamtprojekt.application.repositories.EquipmentRepository;
import com.gesamtprojekt.application.repositories.MeetingRoomRepository;
import com.gesamtprojekt.application.service.dto.MonthlyCount;
import com.gesamtprojekt.application.service.dto.RoomBookingCount;
import com.gesamtprojekt.application.service.dto.RoomUtilization;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final BookingRepository bookingRepository;
    private final MeetingRoomRepository meetingRoomRepository;
    private final EquipmentRepository equipmentRepository;

    // KPI Basics
    public long getTotalActiveBookings() {
        return bookingRepository.countByIsActiveTrue();
    }

    public long getActiveRooms() {
        return meetingRoomRepository.findByIsActiveTrue().size();
    }

    public long getActiveLocations() {
        return meetingRoomRepository.findByIsActiveTrue().stream()
                .map(MeetingRoom::getLocation)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toSet())
                .size();
    }

    public long getTotalEquipment() {
        return equipmentRepository.count();
    }

    // KPI: Today/Week/Month
    public long getBookedRoomsToday() {
        LocalDate today = LocalDate.now();
        return bookingRepository.countByIsActiveTrueAndStartTimeBetween(
                today.atStartOfDay(),
                today.plusDays(1).atStartOfDay()
        );
    }

    public long getBookedRoomsLast7Days() {
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusDays(7);
        return bookingRepository.countByIsActiveTrueAndStartTimeBetween(start, end);
    }

    public long getBookedRoomsLast30Days() {
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusDays(30);
        return bookingRepository.countByIsActiveTrueAndStartTimeBetween(start, end);
    }

    // 1) Top Räume nach Buchungen (z.B. letzte 30 Tage)
    public List<RoomBookingCount> getTopRooms(LocalDateTime start, LocalDateTime end, int limit) {
        return bookingRepository.topRoomsByBookingCount(start, end).stream()
                .limit(limit)
                .map(row -> new RoomBookingCount(
                        (Long) row[0],
                        (String) row[1],
                        (String) row[2],
                        ((Number) row[3]).longValue()
                ))
                .toList();
    }

    // 2) Buchungen pro Monat (Jahr)
    public List<MonthlyCount> getBookingsPerMonth(int year) {
        Map<Integer, Long> monthToCount = new HashMap<>();
        for (Object[] row : bookingRepository.bookingsPerMonth(year)) {
            int month = ((Number) row[0]).intValue();
            long count = ((Number) row[1]).longValue();
            monthToCount.put(month, count);
        }

        // 12 Monate auffüllen (auch wenn 0)
        List<MonthlyCount> out = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            out.add(new MonthlyCount(m, monthToCount.getOrDefault(m, 0L)));
        }
        return out;
    }

    // 3) Auslastung je Raum (in %)
    // Definition: (Summe gebuchte Minuten im Zeitraum) / (Zeitraum-Minuten) * 100
    public List<RoomUtilization> getRoomUtilization(LocalDateTime start, LocalDateTime end) {
        long periodMinutes = Math.max(1, Duration.between(start, end).toMinutes());

        // alle aktiven Räume
        List<MeetingRoom> rooms = meetingRoomRepository.findByIsActiveTrue();

        // alle Buchungen im Zeitraum (überlappend)
        List<Booking> bookings = bookingRepository.findActiveBookingsOverlapping(start, end);

        // je Raum Minuten aufsummieren
        Map<Long, Long> minutesByRoom = new HashMap<>();
        for (Booking b : bookings) {
            Long roomId = b.getMeetingRoom().getRoomId();

            LocalDateTime s = b.getStartTime().isBefore(start) ? start : b.getStartTime();
            LocalDateTime e = b.getEndTime().isAfter(end) ? end : b.getEndTime();

            long minutes = Math.max(0, Duration.between(s, e).toMinutes());
            minutesByRoom.merge(roomId, minutes, Long::sum);
        }

        return rooms.stream()
                .map(r -> {
                    long used = minutesByRoom.getOrDefault(r.getRoomId(), 0L);
                    double pct = (used * 100.0) / periodMinutes;
                    return new RoomUtilization(r.getRoomId(), r.getName(), r.getLocation(), r.getFloor(), pct);
                })
                .sorted(Comparator.comparingDouble(RoomUtilization::utilizationPercent).reversed())
                .toList();
    }

    // Helper: Standardzeitraum "letzte 30 Tage"
    public LocalDateTime defaultStart30Days() {
        return LocalDateTime.now().minusDays(30);
    }

    public LocalDateTime now() {
        return LocalDateTime.now();
    }
}
