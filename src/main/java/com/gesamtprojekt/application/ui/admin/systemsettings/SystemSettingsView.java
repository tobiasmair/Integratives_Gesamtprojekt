package com.gesamtprojekt.application.ui.admin.systemsettings;

import com.gesamtprojekt.application.ui.client.MainLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "systemsettings", layout = MainLayout.class)
@PageTitle("System Settings")
@RolesAllowed("ADMIN")
public class SystemSettingsView extends VerticalLayout {

    public SystemSettingsView() {

        add(new H1("System Settings"));
getStyle().setHeight("100%");
    }
}
