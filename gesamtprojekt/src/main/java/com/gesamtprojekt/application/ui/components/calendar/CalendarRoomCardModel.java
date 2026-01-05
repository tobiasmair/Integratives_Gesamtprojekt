package com.gesamtprojekt.application.ui.components.calendar;

import java.util.List;

public record CalendarRoomCardModel(
        Long roomId,
        String name,
        String building,
        Integer capacity,
        Integer floor,
        List<String> tags,
        String imagePath
) {}
