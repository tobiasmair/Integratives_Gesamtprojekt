package com.gesamtprojekt.application.ui.client.dashboard;

import com.gesamtprojekt.application.security.SecurityService;
import com.gesamtprojekt.application.service.implementation.BookingService;
import com.gesamtprojekt.application.service.implementation.MeetingRoomService;
import com.gesamtprojekt.application.ui.client.MainLayout;
import com.gesamtprojekt.application.ui.components.dashboard.MyBookingsContainer;
import com.gesamtprojekt.application.ui.components.dashboard.QuickBookingContainer;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Dashboard")
@PermitAll
public class DashboardView extends VerticalLayout {

    private final BookingService bookingService;
    private final MeetingRoomService meetingRoomService;
    private final SecurityService securityService;

    public DashboardView(BookingService bookingService, MeetingRoomService meetingRoomService, SecurityService securityService) {
        this.bookingService = bookingService;
        this.meetingRoomService = meetingRoomService;
        this.securityService = securityService;

        addClassName("dashboard-view");
        setSizeFull();

        add(createTwoColumnLayout());
    }

    private HorizontalLayout createTwoColumnLayout() {
        var quick = new QuickBookingContainer(bookingService, meetingRoomService, securityService);
        var bookings = new MyBookingsContainer(bookingService, meetingRoomService, securityService);

        // Listener registrieren
        quick.addBookingChangedListener(event -> bookings.refresh());
        bookings.addBookingChangedListener(event -> quick.loadRooms());

        quick.setWidthFull();
        bookings.setWidthFull();

        var layout = new HorizontalLayout(quick, bookings);
        layout.setWidthFull();
        layout.setHeightFull();
        layout.setAlignItems(Alignment.STRETCH);
        layout.setPadding(false);
        layout.setSpacing(true);
        layout.setFlexGrow(1, quick);
        layout.setFlexGrow(1, bookings);

        return layout;
    }
}



