package com.gesamtprojekt.application.ui.components.admin;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;

public class StatsChangedEvent extends ComponentEvent<Component> {
    public StatsChangedEvent(Component source) {
        super(source, false);
    }
}
