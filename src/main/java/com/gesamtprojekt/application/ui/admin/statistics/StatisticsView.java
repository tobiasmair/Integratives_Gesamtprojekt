package com.gesamtprojekt.application.ui.admin.statistics;

import com.gesamtprojekt.application.service.dto.MonthlyCount;
import com.gesamtprojekt.application.service.dto.RoomBookingCount;
import com.gesamtprojekt.application.service.dto.RoomUtilization;
import com.gesamtprojekt.application.service.implementation.StatisticsService;
import com.gesamtprojekt.application.ui.client.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.time.LocalDateTime;
import java.util.List;

@Route(value = "statistics", layout = MainLayout.class)
@PageTitle("Statistics | MCI Meeting Booker")
@RolesAllowed("ADMIN")
public class StatisticsView extends VerticalLayout {

    private final StatisticsService statisticsService;

    // KPIs
    private final Span totalBookings = new Span("-");
    private final Span activeRooms = new Span("-");
    private final Span buildings = new Span("-");
    private final Span equipmentCount = new Span("-");
    private final Span bookedRoomsToday = new Span("-");
    private final Span bookedRoomsWeek = new Span("-");
    private final Span bookedRoomsMonth = new Span("-");

    // Buttons
    private Button btnTopRooms;
    private Button btnMonthly;
    private Button btnUtilization;

    // Content area
    private final VerticalLayout contentArea = new VerticalLayout();

    // Child views
    private final TopRoomsView topRoomsView = new TopRoomsView();
    private final MonthlyBookingsView monthlyBookingsView = new MonthlyBookingsView();
    private final RoomUtilizationView roomUtilizationView = new RoomUtilizationView();

    public StatisticsView(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        //add(buildHeader());
        add(buildKpiRow());
        add(buildTabBar());
        add(buildContentArea());

        loadAll();

        // Default
        show(topRoomsView, btnTopRooms);
    }



    private HorizontalLayout buildKpiRow() {
        HorizontalLayout row = new HorizontalLayout(
                kpi("Total Bookings", totalBookings),
                kpi("Active Rooms", activeRooms),
                kpi("Buildings", buildings),
                kpi("Equipment", equipmentCount),
                kpi("Today", bookedRoomsToday),
                kpi("Last 7 Days", bookedRoomsWeek),
                kpi("Last 30 Days", bookedRoomsMonth)
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

    private Component buildTabBar() {
        btnTopRooms = new Button("Top 5 Rooms (last 30 days)", e -> show(topRoomsView, btnTopRooms));
        btnMonthly = new Button("Bookings per Month (current year)", e -> show(monthlyBookingsView, btnMonthly));
        btnUtilization = new Button("Room Utilization (last 30 days)", e -> show(roomUtilizationView, btnUtilization));

        HorizontalLayout bar = new HorizontalLayout(btnTopRooms, btnMonthly, btnUtilization);
        bar.setWidthFull();
        bar.setSpacing(true);
        bar.getStyle().set("flex-wrap", "wrap");
        return bar;
    }

    private Component buildContentArea() {
        contentArea.setWidthFull();
        contentArea.setPadding(true);
        contentArea.setSpacing(true);
        contentArea.setSizeFull();

        contentArea.getStyle()
                .set("border", "1px solid var(--lumo-contrast-20pct)")
                .set("border-radius", "12px")
                .set("box-shadow", "var(--lumo-box-shadow-s)")
                .set("background", "var(--lumo-base-color)");

        setFlexGrow(1, contentArea);
        return contentArea;
    }

    private void setActive(Button active) {
        btnTopRooms.getThemeNames().remove("primary");
        btnMonthly.getThemeNames().remove("primary");
        btnUtilization.getThemeNames().remove("primary");
        active.getThemeNames().add("primary");
    }

    private void show(Component view, Button activeButton) {
        setActive(activeButton);
        contentArea.removeAll();
        contentArea.add(view);
        contentArea.setFlexGrow(1, view);
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

        // Time window
        LocalDateTime start = statisticsService.defaultStart30Days();
        LocalDateTime end = statisticsService.now();

        // Data
        List<RoomBookingCount> topRooms = statisticsService.getTopRooms(start, end, 5);
        List<MonthlyCount> monthly = statisticsService.getBookingsPerMonth(end.getYear());
        List<RoomUtilization> utilization = statisticsService.getRoomUtilization(start, end);

        // Pass to child views
        topRoomsView.setData(topRooms);
        monthlyBookingsView.setData(monthly);
        roomUtilizationView.setData(utilization);
    }
}