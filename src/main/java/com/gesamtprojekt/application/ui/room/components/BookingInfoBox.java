package com.gesamtprojekt.application.ui.room.components;

import com.gesamtprojekt.application.model.Booking;
import com.gesamtprojekt.application.model.MeetingRoom;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * The Info-Box is displayed on the Dashboard and shows details of the current booking.
 * shows end-time of the booking, a dynamic time reminder 5 minutes before the end-time and a button to end the booking manually
 */
public class BookingInfoBox extends VerticalLayout {

    private final MeetingRoom room;

    public BookingInfoBox(Booking booking, Runnable onFinish, MeetingRoom room) {
        this.room = room;

        // styling of the box
        setWidth("300px");
        setHeight("315px");
        getStyle().set("background-color", "var(--lumo-base-color)");
        getStyle().set("border", "1px solid var(--lumo-contrast-20pct)");
        getStyle().set("border-radius", "var(--lumo-border-radius-l)");
        getStyle().set("padding", "var(--lumo-space-l)");

        setJustifyContentMode(JustifyContentMode.BETWEEN);
        setAlignItems(Alignment.CENTER);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        // Container for the texts
        VerticalLayout topText = new VerticalLayout();
        topText.setAlignItems(Alignment.CENTER);
        topText.setPadding(false);
        topText.setSpacing(false);

        topText.add(new Span("Current Meeting"));
        topText.add(new H3("End Time: " + booking.getEndTime().format(formatter)));

        room.setHasLightControl(true);
        topText.add(new H3("HasLightControl: " + room.getHasLightControl()));
        topText.add(new H3("Vaccum: " + room.getHasVacuumRobot()));
        topText.add(new H3("Whiteboard: " + room.getHasWhiteboard()));
        topText.add(new H3("Door: " + room.getHasDoorControl()));




        // checks the remaining time till the end of the booking
        LocalDateTime now = LocalDateTime.now();
        long minutesUntilEnd = Duration.between(now, booking.getEndTime()).toMinutes();

        // if the time is 5 mins or fewer, show the reminder
        if (minutesUntilEnd <= 5 && minutesUntilEnd >= 0) {
            Span warning = new Span("Reminder: Your booking is ending soon!");
            warning.getStyle().set("margin-top", "25px");

            topText.add(warning);
        }

        // finish booking button to end meeting manually
        Button finishBtn = new Button("Finish Booking");
        finishBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        finishBtn.setWidthFull();
        finishBtn.setHeight("50px");
        finishBtn.addClickListener(e -> onFinish.run());

        add(topText, finishBtn);
    }
}