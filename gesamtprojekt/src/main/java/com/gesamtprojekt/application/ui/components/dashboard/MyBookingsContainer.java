package com.gesamtprojekt.application.ui.components.dashboard;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import java.time.LocalTime;
import java.util.ArrayList;
import com.vaadin.flow.component.orderedlayout.Scroller;



import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class MyBookingsContainer extends Div {

    private final Tab todayTab = new Tab("Today");
    private final Tab weekTab = new Tab("This week");
    private final Tab monthTab = new Tab("This month");
    private final Tabs tabs = new Tabs(todayTab, weekTab, monthTab);

    private final DatePicker customDate = new DatePicker("Custom date");
    private final Span rangeText = new Span();
    private final Div bookingsList = new Div();
    private final Scroller bookingsScroller = new Scroller();


    private record DummyBooking(String title, String room, LocalDate date, LocalTime start, LocalTime end) {}

    private final List<DummyBooking> dummyBookings = createDummyBookings();

    private LocalDate customSelected = LocalDate.now();

    public MyBookingsContainer() {
        addClassName("my-bookings-container");
        add(createContent());
        initControls();
        showToday();
    }

    private VerticalLayout createContent() {
        var content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);
        content.setWidthFull();
        content.setSizeFull();

        content.add(createHeader());
        content.add(createTopRow());
        content.add(createRangeText());
        content.add(createBookingsList());
        content.expand(bookingsScroller);

        return content;
    }

    private H3 createHeader() {
        var title = new H3("My Bookings:");
        title.getStyle().set("margin", "0");
        return title;
    }

    private HorizontalLayout createTopRow() {
        var row = new HorizontalLayout(tabs, customDate);
        row.setWidthFull();
        row.setAlignItems(HorizontalLayout.Alignment.END);
        row.expand(tabs);
        return row;
    }

    private Span createRangeText() {
        rangeText.addClassName("selected-date-text");
        return rangeText;
    }

    private Component createBookingsList() {
        bookingsList.addClassName("bookings-list");
        bookingsList.setWidthFull();

        bookingsScroller.setContent(bookingsList);
        bookingsScroller.setWidthFull();
        bookingsScroller.setHeight("520px"); // kannst du später feinjustieren
        bookingsScroller.addClassName("bookings-scroller");

        return bookingsScroller;
    }


    private void initControls() {
        customDate.setValue(LocalDate.now());
        customDate.addValueChangeListener(e -> onCustomDatePicked(e.getValue()));
        tabs.addSelectedChangeListener(e -> onTabChanged());
    }

    private void onTabChanged() {
        if (tabs.getSelectedTab() == todayTab) {
            showToday();
            return;
        }
        if (tabs.getSelectedTab() == weekTab) {
            showThisWeek();
            return;
        }
        showThisMonth();
    }

    private void onCustomDatePicked(LocalDate date) {
        if (date == null) {
            return;
        }
        showCustomDate(date);
    }


    private void showToday() {
        var day = LocalDate.now();
        setRangeText("Today: " + formatDay(day));
        loadBookingsForDay(day);
    }

    private void showThisWeek() {
        var today = LocalDate.now();
        var start = startOfWeek(today);
        var end = start.plusDays(6);

        setRangeText("This week: " + formatShort(start) + " - " + formatShort(end));
        loadBookings(start, end);
    }


    private void showThisMonth() {
        var today = LocalDate.now();
        var start = today.withDayOfMonth(1);
        var end = start.plusMonths(1).minusDays(1);

        setRangeText("This month: " + formatShort(start) + " - " + formatShort(end));
        loadBookings(start, end);
    }


    private void showCustomDate(LocalDate date) {
        setRangeText("Custom date: " + formatDay(date));
        loadBookingsForDay(date);
    }


    private LocalDate startOfWeek(LocalDate date) {
        var dow = date.getDayOfWeek();
        var diff = dow.getValue() - DayOfWeek.MONDAY.getValue();
        return date.minusDays(diff < 0 ? 6 : diff);
    }

    private void setRangeText(String text) {
        rangeText.setText(text);
    }

    private String formatDay(LocalDate date) {
        var fmt = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.ENGLISH);
        return date.format(fmt);
    }

    private String formatShort(LocalDate date) {
        var fmt = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);
        return date.format(fmt);
    }

    private void loadDummyBookings() {
        bookingsList.removeAll();

        bookingsList.add(
                new BookingItem(
                        "meeting A",
                        "meeting Room A",
                        formatShort(LocalDate.now()),
                        "09:00 - 11:00"
                ),
                new BookingItem(
                        "meeting B",
                        "meeting Room A",
                        formatShort(LocalDate.now()),
                        "11:00 - 12:00"
                ),
                new BookingItem(
                        "meeting C",
                        "meeting Room A",
                        formatShort(LocalDate.now()),
                        "14:00 - 14:30"
                ),
                new BookingItem(
                        "Lecture DiBSE",
                        "online lecture room A",
                        formatShort(LocalDate.now()),
                        "18:00 - 20:30"
                )
        );
    }

    private List<DummyBooking> createDummyBookings() {
        var list = new ArrayList<DummyBooking>();
        var today = LocalDate.now();

        list.add(new DummyBooking("Daily standup", "Meeting Room A", today, LocalTime.of(9, 0), LocalTime.of(9, 30)));
        list.add(new DummyBooking("Project sync", "Meeting Room B", today, LocalTime.of(11, 0), LocalTime.of(12, 0)));

        list.add(new DummyBooking("Retro", "Meeting Room A", today.plusDays(1), LocalTime.of(14, 0), LocalTime.of(15, 0)));
        list.add(new DummyBooking("Client call", "Online room A", today.plusDays(2), LocalTime.of(18, 0), LocalTime.of(19, 0)));

        list.add(new DummyBooking("Workshop", "Lecture Room D", today.minusDays(1), LocalTime.of(10, 0), LocalTime.of(12, 0)));
        list.add(new DummyBooking("Planning", "Meeting Room C", today.minusDays(3), LocalTime.of(15, 30), LocalTime.of(16, 30)));

        var firstOfMonth = today.withDayOfMonth(1);
        list.add(new DummyBooking("All-hands", "Main Hall", firstOfMonth, LocalTime.of(16, 0), LocalTime.of(17, 0)));
        list.add(new DummyBooking("Budget review", "Meeting Room B", firstOfMonth.plusDays(10), LocalTime.of(9, 0), LocalTime.of(10, 30)));

        var nextMonth = today.plusMonths(1).withDayOfMonth(3);
        list.add(new DummyBooking("Next month kickoff", "Meeting Room A", nextMonth, LocalTime.of(10, 0), LocalTime.of(11, 0)));

        return list;
    }

    private void loadBookingsForDay(LocalDate day) {
        loadBookings(day, day);
    }

    private void loadBookings(LocalDate from, LocalDate to) {
        bookingsList.removeAll();

        dummyBookings.stream()
                .filter(b -> !b.date().isBefore(from) && !b.date().isAfter(to))
                .sorted((a, c) -> {
                    int d = a.date().compareTo(c.date());
                    return d != 0 ? d : a.start().compareTo(c.start());
                })
                .forEach(b -> bookingsList.add(toBookingItem(b)));

        if (bookingsList.getChildren().findAny().isEmpty()) {
            bookingsList.add(new Span("No bookings in this period."));
        }
    }

    private BookingItem toBookingItem(DummyBooking b) {
        return new BookingItem(
                b.title(),
                b.room(),
                formatShort(b.date()),
                formatTimeRange(b.start(), b.end())
        );
    }

    private String formatTimeRange(LocalTime start, LocalTime end) {
        return start.toString() + " - " + end.toString();
    }


}
