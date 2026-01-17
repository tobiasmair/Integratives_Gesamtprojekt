package com.gesamtprojekt.application.service.dto;

public enum NotificationType {
    CONFIRMATION("Booking confirmed for room %s. Your Code: %s."),
    REMINDER_START("Starts in 15 min in room %s. Your Code: %s."),
    REMINDER_END("Ends in 5 min in room %s"),
    MISSED("Room %s was released (no-show)");

    private final String template;

    NotificationType(String template) {
        this.template = template;
    }

    public String format(String roomName, String code) {
        return String.format(template, roomName, code);
    }
}
