package com.gesamtprojekt.application.ui.components.calendar;

import com.gesamtprojekt.application.model.MeetingRoom;
import com.gesamtprojekt.application.security.SecurityService;
import com.gesamtprojekt.application.service.implementation.BookingService;
import com.gesamtprojekt.application.service.implementation.MeetingRoomService;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.shared.Registration;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public class CalendarRoomsSection extends VerticalLayout {

    private final MeetingRoomService meetingRoomService;
    private final BookingService bookingService;
    private final SecurityService securityService;
    private final FlexLayout grid = new FlexLayout();
    private final H4 heading = new H4("Available Meeting Rooms");

    public CalendarRoomsSection(MeetingRoomService meetingRoomService, BookingService bookingService,
                                SecurityService securityService) {
        this.meetingRoomService = meetingRoomService;
        this.bookingService = bookingService;
        this.securityService = securityService;

        setWidthFull();
        setPadding(false);
        setSpacing(true);

        add(heading);
        add(buildGrid());

        //reload();
    }

    public void setHeading(String text) {
        heading.setText(text);
    }

    private FlexLayout buildGrid() {
        grid.setWidthFull();
        grid.getStyle().set("gap", "12px");
        grid.getStyle().set("flex-wrap", "wrap");
        return grid;
    }

    /*
    public void reload() {
        grid.removeAll();
        loadRooms().forEach(r -> grid.add(buildCard(mapToCardModel(r))));
    }
     */

    public void reload(LocalDateTime start, LocalDateTime end, String b, String f, String cap, Set<String> equip) {
        grid.removeAll();

        // Browse Mode: Nur Filter, kein Datum
        if (start == null || end == null) {
            reloadBrowseMode(b, f, cap, equip);
            return;
        }

        // Calendar Mode: Mit Datum und Verfügbarkeit
        if (end.isBefore(start) || end.isEqual(start)) {
            Notification.show("End time must be after start time.", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            grid.removeAll();
            return;
        }

        // Abfrage DB
        List<MeetingRoom> rooms = meetingRoomService.findCalendarRooms(start, end, b, f, cap, equip);

        if (rooms.isEmpty()) {
            grid.add(new Span("No rooms available for the selected filters."));
        } else {
            rooms.forEach(r -> {
                CalendarRoomCard card = buildCard(mapToCardModel(r));
                card.setFilterDateTime(start, end);
                grid.add(card);
            });
        }
    }

    private void reloadBrowseMode(String b, String f, String cap, Set<String> equip) {
        grid.removeAll();

        // Abfrage DB: Alle Räume ohne Verfügbarkeitsprüfung
        List<MeetingRoom> rooms = meetingRoomService.findAllRoomsByFilters(b, f, cap, equip);

        if (rooms.isEmpty()) {
            grid.add(new Span("No rooms found for the selected filters."));
        } else {
            rooms.forEach(r -> {
                CalendarRoomCard card = buildCard(mapToCardModel(r));
                // Im Browse Mode keine Datum-Filterung
                card.setFilterDateTime(null, null);
                grid.add(card);
            });
        }
    }

    private List<MeetingRoom> loadRooms() {
        return meetingRoomService.findAllRooms("", "All Buildings", "ACTIVE");
    }

    private CalendarRoomCard buildCard(CalendarRoomCardModel r) {
        CalendarRoomCard card = new CalendarRoomCard(r, bookingService, meetingRoomService, securityService);
        card.getStyle().set("width", "260px");

        // Event-Listener für Buchungserstellung
        card.addBookingCreatedListener(event -> {
            System.out.println("DEBUG: BookingCreatedEvent received in CalendarRoomsSection from card: " + r.name());
            fireEvent(new BookingCreatedInSectionEvent(this));
        });

        return card;
    }

    private CalendarRoomCardModel mapToCardModel(MeetingRoom r) {
        return new CalendarRoomCardModel(
                r.getRoomId(),
                r.getName(),
                r.getLocation(),
                r.getCapacity(),
                r.getFloor(),
                tagsFromRoom(r),
                r.getImagePath()
        );
    }

    private List<String> tagsFromRoom(MeetingRoom r) {
        if (r.getEquipment() == null || r.getEquipment().isEmpty()) return List.of();

        return r.getEquipment().stream()
                .map(e -> e.getDescription() == null ? "" : e.getDescription().trim())
                .filter(s -> !s.isBlank())
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    public static class BookingCreatedInSectionEvent extends ComponentEvent<CalendarRoomsSection> {
        public BookingCreatedInSectionEvent(CalendarRoomsSection source) {
            super(source, false);
        }
    }

    public Registration addBookingCreatedListener(ComponentEventListener<BookingCreatedInSectionEvent> listener) {
        return addListener(BookingCreatedInSectionEvent.class, listener);
    }
}
