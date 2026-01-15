package com.gesamtprojekt.application.ui.client.dashboard;

import com.gesamtprojekt.application.model.Client;
import com.gesamtprojekt.application.security.SecurityService;
import com.gesamtprojekt.application.service.implementation.BookingService;
import com.gesamtprojekt.application.service.implementation.MeetingRoomService;
import com.gesamtprojekt.application.ui.client.MainLayout;
import com.gesamtprojekt.application.ui.components.dashboard.MyBookingsContainer;
import com.gesamtprojekt.application.ui.components.dashboard.QuickBookingContainer;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Dashboard")
//@RolesAllowed({"CLIENT", "ADMIN"})
@PermitAll
public class DashboardView extends VerticalLayout implements BeforeEnterObserver {

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

    // Weiterleitung für ROOM Nutzer
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Client client = securityService.getAuthenticatedClient().orElseThrow();
        if ("ROOM".equals(client.getRole())) {
            event.forwardTo("roomservice");
        }
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



