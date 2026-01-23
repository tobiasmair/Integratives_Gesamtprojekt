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
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Dashboard | MCI Meeting Booker")
//@RolesAllowed({"USER", "ADMIN"})
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

        HorizontalLayout layout = new HorizontalLayout(quick, bookings);
        layout.setWidthFull();

        // RESPONSIVE
        layout.addClassNames(
                LumoUtility.FlexWrap.WRAP,
                LumoUtility.Display.FLEX
        );

        // Auf großen Bildschirm nebeneinander, ansonsten untereinander
        quick.getStyle().set("flex", "1 1 400px");
        bookings.getStyle().set("flex", "1 1 400px");

        layout.setPadding(true);
        layout.setSpacing(true);

        return layout;
    }
}



