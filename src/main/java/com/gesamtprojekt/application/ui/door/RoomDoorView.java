package com.gesamtprojekt.application.ui.door;

import com.gesamtprojekt.application.repositories.MeetingRoomRepository;
import com.gesamtprojekt.application.service.implementation.BookingService;
import com.gesamtprojekt.application.ui.room.components.RoomHeader;
import com.gesamtprojekt.application.ui.room.screens.RoomDefaultScreen;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Pre;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("door/:roomName")
@PageTitle("Door Display")
@AnonymousAllowed
public class RoomDoorView extends VerticalLayout implements BeforeEnterObserver {

    private final BookingService bookingService;
    private final MeetingRoomRepository meetingRoomRepository;

    private String roomName;

    public RoomDoorView(BookingService bookingService, MeetingRoomRepository meetingRoomRepository) {
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

        // DEBUG: was sieht die App?
        long cnt = meetingRoomRepository.count();
        var sample = meetingRoomRepository.findAll().stream().limit(10)
                .map(r -> "id=" + r.getRoomId() + " name=[" + r.getName() + "] active=" + r.getIsActive())
                .toList();

        var debug = new StringBuilder();
        debug.append("param roomName=[").append(roomName).append("]\n");
        debug.append("meetingRoomRepository.count()=").append(cnt).append("\n");
        debug.append("first rows:\n");
        sample.forEach(s -> debug.append("  ").append(s).append("\n"));
        debug.append("findActiveByExactName=").append(meetingRoomRepository.findActiveByExactName(roomName).isPresent()).append("\n");

        // Wenn du das Debug später nicht mehr willst, einfach entfernen.
        add(new Pre(debug.toString()));

        var roomOpt = meetingRoomRepository.findActiveByExactName(roomName);
        if (roomOpt.isEmpty()) {
            add(new H1("Room not found: " + roomName));
            return;
        }

        var room = roomOpt.get();
        var bookings = bookingService.getBookingsForDoorDisplayByRoomName(room.getName());

        add(
                new RoomHeader(),
                new RoomDefaultScreen(room.getName(), bookings)
        );
    }
}
