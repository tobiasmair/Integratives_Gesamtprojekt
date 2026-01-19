package com.gesamtprojekt.application.service.implementation;

import com.gesamtprojekt.application.model.Booking;
import com.gesamtprojekt.application.repositories.BookingRepository;
import com.gesamtprojekt.application.service.dto.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class NotificationTask {

    private final BookingRepository bookingRepository;
    private final NotificationService notificationService;

    // Prüft jede Minute auf anstehende Buchungen
    @Scheduled(cron = "0 * * * * *")
    public void generateStartingSoonNotifications() {
        // Buchungen die in 15 Minuten starten
        LocalDateTime targetTime = LocalDateTime.now()
                .plusMinutes(15)
                .withSecond(0)
                .withNano(0);

        List<Booking> upcomingBookings = bookingRepository.findByStartTimeAndIsActiveTrue(targetTime);

        for (Booking booking : upcomingBookings) {
            notificationService.createNotification(
                    booking,
                    NotificationType.REMINDER_START
            );
        }
    }

    // Prüft jede Minute auf endende Buchungen
    @Scheduled(cron = "0 * * * * *")
    public void generateEndingSoonNotifications() {
        // Buchungen die in 5 Minuten enden
        LocalDateTime targetTime = LocalDateTime.now()
                .plusMinutes(5)
                .withSecond(0)
                .withNano(0);

        List<Booking> endingBookings = bookingRepository.findByEndTimeAndIsActiveTrue(targetTime);

        for (Booking booking : endingBookings) {
            notificationService.createNotification(
                    booking,
                    NotificationType.REMINDER_END
            );
        }
    }
}