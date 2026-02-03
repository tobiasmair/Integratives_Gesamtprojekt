package com.gesamtprojekt.application.ui.admin.statistics;

import com.gesamtprojekt.application.service.dto.RoomBookingCount;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.List;

public class TopRoomsView extends VerticalLayout {

    private final Grid<RoomBookingCount> grid = new Grid<>(RoomBookingCount.class, false);

    public TopRoomsView() {
        setSizeFull();
        setPadding(false);
        setSpacing(false);

        grid.addColumn(RoomBookingCount::name).setHeader("Room").setAutoWidth(true);
        grid.addColumn(RoomBookingCount::location).setHeader("Building").setAutoWidth(true);
        grid.addColumn(RoomBookingCount::bookings).setHeader("Bookings").setAutoWidth(true);

        grid.setWidthFull();
        grid.setHeightFull();

        add(grid);
        setFlexGrow(1, grid);
    }

    public void setData(List<RoomBookingCount> items) {
        grid.setItems(items == null ? List.of() : items);
    }
}
