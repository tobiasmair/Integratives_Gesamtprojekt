package com.gesamtprojekt.application.ui.components.dashboard;

import com.gesamtprojekt.application.model.Booking;
import com.gesamtprojekt.application.security.SecurityService;
import com.gesamtprojekt.application.service.implementation.BookingService;
import com.gesamtprojekt.application.service.implementation.MeetingRoomService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;

import java.time.LocalDateTime;
import java.time.LocalTime;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.shared.Registration;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class MyBookingsContainer extends Div {

    private final BookingService bookingService;
    private final MeetingRoomService meetingRoomService;
    private final SecurityService securityService;

    private final Tab todayTab = new Tab("Today");
    private final Tab weekTab = new Tab("This week");
    private final Tab monthTab = new Tab("This month");
    private final Tabs tabs = new Tabs(todayTab, weekTab, monthTab);

    private final DatePicker customDate = new DatePicker("Custom date");
    private final Span rangeText = new Span();
    private final Div bookingsList = new Div();
    private final Scroller bookingsScroller = new Scroller();

    public MyBookingsContainer(BookingService bookingService, MeetingRoomService meetingRoomService, SecurityService securityService) {
        this.bookingService = bookingService;
        this.meetingRoomService = meetingRoomService;
        this.securityService = securityService;

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
        var title = new H3("My Bookings");
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
        bookingsScroller.setHeight("520px");
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

    public void refresh() {
        onTabChanged();
        // Event für DashboardView -> trigger QuickBookingContainer
        fireEvent(new BookingChangedEvent(this));
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

    private void loadBookingsForDay(LocalDate day) {
        loadBookings(day, day);
    }

    private void loadBookings(LocalDate from, LocalDate to) {
        bookingsList.removeAll();

        securityService.getAuthenticatedClient().ifPresent(client -> {
            List<Booking> allBookings = bookingService.findBookingByClientId(client.getUserId());

            // Zeit Filter
            LocalDateTime startofDay = from.atStartOfDay();
            LocalDateTime endofDay = to.atTime(LocalTime.MAX);

            // Filtern
            allBookings.stream()
                    .filter(b -> !b.getStartTime().isBefore(startofDay) && !b.getEndTime().isAfter(endofDay))
                    .sorted(Comparator.comparing(Booking::getStartTime))
                    .forEach(b -> bookingsList.add(toBookingItem(b)));

            if (bookingsList.getChildren().findAny().isEmpty()) {
                bookingsList.add(new Span("No bookings in this period."));
            }
        });
    }

    private BookingItem toBookingItem(Booking booking) {
        String meetingRoomName = booking.getMeetingRoom() != null ? booking.getMeetingRoom().getName() : "Unknown Room";

        // Formatiere Datum und Uhrzeit
        String dateString = booking.getStartTime().toLocalDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
        String timeString = booking.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm"))
                + " - " +
                booking.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm"));

        BookingItem item = new BookingItem(
                booking,
                bookingService,
                meetingRoomService,
                this::refresh,  // Runnable event: Liste aktualisieren
                booking.getPurpose(),
                meetingRoomName,
                dateString,
                timeString,
                booking.getBookingStatus()
        );

        return item;
    }

    // View registrieren
    public Registration addBookingChangedListener(ComponentEventListener<BookingChangedEvent> listener) {
        return addListener(BookingChangedEvent.class, listener);
    }

}
