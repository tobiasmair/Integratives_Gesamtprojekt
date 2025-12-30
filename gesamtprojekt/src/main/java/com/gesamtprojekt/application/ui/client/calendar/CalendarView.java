package com.gesamtprojekt.application.ui.client.calendar;

import com.gesamtprojekt.application.ui.client.MainLayout;
import com.gesamtprojekt.application.ui.components.calendar.CalendarControlsBar;
import com.gesamtprojekt.application.ui.components.calendar.CalendarRoomsSection;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route(value = "calendar", layout = MainLayout.class)
@PageTitle("Calendar")
@AnonymousAllowed
public class CalendarView extends VerticalLayout {

    public CalendarView() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        CalendarControlsBar controls = new CalendarControlsBar();
        CalendarRoomsSection rooms = new CalendarRoomsSection();

        rooms.addClassName("calendar-rooms-scroll");

        add(controls, rooms);
        setFlexGrow(1, rooms);
    }
}
