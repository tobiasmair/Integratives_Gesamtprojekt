package com.gesamtprojekt.application.ui.room.screens;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.gesamtprojekt.application.model.Booking;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class RoomDefaultScreen extends VerticalLayout {
    public RoomDefaultScreen(String roomName, List<Booking> bookings) {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        H1 title = new H1(roomName);
        title.getStyle().set("margin-top", "0");

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy um HH:mm");
        Optional<Booking> next = bookings.stream()
                .filter(b -> b.getStartTime().isAfter(LocalDateTime.now()))
                .sorted(Comparator.comparing(Booking::getStartTime))
                .findFirst();

        String nextInfo = next.map(b -> "Next booking: " + b.getStartTime().format(dtf))
                .orElse("No upcoming bookings today");

        H2 subTitle = new H2(nextInfo);
        subTitle.getStyle().set("color", "var(--lumo-secondary-text-color)");

        add(title, subTitle);
    }
}