package com.gesamtprojekt.application.ui.components.admin;

import com.gesamtprojekt.application.service.implementation.BookingService;
import com.gesamtprojekt.application.service.implementation.ClientService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;

public class UserManagementStatsBar extends HorizontalLayout {

    private final ClientService clientService;
    private final BookingService bookingService;

    private final Span totalUsersValue = new Span();
    private final Span totalLecturersValue = new Span();
    private final Span totalStudentsValue = new Span();
    private final Span activeBookingsValue = new Span();

    public UserManagementStatsBar(ClientService clientService, BookingService bookingService) {

        this.clientService = clientService;
        this.bookingService = bookingService;

        setWidthFull();
        setSpacing(true);
        addClassName("user-stats-bar");

        add(
                createStatCard("Total Users", totalUsersValue, VaadinIcon.USERS),
                createStatCard("Total Lecturers", totalLecturersValue, VaadinIcon.ACADEMY_CAP),
                createStatCard("Total Students", totalStudentsValue, VaadinIcon.CHAT),
                createStatCard("Active bookings", activeBookingsValue, VaadinIcon.CALENDAR)
        );

        refresh();
    }

    // Cards befüllen
    public void refresh() {
        totalUsersValue.setText(String.valueOf(clientService.countUsers()));
        totalLecturersValue.setText(String.valueOf(clientService.countByUserTypeAndIsActiveTrue("LECTURER")));
        totalStudentsValue.setText(String.valueOf(clientService.countByUserTypeAndIsActiveTrue("STUDENT")));
        activeBookingsValue.setText(String.valueOf(bookingService.countActiveBookings()));
    }

    private Component createStatCard(String title, Span value, VaadinIcon icon) {
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
