package com.gesamtprojekt.application.ui.components.calendar;

import com.gesamtprojekt.application.model.MeetingRoom;
import com.gesamtprojekt.application.service.implementation.MeetingRoomService;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.time.LocalDateTime;
import java.util.List;

public class CalendarRoomsSection extends VerticalLayout {

    private final MeetingRoomService meetingRoomService;
    private final FlexLayout grid = new FlexLayout();

    public CalendarRoomsSection(MeetingRoomService meetingRoomService) {
        this.meetingRoomService = meetingRoomService;

        setWidthFull();
        setPadding(false);
        setSpacing(true);

        add(new H4("available Meeting Rooms"));
        add(buildGrid());

        //reload();
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

    public void reload(LocalDateTime start, LocalDateTime end, String b, String f, String cap) {
        grid.removeAll();

        if (end.isBefore(start) || end.isEqual(start)) {
            Notification.show("End time must be after start time.", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            grid.removeAll();
            return;
        }

        // Abfrage DB
        List<MeetingRoom> rooms = meetingRoomService.findCalendarRooms(start, end, b, f, cap);

        if (rooms.isEmpty()) {
            grid.add(new Span("No rooms available for the selected filters."));
        } else {
            rooms.forEach(r -> grid.add(buildCard(mapToCardModel(r))));
        }
    }

    private List<MeetingRoom> loadRooms() {
        return meetingRoomService.findAllRooms("", "All Buildings", "ACTIVE");
    }

    private CalendarRoomCard buildCard(CalendarRoomCardModel r) {
        CalendarRoomCard card = new CalendarRoomCard(r);
        card.getStyle().set("width", "260px");
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
                .limit(5)
                .toList();
    }
}
