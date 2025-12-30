package com.gesamtprojekt.application.ui.components.dashboard;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class BookingItem extends Div {

    public BookingItem(String title, String room, String dateText, String timeRange) {
        addClassName("booking-item");
        add(createRow(title, room, dateText, timeRange));
    }

    private HorizontalLayout createRow(
            String title,
            String room,
            String dateText,
            String timeRange
    ) {
        var left = createTextBlock(title, room, dateText);
        var badge = createTimeBadge(timeRange);

        var row = new HorizontalLayout(left, badge);
        row.setWidthFull();
        row.setAlignItems(HorizontalLayout.Alignment.CENTER);
        row.expand(left);

        return row;
    }

    private VerticalLayout createTextBlock(String title, String room, String dateText) {
        var t = new Span(title);
        t.addClassName("booking-title");

        var r = new Span(room);
        r.addClassName("booking-subtitle");

        var d = new Span(dateText);
        d.addClassName("booking-date");

        var box = new VerticalLayout(t, r, d);
        box.setPadding(false);
        box.setSpacing(false);

        return box;
    }

    private Span createTimeBadge(String text) {
        var badge = new Span(text);
        badge.addClassName("booking-time-badge");
        return badge;
    }
}
