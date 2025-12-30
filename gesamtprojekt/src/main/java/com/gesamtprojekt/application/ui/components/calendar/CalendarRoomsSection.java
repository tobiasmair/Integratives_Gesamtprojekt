package com.gesamtprojekt.application.ui.components.calendar;

import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class CalendarRoomsSection extends VerticalLayout {

    public CalendarRoomsSection() {
        setWidthFull();
        setPadding(false);
        setSpacing(true);

        add(new H4("available Meeting Rooms"));
        add(buildGrid());
    }


    private FlexLayout buildGrid() {
        FlexLayout grid = new FlexLayout();
        grid.setWidthFull();
        grid.getStyle().set("gap", "12px");
        grid.getStyle().set("flex-wrap", "wrap");

        CalendarDummyRooms.rooms().forEach(r -> grid.add(buildCard(r)));
        return grid;
    }

    private CalendarRoomCard buildCard(CalendarDummyRooms.Room r) {
        CalendarRoomCard card = new CalendarRoomCard(r);
        card.getStyle().set("width", "260px");
        return card;
    }
}
