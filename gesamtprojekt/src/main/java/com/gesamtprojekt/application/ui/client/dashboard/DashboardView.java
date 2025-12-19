package com.gesamtprojekt.application.ui.client.dashboard;

import com.gesamtprojekt.application.ui.client.MainLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route(value = "dashboard", layout = MainLayout.class)
@PageTitle("Dashboard")
@AnonymousAllowed
public class DashboardView extends VerticalLayout {

    public DashboardView() {

        add(new H1("Dashboard"));
    }
}
