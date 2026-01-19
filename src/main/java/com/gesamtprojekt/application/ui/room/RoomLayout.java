package com.gesamtprojekt.application.ui.room;


import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.dependency.CssImport;


public class RoomLayout extends AppLayout {

    public RoomLayout() {

        getElement().getStyle()
                .set("padding", "0")
                .set("margin", "0");
    }
}
