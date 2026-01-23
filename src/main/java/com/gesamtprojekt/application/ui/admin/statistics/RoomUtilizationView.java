package com.gesamtprojekt.application.ui.admin.statistics;

import com.gesamtprojekt.application.service.dto.RoomUtilization;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;

import java.util.List;

public class RoomUtilizationView extends VerticalLayout {

    private final Grid<RoomUtilization> grid = new Grid<>(RoomUtilization.class, false);

    public RoomUtilizationView() {
        setSizeFull();
        setPadding(false);
        setSpacing(false);

        grid.addColumn(RoomUtilization::name).setHeader("Room").setAutoWidth(true);
        grid.addColumn(RoomUtilization::location).setHeader("Building").setAutoWidth(true);

        grid.addComponentColumn(u -> {
            double pct = Math.max(0, Math.min(100, u.utilizationPercent()));
            ProgressBar pb = new ProgressBar(0, 100, pct);
            pb.setWidth("240px");
            pb.getElement().setProperty("title", String.format("%.1f%%", pct));
            return pb;
        }).setHeader("Utilization").setAutoWidth(true);

        grid.addColumn(u -> String.format("%.1f%%", u.utilizationPercent()))
                .setHeader("%").setAutoWidth(true);

        grid.setWidthFull();
        grid.setHeightFull();

        add(grid);
        setFlexGrow(1, grid);
    }

    public void setData(List<RoomUtilization> items) {
        grid.setItems(items == null ? List.of() : items);
    }
}
