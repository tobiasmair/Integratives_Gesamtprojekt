package com.gesamtprojekt.application.ui.room.screens;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;

/**
 * Is displayed one minute before and 5 minutes past the booking's startTime to prompt the user for the check-in code
 */
public class RoomLockScreen extends VerticalLayout {
    public RoomLockScreen(Runnable onCheckInClick) {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        H1 locked = new H1("Room is booked");
        H2 info = new H2("Please enter the Booking Code");

        Button enterCodeBtn = new Button("Check-in", VaadinIcon.KEY.create());
        enterCodeBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        enterCodeBtn.getStyle().set("width", "250px").set("height", "80px");
        enterCodeBtn.addClickListener(e -> onCheckInClick.run());

        add(locked, info, enterCodeBtn);
    }
}
