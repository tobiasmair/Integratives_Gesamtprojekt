package com.gesamtprojekt.application.ui.admin.statistics;

import com.gesamtprojekt.application.service.implementation.StatisticsService;
import com.gesamtprojekt.application.ui.client.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "statistics", layout = MainLayout.class)
@PageTitle("Statistics")
@RolesAllowed("ADMIN")
public class StatisticsView extends VerticalLayout {

    private final StatisticsService statisticsService;

    private final Span totalBookings = new Span("-");
    private final Span activeRooms = new Span("-");
    private final Span totalCapacity = new Span("-");
    private final Span buildings = new Span("-");
    private final Span activeUsers = new Span("-");
    private final Span equipmentCount = new Span("-");
    private final Span bookedRoomsToday = new Span("-");
    private final Span bookedRoomsWeek = new Span("-");
    private final Span bookedRoomsMonth = new Span("-");


    public StatisticsView(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(new H1("Statistics"));

        Button refresh = new Button("Refresh", e -> loadStatistics());
        add(refresh);

        HorizontalLayout row1 = new HorizontalLayout(
                kpiCard("Total bookings", totalBookings),
                kpiCard("Active rooms", activeRooms),
                kpiCard("Total capacity", totalCapacity)
        );
        row1.setWidthFull();
        row1.setFlexGrow(1);

        HorizontalLayout row2 = new HorizontalLayout(
                kpiCard("Buildings", buildings),
                kpiCard("Active users", activeUsers),
                kpiCard("Equipment (total)", equipmentCount)
        );
        row2.setWidthFull();
        row2.setFlexGrow(1);

        HorizontalLayout row3 = new HorizontalLayout(
                kpiCard("Booked rooms today", bookedRoomsToday),
                kpiCard("Booked rooms this week", bookedRoomsWeek),
                kpiCard("Booked rooms this month", bookedRoomsMonth)
        );

        add(row1, row2, row3);

        loadStatistics();
    }

    private VerticalLayout kpiCard(String title, Span value) {
        Span t = new Span(title);
        t.getStyle().set("font-weight", "600");

        value.getStyle()
                .set("font-size", "26px")
                .set("font-weight", "700");

        VerticalLayout card = new VerticalLayout(t, value);
        card.setWidthFull();
        card.setSpacing(false);
        card.getStyle()
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "12px")
                .set("padding", "12px");
        return card;
    }

    private void loadStatistics() {
        totalBookings.setText(String.valueOf(statisticsService.getTotalBookings()));
        activeRooms.setText(String.valueOf(statisticsService.getActiveRooms()));
        totalCapacity.setText(String.valueOf(statisticsService.getTotalCapacity()));
        buildings.setText(String.valueOf(statisticsService.getBuildingsCount()));
        activeUsers.setText(String.valueOf(statisticsService.getActiveUsers()));
        equipmentCount.setText(String.valueOf(statisticsService.getEquipmentCount()));
        bookedRoomsToday.setText(String.valueOf(statisticsService.getBookedRoomsToday()));
        bookedRoomsWeek.setText(String.valueOf(statisticsService.getBookedRoomsThisWeek()));
        bookedRoomsMonth.setText(String.valueOf(statisticsService.getBookedRoomsThisMonth()));

    }
}
