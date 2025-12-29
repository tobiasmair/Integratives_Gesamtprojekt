package com.gesamtprojekt.application.ui.client.browserooms;

import com.gesamtprojekt.application.ui.client.MainLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import jakarta.annotation.security.PermitAll;

@Route(value = "browserooms", layout = MainLayout.class)
@PageTitle("Browse Rooms")
@PermitAll
public class BrowseRoomsView extends VerticalLayout {

    public BrowseRoomsView() {

        add(new H1("Browse Rooms"));
getStyle().setHeight("100%");
    }
}
