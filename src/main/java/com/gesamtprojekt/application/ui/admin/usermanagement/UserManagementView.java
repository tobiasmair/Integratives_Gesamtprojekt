package com.gesamtprojekt.application.ui.admin.usermanagement;

import com.gesamtprojekt.application.service.implementation.BookingService;
import com.gesamtprojekt.application.service.implementation.ClientService;
import com.gesamtprojekt.application.ui.client.MainLayout;
import com.gesamtprojekt.application.ui.components.admin.UserManagementStatsBar;
import com.gesamtprojekt.application.ui.components.admin.UserTableSection;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "usermanagement", layout = MainLayout.class)
@PageTitle("User Management")
@RolesAllowed("ADMIN")
public class UserManagementView extends VerticalLayout {

    public UserManagementView(ClientService clientService, BookingService bookingService) {
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // Komponente hinzufügen
        UserManagementStatsBar statsBar = new UserManagementStatsBar(clientService, bookingService);
        UserTableSection tableSection = new UserTableSection(clientService, bookingService);

        // Listener registrieren
        tableSection.addStatsChangedListener(event -> statsBar.refresh());

        // Aufbau View
        add(statsBar, tableSection);
        setFlexGrow(1, tableSection);
    }
}
