package com.gesamtprojekt.application.ui.room.components;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.gesamtprojekt.application.security.SecurityService;

public class RoomFooter extends HorizontalLayout {

    public RoomFooter(SecurityService securityService) {
        setWidthFull();
        setPadding(false);
        getStyle().set("padding", "var(--lumo-space-m)");

        // Logout Button erstellen
        Button logoutBtn = new Button("", VaadinIcon.SIGN_OUT.create());
        logoutBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);

        // Die Logik aus deinem SecurityService nutzen
        logoutBtn.addClickListener(e -> securityService.logout());

        // Layout für die Positionierung links unten
        HorizontalLayout layout = new HorizontalLayout(logoutBtn);
        layout.setWidthFull();
        layout.setJustifyContentMode(HorizontalLayout.JustifyContentMode.START);

        add(layout);
    }
}
