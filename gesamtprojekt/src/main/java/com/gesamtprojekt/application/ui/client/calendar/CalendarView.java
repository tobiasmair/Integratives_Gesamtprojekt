package com.gesamtprojekt.application.ui.client.calendar;

import com.gesamtprojekt.application.ui.client.MainLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import jakarta.annotation.security.PermitAll;

@Route(value = "calendar", layout = MainLayout.class)
@PageTitle("Calendar")
@PermitAll
public class CalendarView extends VerticalLayout {

    public CalendarView() {

        add(new H1("Calendar"));
    }
}
