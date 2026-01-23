package com.gesamtprojekt.application.ui.door;

import com.gesamtprojekt.application.model.Booking;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DoorOccupiedScreen extends VerticalLayout {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    public DoorOccupiedScreen(String roomName, Booking current, List<Booking> upcoming) {
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        setWidthFull();

        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        VerticalLayout block = new VerticalLayout();
        block.setWidthFull();
        block.setMaxWidth("520px");
        block.setPadding(false);
        block.setSpacing(false);
        block.setAlignItems(FlexComponent.Alignment.CENTER);

        H1 roomNumber = new H1(roomName);
        roomNumber.getStyle()
                .set("margin", "0")
                .set("font-size", "64px")
                .set("font-weight", "700");

        Paragraph status = new Paragraph("Room occupied");
        status.getStyle()
                .set("margin-top", "18px")
                .set("margin-bottom", "6px")
                .set("font-size", "20px")
                .set("font-weight", "700");

        Paragraph range = new Paragraph("From: " + formatTime(current.getStartTime()) + "  —  To: " + formatTime(current.getEndTime()));
        range.getStyle()
                .set("margin", "0")
                .set("font-size", "18px")
                .set("font-weight", "600")
                .set("color", "var(--lumo-secondary-text-color)");

        Div divider = new Div();
        divider.getStyle()
                .set("width", "140px")
                .set("height", "2px")
                .set("background", "var(--lumo-contrast-20pct)")
                .set("margin", "14px 0 12px 0")
                .set("border-radius", "999px");

        block.add(roomNumber, status, range, divider);

        if (upcoming != null && !upcoming.isEmpty()) {
            Paragraph title = new Paragraph("Upcoming bookings today");
            title.getStyle()
                    .set("margin", "0 0 8px 0")
                    .set("font-size", "18px")
                    .set("font-weight", "600")
                    .set("color", "var(--lumo-secondary-text-color)");
            block.add(title);

            for (Booking b : upcoming) {
                Paragraph line = new Paragraph(formatTime(b.getStartTime()) + " – " + formatTime(b.getEndTime()));
                line.getStyle()
                        .set("margin", "4px 0")
                        .set("font-size", "18px")
                        .set("font-weight", "600")
                        .set("color", "var(--lumo-secondary-text-color)");
                block.add(line);
            }
        } else {
            Paragraph none = new Paragraph("No upcoming bookings today");
            none.getStyle()
                    .set("margin", "0")
                    .set("font-size", "18px")
                    .set("font-weight", "600")
                    .set("color", "var(--lumo-secondary-text-color)");
            block.add(none);
        }

        add(block);
    }

    private String formatTime(LocalDateTime dt) {
        if (dt == null) return "-";
        return dt.toLocalTime().format(TIME_FMT);
    }
}
