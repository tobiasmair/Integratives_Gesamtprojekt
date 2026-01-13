package com.gesamtprojekt.application.ui.client.calendar;

import com.gesamtprojekt.application.service.implementation.MeetingRoomService;
import com.gesamtprojekt.application.ui.client.MainLayout;
import com.gesamtprojekt.application.ui.components.calendar.CalendarControlsBar;
import com.gesamtprojekt.application.ui.components.calendar.CalendarRoomsSection;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "calendar", layout = MainLayout.class)
@PageTitle("Calendar")
@RolesAllowed({"CLIENT", "ADMIN"})
public class CalendarView extends VerticalLayout {

    public CalendarView(MeetingRoomService meetingRoomService) {
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        CalendarControlsBar controls = new CalendarControlsBar();
        CalendarRoomsSection rooms = new CalendarRoomsSection(meetingRoomService);

        // Initiales laden der Daten
        rooms.reload(
                controls.getStartDateTime(),
                controls.getEndDateTime(),
                controls.getBuilding(),
                controls.getFloor(),
                controls.getCapacity()
        );

        controls.addFilterChangedListener(e -> {
            rooms.reload(
                    controls.getStartDateTime(),
                    controls.getEndDateTime(),
                    controls.getBuilding(),
                    controls.getFloor(),
                    controls.getCapacity()
            );
        });

        rooms.addClassName("calendar-rooms-scroll");

        add(controls, rooms);
        setFlexGrow(1, rooms);
    }
}
