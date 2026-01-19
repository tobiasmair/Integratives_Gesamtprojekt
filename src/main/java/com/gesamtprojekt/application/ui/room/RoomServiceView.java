package com.gesamtprojekt.application.ui.room;

import com.gesamtprojekt.application.security.SecurityService;
import com.gesamtprojekt.application.ui.room.components.ButtonContainer;
import com.gesamtprojekt.application.ui.room.components.RoomFooter;
import com.gesamtprojekt.application.ui.room.components.RoomHeader;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "roomservice", layout = RoomLayout.class)
@PageTitle("Room Service")
@RolesAllowed("ROOM")
public class RoomServiceView extends VerticalLayout {

    public RoomServiceView(SecurityService securityService) {

        RoomHeader header = new RoomHeader();

        ButtonContainer controlButton = new ButtonContainer();
        controlButton.addButton("Light", VaadinIcon.LIGHTBULB);
        controlButton.addButton("AC", VaadinIcon.LIGHTBULB);
        controlButton.addButton("Beamer", VaadinIcon.LIGHTBULB);

        RoomFooter footer = new RoomFooter(securityService);

        // content layout
        VerticalLayout content = new VerticalLayout(controlButton);
        content.setSizeFull();
        content.setJustifyContentMode(JustifyContentMode.CENTER);
        content.setAlignItems(Alignment.CENTER);

        add(header, content, footer);

        setSizeFull();
        setPadding(false);
        setSpacing(false);
    }
}
