package com.gesamtprojekt.application.ui.browserooms;

import com.gesamtprojekt.application.ui.MainLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route(value = "browserooms", layout = MainLayout.class)
@PageTitle("Browse Rooms")
@AnonymousAllowed
public class BrowseRoomsView extends VerticalLayout {

    public BrowseRoomsView() {

        add(new H1("Browse Rooms"));
getStyle().setHeight("100%");
    }
}
