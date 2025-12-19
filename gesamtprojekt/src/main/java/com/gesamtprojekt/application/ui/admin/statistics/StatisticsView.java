package com.gesamtprojekt.application.ui.admin.statistics;

import com.gesamtprojekt.application.ui.MainLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route(value = "statistics", layout = MainLayout.class)
@PageTitle("Statistics")
@AnonymousAllowed
public class StatisticsView extends VerticalLayout {

    public StatisticsView() {

        add(new H1("Statistics"));
getStyle().setHeight("100%");
    }
}
