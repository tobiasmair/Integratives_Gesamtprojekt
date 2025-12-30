package com.gesamtprojekt.application.ui.admin.statistics;

import com.gesamtprojekt.application.ui.client.MainLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "statistics", layout = MainLayout.class)
@PageTitle("Statistics")
@RolesAllowed("ADMIN")
public class StatisticsView extends VerticalLayout {

    public StatisticsView() {

        add(new H1("Statistics"));
getStyle().setHeight("100%");
    }
}
