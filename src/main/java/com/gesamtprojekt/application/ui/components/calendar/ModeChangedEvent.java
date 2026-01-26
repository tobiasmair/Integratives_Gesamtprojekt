package com.gesamtprojekt.application.ui.components.calendar;

import com.vaadin.flow.component.ComponentEvent;

public class ModeChangedEvent extends ComponentEvent<CalendarControlsBar> {
    private final ViewMode mode;

    public ModeChangedEvent(CalendarControlsBar source, ViewMode mode) {
        super(source, false);
        this.mode = mode;
    }

    public ViewMode getMode() {
        return mode;
    }
}
