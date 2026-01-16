package com.gesamtprojekt.application.ui.components.dashboard;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;

// Event das ausgelöst wird, wenn eine Buchung erstellt, bearbeitet oder gelöscht wurde
public class BookingChangedEvent extends ComponentEvent<Component> {
    // Komponente die Event auslöst
    public BookingChangedEvent(Component source) {
        super(source, false);   // false: Event kommt von Server
    }

}
