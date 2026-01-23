package com.gesamtprojekt.application.ui.door;

import com.gesamtprojekt.application.model.Booking;
import com.gesamtprojekt.application.repositories.MeetingRoomRepository;
import com.gesamtprojekt.application.service.implementation.BookingService;
import com.gesamtprojekt.application.ui.room.components.RoomHeader;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Route("door/:roomName")
@PageTitle("Door Display")
@AnonymousAllowed
public class RoomDoorView extends VerticalLayout implements BeforeEnterObserver {

    private final BookingService bookingService;
    private final MeetingRoomRepository meetingRoomRepository;

    private String roomName;

    public RoomDoorView(
            BookingService bookingService,
            MeetingRoomRepository meetingRoomRepository
    ) {
        this.bookingService = bookingService;
        this.meetingRoomRepository = meetingRoomRepository;

        setSizeFull();
        setPadding(false);
        setSpacing(false);

        UI.getCurrent().setPollInterval(30_000);
        UI.getCurrent().addPollListener(e -> render());
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        roomName = event.getRouteParameters().get("roomName").orElse(null);
        render();
    }

    private void render() {
        removeAll();

        if (roomName == null || roomName.isBlank()) {
            add(new H1("Missing room name"));
            return;
        }

        var roomOpt = meetingRoomRepository.findActiveByExactName(roomName);
        if (roomOpt.isEmpty()) {
            add(new H1("Room not found: " + roomName));
            return;
        }

        var room = roomOpt.get();

        List<Booking> bookings = bookingService.getBookingsForDoorDisplayByRoomName(room.getName());

        add(new RoomHeader());

        LocalDateTime now = LocalDateTime.now();

        Booking current = null;
        List<Booking> upcoming = new ArrayList<>();

        for (Booking b : bookings) {
            LocalDateTime start = b.getStartTime();
            LocalDateTime end = b.getEndTime();

            if (start != null && end != null && !now.isBefore(start) && now.isBefore(end)) {
                if (current == null || end.isBefore(current.getEndTime())) {
                    current = b;
                }
            } else if (start != null && now.isBefore(start)) {
                upcoming.add(b);
            }
        }

        if (current != null) {
            add(new DoorOccupiedScreen(room.getName(), current, upcoming));
        } else {
            add(new DoorUpcomingScreen(room.getName(), upcoming));
        }
    }
}
