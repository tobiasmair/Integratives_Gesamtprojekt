package com.gesamtprojekt.application.ui.components.admin;

import com.gesamtprojekt.application.service.implementation.ClientService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;

public class UserManagementStatsBar extends HorizontalLayout {

    public UserManagementStatsBar(ClientService clientService) {
        setWidthFull();
        setSpacing(true);
        addClassName("user-stats-bar");

        add(
                createStatCard("Total Users", String.valueOf(clientService.countUsers()), VaadinIcon.USERS),
                createStatCard("Total Lecturers", "DUMMY", VaadinIcon.ACADEMY_CAP),
                createStatCard("Total Students", "DUMMY", VaadinIcon.CHAT),
                createStatCard("Active bookings", "DUMMY", VaadinIcon.CALENDAR)
        );
    }

    private Component createStatCard(String title, String value, VaadinIcon icon) {
        VerticalLayout card = new VerticalLayout();
        card.addClassNames(
                LumoUtility.Background.BASE,
                LumoUtility.BorderRadius.MEDIUM,
                LumoUtility.BoxShadow.SMALL,
                LumoUtility.Padding.MEDIUM
        );

        HorizontalLayout header = new HorizontalLayout(new Span(title), icon.create());
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.setWidthFull();
        header.addClassName(LumoUtility.TextColor.SECONDARY);

        Span val = new Span(value);
        val.addClassNames(LumoUtility.FontSize.XXLARGE, LumoUtility.FontWeight.BOLD);

        card.add(header, val);
        return card;
    }

}
