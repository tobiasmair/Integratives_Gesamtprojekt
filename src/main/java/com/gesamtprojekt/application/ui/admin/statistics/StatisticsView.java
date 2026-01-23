package com.gesamtprojekt.application.ui.admin.statistics;

import com.gesamtprojekt.application.service.dto.MonthlyCount;
import com.gesamtprojekt.application.service.dto.RoomBookingCount;
import com.gesamtprojekt.application.service.dto.RoomUtilization;
import com.gesamtprojekt.application.service.implementation.StatisticsService;
import com.gesamtprojekt.application.ui.client.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

@Route(value = "statistics", layout = MainLayout.class)
@PageTitle("Statistics | MCI Meeting Booker")
@RolesAllowed("ADMIN")
public class StatisticsView extends VerticalLayout {

    private final StatisticsService statisticsService;

    // KPI Spans
    private final Span totalBookings = new Span("-");
    private final Span activeRooms = new Span("-");
    private final Span buildings = new Span("-");
    private final Span equipmentCount = new Span("-");
    private final Span bookedRoomsToday = new Span("-");
    private final Span bookedRoomsWeek = new Span("-");
    private final Span bookedRoomsMonth = new Span("-");

    private final Grid<RoomBookingCount> topRoomsGrid = new Grid<>(RoomBookingCount.class, false);
    private final Grid<MonthlyCount> monthlyGrid = new Grid<>(MonthlyCount.class, false);
    private final Grid<RoomUtilization> utilizationGrid = new Grid<>(RoomUtilization.class, false);

    public StatisticsView(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        //add(new H1("Statistics Übersicht"));

        //Button refresh = new Button("Aktualisieren", e -> loadAll());
        //add(refresh);

        add(buildKpiRow());

        add(new H3("Top 5 Räume nach Buchungen (letzte 30 Tage)"));
        configureTopRoomsGrid();
        add(topRoomsGrid);

        add(new H3("Buchungen pro Monat (aktuelles Jahr)"));
        configureMonthlyGrid();
        add(monthlyGrid);

        add(new H3("Auslastung je Raum (letzte 30 Tage)"));
        configureUtilizationGrid();
        add(utilizationGrid);

        loadAll();
    }

    private HorizontalLayout buildKpiRow() {
        HorizontalLayout row = new HorizontalLayout(
                kpi("Aktive Buchungen", totalBookings),
                kpi("Aktive Räume", activeRooms),
                kpi("Gebäude", buildings),
                kpi("Equipment", equipmentCount),
                kpi("Heute", bookedRoomsToday),
                kpi("Letzte 7 Tage", bookedRoomsWeek),
                kpi("Letzte 30 Tage", bookedRoomsMonth)
        );
        row.setWidthFull();
        row.setSpacing(true);
        row.getStyle().set("flex-wrap", "wrap");
        return row;
    }

    private VerticalLayout kpi(String label, Span value) {
        value.getStyle().set("font-size", "1.8rem").set("font-weight", "700");
        Span l = new Span(label);
        l.getStyle().set("opacity", "0.7");

        VerticalLayout box = new VerticalLayout(l, value);
        box.setPadding(true);
        box.setSpacing(false);
        box.setWidth("220px");

        box.getStyle()
                .set("border", "1px solid var(--lumo-contrast-20pct)")
                .set("border-radius", "12px")
                .set("box-shadow", "var(--lumo-box-shadow-s)")
                .set("background", "var(--lumo-base-color)");
        return box;
    }

    private void configureTopRoomsGrid() {
        topRoomsGrid.addColumn(RoomBookingCount::name).setHeader("Raum").setAutoWidth(true);
        topRoomsGrid.addColumn(RoomBookingCount::location).setHeader("Gebäude").setAutoWidth(true);
        topRoomsGrid.addColumn(RoomBookingCount::bookings).setHeader("Buchungen").setAutoWidth(true);

        topRoomsGrid.setWidthFull();
        topRoomsGrid.setHeight("260px");
    }

    private void configureMonthlyGrid() {
        monthlyGrid.addColumn(mc -> Month.of(mc.month()).getDisplayName(TextStyle.SHORT, Locale.GERMAN))
                .setHeader("Monat").setAutoWidth(true);

        monthlyGrid.addColumn(MonthlyCount::bookings)
                .setHeader("Buchungen").setAutoWidth(true);

        monthlyGrid.setWidthFull();
        monthlyGrid.setHeight("420px");
    }

    private void configureUtilizationGrid() {
        utilizationGrid.addColumn(RoomUtilization::name).setHeader("Raum").setAutoWidth(true);
        utilizationGrid.addColumn(RoomUtilization::location).setHeader("Gebäude").setAutoWidth(true);

        utilizationGrid.addComponentColumn(u -> {
            double pct = Math.max(0, Math.min(100, u.utilizationPercent()));
            ProgressBar pb = new ProgressBar(0, 100, pct);
            pb.setWidth("220px");
            pb.getElement().setProperty("title", String.format("%.1f%%", pct));
            return pb;
        }).setHeader("Auslastung").setAutoWidth(true);

        utilizationGrid.addColumn(u -> String.format("%.1f%%", u.utilizationPercent()))
                .setHeader("%").setAutoWidth(true);

        utilizationGrid.setWidthFull();
        utilizationGrid.setHeight("420px");
    }

    private void loadAll() {
        // KPIs
        totalBookings.setText(String.valueOf(statisticsService.getTotalActiveBookings()));
        activeRooms.setText(String.valueOf(statisticsService.getActiveRooms()));
        buildings.setText(String.valueOf(statisticsService.getActiveLocations()));
        equipmentCount.setText(String.valueOf(statisticsService.getTotalEquipment()));

        bookedRoomsToday.setText(String.valueOf(statisticsService.getBookedRoomsToday()));
        bookedRoomsWeek.setText(String.valueOf(statisticsService.getBookedRoomsLast7Days()));
        bookedRoomsMonth.setText(String.valueOf(statisticsService.getBookedRoomsLast30Days()));

        // Zeitraum (letzte 30 Tage)
        LocalDateTime start = statisticsService.defaultStart30Days();
        LocalDateTime end = statisticsService.now();

        // Top Rooms
        List<RoomBookingCount> topRooms = statisticsService.getTopRooms(start, end, 5);
        topRoomsGrid.setItems(topRooms);

        // Monthly
        int year = end.getYear();
        monthlyGrid.setItems(statisticsService.getBookingsPerMonth(year));

        // Utilization
        utilizationGrid.setItems(statisticsService.getRoomUtilization(start, end));
    }
}
