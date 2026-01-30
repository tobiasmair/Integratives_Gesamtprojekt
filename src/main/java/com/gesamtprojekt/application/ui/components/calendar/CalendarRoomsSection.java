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
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
        // Öffnungszeiten
        LocalTime opensAt = LocalTime.of(7, 0);
        LocalTime closesAt = LocalTime.of(22, 0);
        
        grid.removeAll();

        // Browse Mode: Nur Filter, kein Datum
        if (start == null || end == null) {
            reloadBrowseMode(b, f, cap, equip);
            return;
        }

        // Calendar Mode: Mit Datum und Verfügbarkeit
        if (end.isBefore(start) || end.isEqual(start)) {
            Notification.show("End time must be after start time", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            grid.removeAll();
            return;
        }

        // Nicht in der Vergangenheit
        if (start.isBefore(LocalDateTime.now())) {
            Notification.show("Start time must be in the future", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            grid.removeAll();
            return;
        }

        // Öffnungszeiten prüfen
        if (start.toLocalTime().isBefore(opensAt) || end.toLocalTime().isAfter(closesAt)) {
            Notification.show("The building is closed! (07:00 - 22:00)", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            grid.removeAll();
            return;
        }

        // Abfrage DB
        List<MeetingRoom> rooms = meetingRoomService.findCalendarRooms(start, end, b, f, cap, equip);

        // Sortiere Räume nach Gebäude und dann nach Raumnummer
        rooms = sortRooms(rooms);

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

        // Sortiere Räume nach Gebäude und dann nach Raumnummer
        rooms = sortRooms(rooms);

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

    /**
     * Sortiert Räume nach Gebäude (MCI I -> V) und dann nach Raumnummer (aufsteigend).
     */
    private List<MeetingRoom> sortRooms(List<MeetingRoom> rooms) {
        return rooms.stream()
                .sorted(Comparator
                        .comparing((MeetingRoom r) -> getBuildingOrder(r.getLocation()))
                        .thenComparing((MeetingRoom r) -> extractRoomNumber(r.getName())))
                .collect(Collectors.toList());
    }

    /**
     * Gibt Sortier-Reihenfolge für Gebäude zurück.
     * MCI I = 1, MCI II = 2, ..., MCI V = 5, andere = 999
     */
    private int getBuildingOrder(String building) {
        if (building == null) return 999;
        return switch (building) {
            case "MCI I" -> 1;
            case "MCI II" -> 2;
            case "MCI III" -> 3;
            case "MCI IV" -> 4;
            case "MCI V" -> 5;
            default -> 999;
        };
    }

    /**
     * Extrahiert Raumnummer aus dem Namen für Sortierung.
     * Versucht die erste Zahl zu extrahieren, sonst lexikalische Sortierung.
     */
    private String extractRoomNumber(String roomName) {
        if (roomName == null) return "";

        // Extrahiere führende Zahlen für numerische Sortierung
        String numberPart = roomName.replaceAll("\\D.*", ""); // Nur führende Ziffern
        if (!numberPart.isEmpty()) {
            // Padding für korrekte numerische Sortierung (z.B. "2" -> "0002", "234" -> "0234")
            return String.format("%04d", Integer.parseInt(numberPart)) + roomName;
        }

        // Fallback: lexikalische Sortierung
        return roomName.toLowerCase();
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
