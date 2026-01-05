package com.gesamtprojekt.application.ui.components.admin;

import com.gesamtprojekt.application.service.implementation.MeetingRoomService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;

public class RoomManagementStatsBar extends HorizontalLayout {

    public RoomManagementStatsBar(MeetingRoomService service) {

        setWidthFull();
        setSpacing(true);
        addClassName("room-stats-bar");


        add(
                card("Total Rooms", String.valueOf(service.countRooms()), VaadinIcon.HOME),
                card("Total Capacity", String.valueOf(service.sumCapacity()), VaadinIcon.GROUP),
                card("Buildings", String.valueOf(service.countBuildings()), VaadinIcon.OFFICE)
        );
    }

    private Component card(String title, String value, VaadinIcon icon) {
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
