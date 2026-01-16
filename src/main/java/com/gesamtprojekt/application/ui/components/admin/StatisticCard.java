package com.gesamtprojekt.application.ui.components.admin;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class StatisticCard extends HorizontalLayout {
    public StatisticCard(String title, String value, VaadinIcon icon) {
        VerticalLayout text=new VerticalLayout(new Span(title),new Span(value));
        add (icon.create(),text);
    }
}
