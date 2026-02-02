package com.gesamtprojekt.application.ui.room.components;

import com.gesamtprojekt.application.model.MeetingRoom;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * Container for Room-control buttons: (Door, Light, Blinds, Whiteboard, Beamer and AC).
 * Displays the buttons in a 3 x 2 pattern
 */
public class ButtonContainer extends VerticalLayout {

    private final MeetingRoom room;

    public ButtonContainer(MeetingRoom room) {
        this.room = room;

        setSpacing(true);
        setPadding(false);
        setWidth("500px");
        setAlignItems(Alignment.CENTER);

        // CSS grid for 3 column pattern
        getStyle().set("display", "grid");
        getStyle().set("grid-template-columns", "repeat(3, 1fr)");
        getStyle().set("gap", "10px");
    }


    /**
     *  special button to control the door with unique Icon-toggle (Lock/unlock)
     */
    public void addDoorLockButton() {

        // check equipment flag
        boolean enabled = true; // hardcoded for now - update when db issue is fixed

        final boolean[] isLocked = {false}; // save state client side
        Icon icon = VaadinIcon.LOCK.create();
        icon.setSize("45px");
        Span text = new Span("Lock Door");
        text.getStyle().set("font-weight", "600");

        VerticalLayout customButton = createBaseButton(icon, text, enabled);

        // ui logic for toggle state (color, text and icon)
        if (enabled) {
            Runnable updateStyle = () -> {
                customButton.getStyle().set("background-color", isLocked[0] ? "var(--lumo-error-color)" : "var(--lumo-contrast-10pct)");
                customButton.getStyle().set("color", isLocked[0] ? "white" : "var(--lumo-body-text-color)");
                text.setText(isLocked[0] ? "Door Locked" : "Lock Door");
                icon.getElement().setAttribute("icon", isLocked[0] ? "vaadin:lock" : "vaadin:unlock");
            };

            customButton.addClickListener(e -> {
                isLocked[0] = !isLocked[0];
                updateStyle.run();
                showNotification(isLocked[0] ? "Door Locked" : "Door Unlocked", isLocked[0]);
            });
            updateStyle.run();
        }
        add(customButton);
    }

    /**
     * Generic button for room equipment
     * @param label name of the button
     * @param iconEnum the displayed icon
     * @param onMsg on - notification
     * @param offMsg off - notification
     */
    public void addButton(String label, VaadinIcon iconEnum, String onMsg, String offMsg, boolean enabled) {
        final boolean[] isActive = {false};
        Icon icon = iconEnum.create();
        icon.setSize("45px");
        Span text = new Span(label);
        text.getStyle().set("font-weight", "600");

        VerticalLayout customButton = createBaseButton(icon, text, enabled);

        if (enabled) {
            Runnable updateStyle = () -> {
                // blue if on, grey if off
                customButton.getStyle().set("background-color", isActive[0] ? "var(--lumo-primary-color)" : "var(--lumo-contrast-10pct)");
                customButton.getStyle().set("color", isActive[0] ? "var(--lumo-primary-contrast-color)" : "var(--lumo-body-text-color)");
            };

            customButton.addClickListener(e -> {
                isActive[0] = !isActive[0];
                updateStyle.run();
                if (onMsg != null && offMsg != null) {
                    showNotification(isActive[0] ? onMsg : offMsg, false);
                }
            });
            updateStyle.run();
        }
        add(customButton);
    }

    /**
     * Base-layout and styling for a quadratic dashboard button
     */
    private VerticalLayout createBaseButton(Icon icon, Span text, boolean enabled) {
        VerticalLayout button = new VerticalLayout(icon, text);
        button.setAlignItems(Alignment.CENTER);
        button.setJustifyContentMode(JustifyContentMode.CENTER);
        button.setWidth("150px");
        button.setHeight("150px");
        button.getStyle().set("border-radius", "var(--lumo-border-radius-l)");
        button.setMargin(false);
        button.setPadding(false);

        if (!enabled) {
            // Ausgegrautes Styling
            button.getStyle().set("background-color", "var(--lumo-contrast-5pct)");
            button.getStyle().set("color", "var(--lumo-disabled-text-color)");
            button.getStyle().set("opacity", "0.5");
            button.getStyle().set("cursor", "not-allowed");
            icon.setColor("var(--lumo-disabled-text-color)");
        } else {
            button.getStyle().set("cursor", "pointer");
        }
        return button;
    }

    /**
     * Display button-notification
     */
    private void showNotification(String msg, boolean isError) {
        Notification n = new Notification(msg, 2000, Notification.Position.TOP_CENTER);
        if (isError) n.addThemeVariants(NotificationVariant.LUMO_ERROR);
        n.open();
    }
}