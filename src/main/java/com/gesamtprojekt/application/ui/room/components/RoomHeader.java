package com.gesamtprojekt.application.ui.room.components;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

/**
 * Screen header that shoes the current time
 * to reduce overhead, the time is updated on client-side
 */
public class RoomHeader extends HorizontalLayout {

    public RoomHeader() {
        setWidthFull();
        setHeight("50px");
        setPadding(false);
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        // styling
        getStyle().set("background-color", "var(--lumo-base-color)");
        getStyle().set("border-bottom", "1px solid var(--lumo-contrast-10pct)");
        getStyle().set("box-shadow", "var(--lumo-box-shadow-xs)");

        // display current time
        Span timeSpan = new Span();
        timeSpan.getStyle().set("font-size", "var(--lumo-font-size-xl)");
        timeSpan.getStyle().set("font-weight", "600");
        timeSpan.setId("live-clock"); // ID for javascript

        add(timeSpan);

        // updates the time every second browser-native without request to a server
        getElement().executeJs(
                "const clock = document.getElementById('live-clock');" +
                        "setInterval(() => {" +
                        "  clock.textContent = new Date().toLocaleTimeString('de-DE', {hour: '2-digit', minute:'2-digit'});" +
                        "}, 1000);"
        );
    }
}
