package com.gesamtprojekt.application.util;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import java.time.LocalDateTime;
import java.time.LocalTime;

// Zentrale utility für Buchungs validierungen
public class BookingValidator {

    public static final LocalTime OPENS_AT = LocalTime.of(7, 0);
    public static final LocalTime CLOSES_AT = LocalTime.of(22, 0);

    public static boolean isTimeRangeValid(LocalDateTime start, LocalDateTime end, boolean isEditMode) {
        // Basis-Check
        if (start == null || end == null) return false;

        // Endzeit nach Startzeit
        if (end.isBefore(start) || end.isEqual(start)) {
            showError("End time must be after start time");
            return false;
        }

        // In der Zukunft
        if (!isEditMode && start.isBefore(LocalDateTime.now())) {
            showError("Start time must be in the future");
            return false;
        }

        // Öffnungszeiten
        if (start.toLocalTime().isBefore(OPENS_AT) || end.toLocalTime().isAfter(CLOSES_AT)) {
            showError("The building is closed! (07:00 - 22:00)");
            return false;
        }

        return true;
    }

    private static void showError(String message) {
        Notification.show(message, 3000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    public static LocalTime roundToNextHalfHour(LocalTime time) {
        int minutes = time.getMinute();
        if (minutes == 0) return time.withSecond(0).withNano(0);
        if (minutes <= 30) return time.withMinute(30).withSecond(0).withNano(0);
        return time.plusHours(1).withMinute(0).withSecond(0).withNano(0);
    }

    // Bereich Öffnungszeiten
    public static LocalTime clampToOpeningHours(LocalTime time) {
        if (time == null) return OPENS_AT;

        LocalTime latestStart = CLOSES_AT.minusMinutes(30);

        if (time.isBefore(OPENS_AT)) return OPENS_AT;
        if (time.isAfter(latestStart)) return latestStart;
        return time;
    }
}
