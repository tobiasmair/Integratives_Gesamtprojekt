package com.gesamtprojekt.application.ui.door;

import com.gesamtprojekt.application.model.Booking;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DoorUpcomingScreen extends VerticalLayout {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    public DoorUpcomingScreen(String roomName, List<Booking> upcoming) {
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
        block.add(roomNumber);

        Paragraph spacer = new Paragraph();
        spacer.getStyle().set("margin", "22px 0 0 0");
        block.add(spacer);

        if (upcoming != null && !upcoming.isEmpty()) {
            Paragraph title = new Paragraph("UPCOMING TODAY:");
            title.getStyle()
                    .set("margin", "0 0 16px 0")
                    .set("letter-spacing", "0.12em")
                    .set("font-size", "14px")
                    .set("font-weight", "600")
                    .set("color", "var(--lumo-secondary-text-color)");
            block.add(title);

            for (Booking b : upcoming) {
                Paragraph line = new Paragraph(formatTime(b.getStartTime()) + " - " + formatTime(b.getEndTime()));
                line.getStyle()
                        .set("margin", "10px 0")
                        .set("font-size", "22px")
                        .set("font-weight", "700");
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
