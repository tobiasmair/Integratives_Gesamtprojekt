package com.gesamtprojekt.application.ui.admin.roommanagement;

import com.gesamtprojekt.application.ui.client.MainLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route(value = "roommanagement", layout = MainLayout.class)
@PageTitle("Room Management")
@AnonymousAllowed //später entfernen
public class RoomManagementView extends VerticalLayout {

    public RoomManagementView() {

        add(new H1("Room Management"));
    }
}
