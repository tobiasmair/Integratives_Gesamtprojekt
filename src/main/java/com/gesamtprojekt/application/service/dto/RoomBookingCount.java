package com.gesamtprojekt.application.service.dto;

public record RoomBookingCount(
        Long roomId,
        String name,
        String location,
        long bookings
) {}
