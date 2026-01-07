package com.gesamtprojekt.application.ui.admin.statistics;

import com.gesamtprojekt.application.model.Booking;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class StatisticsQueryService {

    private final EntityManager em;

    public StatisticsQueryService(EntityManager em) {
        this.em = em;
    }

    public StatsSummary getSummary(LocalDate from, LocalDate to) {
        if (from == null || to == null || to.isBefore(from)) {
            return empty();
        }

        LocalDateTime fromDT = from.atStartOfDay();
        LocalDateTime toDT = to.plusDays(1).atStartOfDay(); // exclusive

        TypedQuery<Booking> q = em.createQuery("""
                select b
                from Booking b
                where b.isActive = true
                  and b.startTime >= :from
                  and b.startTime < :to
                """, Booking.class);

        q.setParameter("from", fromDT);
        q.setParameter("to", toDT);

        List<Booking> bookings = q.getResultList();

        long totalBookings = bookings.size();

        long totalBookedMinutes = 0;

        Map<LocalDate, Long> bookingsPerDay = new TreeMap<>();
        Map<String, Long> statusCounts = new HashMap<>();
        Map<String, Long> roomCounts = new HashMap<>();

        // NEW
        Map<String, Long> userCounts = new HashMap<>();
        Map<String, Long> cancelledRoomCounts = new HashMap<>();

        long totalMinutesForAvg = 0;
        long countForAvg = 0;

        for (Booking b : bookings) {
            // per day
            if (b.getStartTime() != null) {
                bookingsPerDay.merge(b.getStartTime().toLocalDate(), 1L, Long::sum);
            }

            // status counts
            String status = String.valueOf(b.getBookingStatus());
            statusCounts.merge(status, 1L, Long::sum);

            // room counts
            String roomName = (b.getMeetingRoom() != null) ? b.getMeetingRoom().getName() : null;
            if (roomName != null) {
                roomCounts.merge(roomName, 1L, Long::sum);
            }

            // total booked minutes + avg duration
            if (b.getStartTime() != null && b.getEndTime() != null && b.getEndTime().isAfter(b.getStartTime())) {
                long minutes = Duration.between(b.getStartTime(), b.getEndTime()).toMinutes();
                if (minutes > 0) {
                    totalBookedMinutes += minutes;

                    totalMinutesForAvg += minutes;
                    countForAvg++;
                }
            }

            // top users
            String username = (b.getClient() != null) ? b.getClient().getUsername() : null;
            if (username != null) {
                userCounts.merge(username, 1L, Long::sum);
            }

            // most cancelled room
            if (status != null && status.toUpperCase(Locale.ROOT).contains("CANCEL")) {
                if (roomName != null) {
                    cancelledRoomCounts.merge(roomName, 1L, Long::sum);
                }
            }
        }

        long avgDurationMinutes = (countForAvg > 0) ? (totalMinutesForAvg / countForAvg) : 0;

        List<RoomStat> topRooms = roomCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(e -> new RoomStat(e.getKey(), e.getValue()))
                .toList();

        // NEW: top users
        List<UserStat> topUsers = userCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(e -> new UserStat(e.getKey(), e.getValue()))
                .toList();

        // NEW: most cancelled room
        MostCancelledRoom mostCancelledRoom = cancelledRoomCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(e -> new MostCancelledRoom(e.getKey(), e.getValue()))
                .orElse(new MostCancelledRoom("-", 0));

        return new StatsSummary(
                totalBookings,
                avgDurationMinutes,
                totalBookedMinutes,
                bookingsPerDay,
                statusCounts,
                topRooms,
                topUsers,
                mostCancelledRoom
        );
    }

    private StatsSummary empty() {
        return new StatsSummary(
                0, 0, 0,
                Map.of(), Map.of(),
                List.of(), List.of(),
                new MostCancelledRoom("-", 0)
        );
    }

    public record StatsSummary(
            long totalBookings,
            long avgDurationMinutes,
            long totalBookedMinutes,                // NEW
            Map<LocalDate, Long> bookingsPerDay,
            Map<String, Long> statusCounts,
            List<RoomStat> topRooms,
            List<UserStat> topUsers,                // NEW
            MostCancelledRoom mostCancelledRoom     // NEW
    ) {}

    public record RoomStat(String roomName, long bookings) {}
    public record UserStat(String username, long bookings) {}
    public record MostCancelledRoom(String roomName, long cancelledBookings) {}
}
