package com.gesamtprojekt.application.ui.room;

import com.gesamtprojekt.application.ui.client.MainLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "roomservice", layout = MainLayout.class)
@PageTitle("Room Service")
@RolesAllowed("ROOM")
public class RoomServiceView extends VerticalLayout {

    public RoomServiceView() {
        setSizeFull();

        H1 title = new H1("Room Service Dashboard");

        add(title);
    }
}
