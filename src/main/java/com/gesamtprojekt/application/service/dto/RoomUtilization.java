package com.gesamtprojekt.application.service.dto;

public record RoomUtilization(
        Long roomId,
        String name,
        String location,
        Integer floor,
        double utilizationPercent
) {}
