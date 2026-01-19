package com.gesamtprojekt.application.ui.room.components;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

public class RoomHeader extends HorizontalLayout {

    public RoomHeader() {
        setWidthFull();
        setHeight("50px");
        setPadding(false);
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        // Styling
        getStyle().set("background-color", "var(--lumo-base-color)");
        getStyle().set("border-bottom", "1px solid var(--lumo-contrast-10pct)");
        getStyle().set("box-shadow", "var(--lumo-box-shadow-xs)");

        // display current time
        Span timeSpan = new Span();
        timeSpan.getStyle().set("font-size", "var(--lumo-font-size-xl)");
        timeSpan.getStyle().set("font-weight", "600");
        timeSpan.setId("live-clock"); // ID for javascript

        add(timeSpan);

        // Ein kleiner JavaScript-Schnipsel, der die Uhrzeit im Browser aktualisiert
        // So läuft die Uhr flüssig ohne Server-Requests
        getElement().executeJs(
                "const clock = document.getElementById('live-clock');" +
                        "setInterval(() => {" +
                        "  clock.textContent = new Date().toLocaleTimeString('de-DE', {hour: '2-digit', minute:'2-digit'});" +
                        "}, 1000);"
        );
    }
}
