package com.gesamtprojekt.application.ui.admin.statistics;

import com.gesamtprojekt.application.service.dto.MonthlyCount;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

public class MonthlyBookingsView extends VerticalLayout {

    private final Grid<MonthlyCount> grid = new Grid<>(MonthlyCount.class, false);

    public MonthlyBookingsView() {
        setSizeFull();
        setPadding(false);
        setSpacing(false);

        grid.addColumn(mc -> Month.of(mc.month()).getDisplayName(TextStyle.SHORT, Locale.ENGLISH))
                .setHeader("Month").setAutoWidth(true);

        grid.addColumn(MonthlyCount::bookings)
                .setHeader("Bookings").setAutoWidth(true);

        grid.setWidthFull();
        grid.setHeightFull();

        add(grid);
        setFlexGrow(1, grid);
    }

    public void setData(List<MonthlyCount> items) {
        grid.setItems(items == null ? List.of() : items);
    }
}
