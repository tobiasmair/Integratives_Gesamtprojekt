package com.gesamtprojekt.application.ui.client.dashboard;

import com.gesamtprojekt.application.ui.client.MainLayout;
import com.gesamtprojekt.application.ui.components.dashboard.MyBookingsContainer;
import com.gesamtprojekt.application.ui.components.dashboard.QuickBookingContainer;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

//@RolesAllowed({"USER", "ADMIN"})
@Route(value = "dashboard", layout = MainLayout.class)
@PageTitle("Dashboard")
@AnonymousAllowed

public class DashboardView extends VerticalLayout {

    public DashboardView() {
        addClassName("dashboard-view");
        setSizeFull();

        //add(new H1("Dashboard!!!"));
        add(createTwoColumnLayout());
    }

    private HorizontalLayout createTwoColumnLayout() {
        var quick = new QuickBookingContainer();
        var bookings = new MyBookingsContainer();


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
