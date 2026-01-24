package com.gesamtprojekt.application.ui.components.calendar;

import com.gesamtprojekt.application.model.Booking;
import com.gesamtprojekt.application.service.implementation.BookingService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class RoomDetailsDialog extends Dialog {

    private final BookingService bookingService;
    private final CalendarRoomCardModel room;
    private VerticalLayout bookingsContainer;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public RoomDetailsDialog(CalendarRoomCardModel room, BookingService bookingService) {
        this.room = room;
        this.bookingService = bookingService;

        setWidth("700px");
        setMaxHeight("95vh");
        setCloseOnEsc(true);
        setCloseOnOutsideClick(true);

        add(createContent(room));
    }

    // Konstruktor für "alten"" Code - backup
    public RoomDetailsDialog(CalendarRoomCardModel room) {
        this(room, null);
    }

    private VerticalLayout createContent(CalendarRoomCardModel room) {
        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);

        H2 header = new H2(room.name() != null ? room.name() : "");
        header.getStyle().set("margin", "0");

        Button closeButton = new Button(new Icon(VaadinIcon.CLOSE_SMALL), e -> close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);
        closeButton.getStyle()
                .set("margin", "0")
                .set("padding", "0");

        HorizontalLayout headerRow = new HorizontalLayout(header, closeButton);
        headerRow.setWidthFull();
        headerRow.setAlignItems(HorizontalLayout.Alignment.CENTER);
        headerRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        headerRow.getStyle().set("margin-bottom", "4px");

        Image img = buildImage(room.name());

        VerticalLayout infoSection = new VerticalLayout();
        infoSection.setPadding(false);
        infoSection.setSpacing(true);
        infoSection.add(infoRow(VaadinIcon.BUILDING, "Building: " + (room.building() != null ? room.building() : "")));
        infoSection.add(infoRow(VaadinIcon.USERS, "Capacity: " + (room.capacity() != null ? room.capacity() : 0) + " people"));
        infoSection.add(infoRow(VaadinIcon.LINES, "Floor: " + (room.floor() != null ? room.floor() : 0)));

        if (room.tags() != null && !room.tags().isEmpty()) {
            Span equipmentLabel = new Span("Equipment:");
            equipmentLabel.getStyle().set("font-weight", "600");
            equipmentLabel.getStyle().set("margin-top", "8px");

            HorizontalLayout tagsLayout = new HorizontalLayout();
            tagsLayout.getStyle().set("flex-wrap", "wrap");
            tagsLayout.getStyle().set("gap", "8px");
            room.tags().forEach(tag -> tagsLayout.add(createTagChip(tag)));

            infoSection.add(equipmentLabel, tagsLayout);
        }

        // Bookings Section (nur wenn BookingService vorhanden)
        if (bookingService != null && room.roomId() != null) {
            content.add(headerRow, img, infoSection, createBookingsSection());
        } else {
            content.add(headerRow, img, infoSection);
        }

        return content;
    }

    private VerticalLayout createBookingsSection() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.setSpacing(true);
        section.setWidthFull();
        section.getStyle().set("margin-top", "8px");

        H4 bookingsHeader = new H4("Bookings");
        bookingsHeader.getStyle().set("margin", "8px 0");

        Tab todayTab = new Tab("Today");
        Tab weekTab = new Tab("This Week");
        Tab monthTab = new Tab("This Month");

        Tabs tabs = new Tabs(todayTab, weekTab, monthTab);
        tabs.setWidthFull();

        // Container für die Bookings
        bookingsContainer = new VerticalLayout();
        bookingsContainer.setPadding(false);
        bookingsContainer.setSpacing(true);
        bookingsContainer.setWidthFull();

        // Scroller um den Container
        Scroller scroller = new Scroller(bookingsContainer);
        scroller.setWidthFull();
        scroller.setHeight("200px"); // Fixe Höhe für Scroller
        scroller.setScrollDirection(Scroller.ScrollDirection.VERTICAL);

        // SAls erstes Today laden
        loadBookingsForPeriod("today");

        tabs.addSelectedChangeListener(event -> {
            Tab selected = tabs.getSelectedTab();
            if (selected == todayTab) {
                loadBookingsForPeriod("today");
            } else if (selected == weekTab) {
                loadBookingsForPeriod("week");
            } else if (selected == monthTab) {
                loadBookingsForPeriod("month");
            }
        });

        section.add(bookingsHeader, tabs, scroller);
        return section;
    }

    private void loadBookingsForPeriod(String period) {
        bookingsContainer.removeAll();

        LocalDateTime start;
        LocalDateTime end;

        switch (period) {
            case "today":
                start = LocalDate.now().atStartOfDay();
                end = LocalDate.now().atTime(LocalTime.MAX);
                break;
            case "week":
                start = LocalDate.now().atStartOfDay();
                end = LocalDate.now().plusDays(7).atTime(LocalTime.MAX);
                break;
            case "month":
                start = LocalDate.now().atStartOfDay();
                end = LocalDate.now().plusMonths(1).atTime(LocalTime.MAX);
                break;
            default:
                return;
        }

        List<Booking> bookings = bookingService.findBookingsByRoomAndTimeRange(room.roomId(), start, end);

        if (bookings.isEmpty()) {
            Span noBookings = new Span("No bookings in this period");
            noBookings.getStyle()
                    .set("color", "var(--lumo-secondary-text-color)")
                    .set("font-style", "italic")
                    .set("padding", "12px");
            bookingsContainer.add(noBookings);
        } else {
            bookings.forEach(booking -> bookingsContainer.add(createBookingItem(booking)));
        }
    }

    private VerticalLayout createBookingItem(Booking booking) {
        VerticalLayout item = new VerticalLayout();
        item.setPadding(true);
        item.setSpacing(true);
        item.setWidthFull();
        item.getStyle()
                .set("border", "1px solid var(--lumo-contrast-20pct)")
                .set("border-radius", "4px")
                .set("margin-bottom", "4px")
                .set("background", "var(--lumo-base-color)")
                .set("gap", "12px");

        String dateStr = booking.getStartTime().format(DATE_FORMATTER);
        String timeStr = booking.getStartTime().format(TIME_FORMATTER) + " - " +
                booking.getEndTime().format(TIME_FORMATTER);

        String clientName = booking.getClient() != null && booking.getClient().getUsername() != null
                ? booking.getClient().getUsername()
                : "Unknown";

        HorizontalLayout firstRow = new HorizontalLayout();
        firstRow.setSpacing(true);
        firstRow.setPadding(false);
        firstRow.setAlignItems(HorizontalLayout.Alignment.CENTER);
        firstRow.setWidthFull();
        firstRow.getStyle().set("gap", "4px");

        Span dateSpan = new Span(dateStr);
        dateSpan.getStyle()
                .set("font-weight", "600")
                .set("font-size", "14px");

        Span separator1 = new Span("•");
        separator1.getStyle().set("color", "var(--lumo-secondary-text-color)");

        Span timeSpan = new Span(timeStr);
        timeSpan.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "14px");

        Span separator2 = new Span("•");
        separator2.getStyle().set("color", "var(--lumo-contrast-30pct)");

        Icon userIcon = VaadinIcon.USER.create();
        userIcon.setSize("14px");
        userIcon.getStyle().set("color", "var(--lumo-secondary-text-color)");

        Span clientText = new Span(clientName);
        clientText.getStyle()
                .set("font-size", "14px")
                .set("color", "var(--lumo-secondary-text-color)");

        firstRow.add(dateSpan, separator1, timeSpan, separator2, userIcon, clientText);

        // Purpose - nur wenn vorhanden
        if (booking.getPurpose() != null && !booking.getPurpose().trim().isEmpty()) {
            Span separator3 = new Span("•");
            separator3.getStyle().set("color", "var(--lumo-contrast-30pct)");

            Span purposeSpan = new Span(booking.getPurpose());
            purposeSpan.getStyle()
                    .set("font-size", "14px")
                    .set("color", "var(--lumo-secondary-text-color)")
                    .set("font-style", "italic");

            firstRow.add(separator3, purposeSpan);
        }

        item.add(firstRow);

        return item;
    }

    private Image buildImage(String roomName) {
        String src = "/room-images/by-room?roomName=" + (roomName != null ? roomName : "dummypicture");

        Image img = new Image(src, "Room");
        img.setWidthFull();
        img.setHeight("250px");
        img.getStyle().set("object-fit", "cover");
        img.getStyle().set("border-radius", "8px");
        return img;
    }

    private HorizontalLayout infoRow(VaadinIcon icon, String text) {
        Icon i = icon.create();
        i.getStyle().set("color", "var(--lumo-secondary-text-color)");
        i.setSize("18px");

        Span t = new Span(text);
        t.getStyle().set("font-size", "16px");

        HorizontalLayout row = new HorizontalLayout(i, t);
        row.setSpacing(true);
        row.setPadding(false);
        row.setAlignItems(HorizontalLayout.Alignment.CENTER);
        return row;
    }

    private Div createTagChip(String text) {
        Div chip = new Div(new Span(text));
        chip.getStyle().set("font-size", "12px");
        chip.getStyle().set("padding", "6px 12px");
        chip.getStyle().set("border-radius", "4px");
        chip.getStyle().set("background", "var(--lumo-primary-color-10pct)");
        chip.getStyle().set("color", "var(--lumo-primary-text-color)");
        return chip;
    }
}
