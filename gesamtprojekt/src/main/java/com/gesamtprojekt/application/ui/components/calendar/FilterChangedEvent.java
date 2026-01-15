package com.gesamtprojekt.application.ui.components.calendar;

import com.vaadin.flow.component.ComponentEvent;

public class FilterChangedEvent extends ComponentEvent<CalendarControlsBar> {

    // Komponente die Event auslöst
    public FilterChangedEvent(CalendarControlsBar source) {
            super(source, false);   // false: Event kommt von Server
    }

}
