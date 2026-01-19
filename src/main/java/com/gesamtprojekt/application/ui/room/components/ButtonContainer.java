package com.gesamtprojekt.application.ui.room.components;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class ButtonContainer extends HorizontalLayout {

    public ButtonContainer() {
        setSpacing(true);
        setPadding(true);
    }

        public void addButton(String label, VaadinIcon iconEnum) {
            // initial state is off
            final boolean[] isActive = {false};

            Icon icon = iconEnum.create();
            icon.setSize("45px");

            Span text = new Span(label);
            text.getStyle().set("font-weight", "600");

            VerticalLayout customButton = new VerticalLayout(icon, text);
            customButton.setAlignItems(Alignment.CENTER);
            customButton.setJustifyContentMode(JustifyContentMode.CENTER);
            customButton.setWidth("150px");
            customButton.setHeight("150px");
            customButton.getStyle().set("border-radius", "var(--lumo-border-radius-l)");
            customButton.getStyle().set("cursor", "pointer");
            customButton.getStyle().set("transition", "all 0.2s ease-in-out");


            Runnable updateStyle = () -> {
                if (isActive[0]) {
                    // Button on
                    customButton.getStyle().set("background-color", "var(--lumo-primary-color)");
                    customButton.getStyle().set("color", "var(--lumo-primary-contrast-color)");
                    customButton.getStyle().set("opacity", "1.0");
                } else {
                    // Button off
                    customButton.getStyle().set("background-color", "var(--lumo-contrast-10pct)");
                    customButton.getStyle().set("color", "var(--lumo-body-text-color)");
                    customButton.getStyle().set("opacity", "0.6");
                }
            };

            updateStyle.run();

            // clicking logic
            customButton.addClickListener(e -> {
                isActive[0] = !isActive[0]; // toggle status
                updateStyle.run();  // update button-design

                String statusText = isActive[0] ? "eingeschaltet" : "ausgeschaltet";

                Notification notification = new Notification(label + " wurde " + statusText);   // create notification- instance
                notification.setPosition(Notification.Position.TOP_CENTER); // set position to top center of the screen
                notification.setDuration(2000); // display for 2 secs
                notification.open();    // show notification
            });

            add(customButton);
        }
    }
