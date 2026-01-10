package com.gesamtprojekt.application.ui.components.admin;

import com.gesamtprojekt.application.service.implementation.MeetingRoomService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;

public class RoomManagementStatsBar extends HorizontalLayout {

    private final MeetingRoomService meetingRoomService;

    private final Span totalRoomsValue = new Span();
    private final Span totalCapacityValue = new Span();
    private final Span buildingsValue = new Span();

    public RoomManagementStatsBar(MeetingRoomService meetingRoomService) {
        this.meetingRoomService = meetingRoomService;

        setWidthFull();
        setSpacing(true);
        addClassName("room-stats-bar");


        add(
                card("Total Rooms", totalRoomsValue, VaadinIcon.HOME),
                card("Total Capacity", totalCapacityValue, VaadinIcon.GROUP),
                card("Buildings", buildingsValue, VaadinIcon.OFFICE)
        );

        // Erstes mal laden
        refresh();
    }

    // Cards befüllen
    public void refresh() {
        totalRoomsValue.setText(String.valueOf(meetingRoomService.countRooms()));
        totalCapacityValue.setText(String.valueOf(meetingRoomService.sumCapacity()));
        buildingsValue.setText(String.valueOf(meetingRoomService.countBuildings()));
    }

    private Component card(String title, Span value, VaadinIcon icon) {
        var c = new VerticalLayout();
        c.addClassNames(
                LumoUtility.Background.BASE,
                LumoUtility.BorderRadius.MEDIUM,
                LumoUtility.BoxShadow.SMALL,
                LumoUtility.Padding.MEDIUM
        );


        var header = new HorizontalLayout(new Span(title), icon.create());
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.setWidthFull();
        header.addClassName(LumoUtility.TextColor.SECONDARY);


        var val = new Span(value);
        val.addClassNames(LumoUtility.FontSize.XXLARGE, LumoUtility.FontWeight.BOLD);

        c.add(header, val);
        return c;
    }
}
