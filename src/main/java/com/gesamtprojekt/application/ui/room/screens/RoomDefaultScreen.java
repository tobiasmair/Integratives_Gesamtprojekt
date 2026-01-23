package com.gesamtprojekt.application.ui.room.screens;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.gesamtprojekt.application.model.Booking;
import com.vaadin.flow.router.PageTitle;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;


/**
 * Shows the state of an unbooked room and lists upcoming bookings for that room
 */
public class RoomDefaultScreen extends VerticalLayout {

    public RoomDefaultScreen(String roomName, List<Booking> bookings) {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setSpacing(false);

        // show room name
        H1 title = new H1(roomName);
        title.getStyle()
                .set("margin-bottom", "1.5em")
                .set("font-size", "3em");

        // show today's bookings that are in the future
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = LocalDate.now();

        List<Booking> upcomingToday = bookings.stream()
                .filter(Booking::getIsActive)
                .filter(b -> b.getStartTime().toLocalDate().equals(today))
                .filter(b -> b.getStartTime().isAfter(now))
                .sorted(Comparator.comparing(Booking::getStartTime))
                .toList();

        VerticalLayout scheduleLayout = new VerticalLayout();
        scheduleLayout.setAlignItems(Alignment.CENTER);
        scheduleLayout.setPadding(false);

        // if no more bookings today
        if (upcomingToday.isEmpty()) {
            H2 noBookings = new H2("No upcoming bookings today");
            noBookings.getStyle().set("color", "var(--lumo-secondary-text-color)");
            scheduleLayout.add(noBookings);
        } else {
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

            // heading for bookings list
            Span header = new Span("Upcoming today:");
            header.getStyle().set("color", "var(--lumo-secondary-text-color)")
                    .set("margin-bottom", "10px")
                    .set("text-transform", "uppercase")
                    .set("letter-spacing", "1px");
            scheduleLayout.add(header);

            // list next 3 bookings
            int displayCount = Math.min(upcomingToday.size(), 3);
            for (int i = 0; i < displayCount; i++) {
                Booking b = upcomingToday.get(i);
                String timeRange = b.getStartTime().format(timeFormatter) + " - " + b.getEndTime().format(timeFormatter);
                H2 timeLabel = new H2(timeRange);
                timeLabel.getStyle().set("margin", "5px 0");
                scheduleLayout.add(timeLabel);
            }

            // if more than 3 upcoming bookings, show how many more and finish time from last
            if (upcomingToday.size() > 3) {
                int moreCount = upcomingToday.size() - 3;
                LocalDateTime lastEndTime = upcomingToday.get(upcomingToday.size() - 1).getEndTime();

                Span moreInfo = new Span(String.format("and %d more - last booking ends at %s",
                        moreCount, lastEndTime.format(timeFormatter)));
                moreInfo.getStyle().set("color", "var(--lumo-primary-color)")
                        .set("font-weight", "600")
                        .set("margin-top", "15px");
                scheduleLayout.add(moreInfo);
            }
        }

        add(title, scheduleLayout);
    }
}