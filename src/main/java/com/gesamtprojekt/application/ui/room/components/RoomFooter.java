package com.gesamtprojekt.application.ui.room.components;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.gesamtprojekt.application.security.SecurityService;

/**
 * Creates footer with improved logout-button
 */
public class RoomFooter extends HorizontalLayout {

    public RoomFooter(SecurityService securityService) {
        setWidthFull();
        setPadding(false);
        getStyle().set("padding", "var(--lumo-space-m)");

        // logout button
        Button logoutBtn = new Button("", VaadinIcon.SIGN_OUT.create());
        logoutBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);

        // click opens the confirmation-dialog
        logoutBtn.addClickListener(e -> {
            ConfirmLogout dialog = new ConfirmLogout(
                    securityService::checkPassword,
                    securityService::logout
            );
            dialog.open();
        });

        HorizontalLayout layout = new HorizontalLayout(logoutBtn);
        layout.setWidthFull();
        layout.setJustifyContentMode(HorizontalLayout.JustifyContentMode.START);

        add(layout);
    }
}
