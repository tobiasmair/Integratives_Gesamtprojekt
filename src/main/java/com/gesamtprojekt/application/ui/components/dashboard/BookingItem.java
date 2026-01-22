package com.gesamtprojekt.application.ui.components.dashboard;

import com.gesamtprojekt.application.model.Booking;
import com.gesamtprojekt.application.model.MeetingRoom;
import com.gesamtprojekt.application.service.implementation.BookingService;
import com.gesamtprojekt.application.service.implementation.MeetingRoomService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.time.LocalDateTime;

public class BookingItem extends Div {

    private final Booking booking;
    private final BookingService bookingService;
    private final MeetingRoomService meetingRoomService;
    private final Runnable runnable;

    public BookingItem(Booking booking, BookingService bookingService, MeetingRoomService meetingRoomService, Runnable runnable, String title, String room, String dateText, String timeRange, String status) {
        this.booking = booking;
        this.bookingService = bookingService;
        this.meetingRoomService = meetingRoomService;
        this.runnable = runnable;

        addClassName("booking-item");

        // Optisches Feedback für vergangene Buchungen
        if (booking.getEndTime().isBefore(LocalDateTime.now())) {
            addClassName("past-booking");
        }

        add(createMainLayout(title, room, dateText, timeRange, status));
    }

    private HorizontalLayout createMainLayout(String title, String room, String dateText, String timeRange, String status) {
        // Linke Seite: Alle Infos
        var infoLayout = createInfoSection(title, room, dateText, status);

        // Rechte Seite: Zeit-Badge und Buttons
        var actionsLayout = new VerticalLayout();
        actionsLayout.setSpacing(true);
        actionsLayout.setPadding(false);
        actionsLayout.setWidth("auto");
        actionsLayout.setAlignItems(FlexComponent.Alignment.END);

        var timeBadge = createTimeBadge(timeRange);
        var buttons = new HorizontalLayout(createEditButton(), createDeleteButton());

        actionsLayout.add(timeBadge, buttons);

        var mainRow = new HorizontalLayout(infoLayout, actionsLayout);
        mainRow.setWidthFull();
        mainRow.setAlignItems(FlexComponent.Alignment.CENTER);
        mainRow.expand(infoLayout);

        return mainRow;
    }

    private VerticalLayout createInfoSection(String title, String room, String dateText, String status) {
        // Titel & Status
        var titleRow = new HorizontalLayout(new Span(title), new Span(status));
        titleRow.setAlignItems(FlexComponent.Alignment.CENTER);
        titleRow.getChildren().findFirst().ifPresent(c -> c.setClassName("booking-title"));
        titleRow.getChildren().skip(1).findFirst().ifPresent(c -> c.setClassName("booking-status"));

        // Raum & Gebäude Info
        MeetingRoom roomEntity = booking.getMeetingRoom();
        String locationInfo = roomEntity != null ?
                roomEntity.getLocation() + " | Floor " + roomEntity.getFloor() : "No Location";

        var locationRow = new HorizontalLayout();
        locationRow.setSpacing(true);
        locationRow.setAlignItems(FlexComponent.Alignment.CENTER);

        Icon roomIcon = VaadinIcon.BUILDING.create();
        roomIcon.setSize("12px");
        Span roomSpan = new Span(room + " (" + locationInfo + ")");
        roomSpan.getStyle().set("font-size", "var(--lumo-font-size-s)").set("color", "var(--lumo-secondary-text-color)");
        locationRow.add(roomIcon, roomSpan);

        // Gehzeit & Lageplan
        var navigationRow = new HorizontalLayout();
        navigationRow.setSpacing(true);
        navigationRow.setAlignItems(FlexComponent.Alignment.CENTER);

        Icon walkIcon = VaadinIcon.MALE.create();
        walkIcon.setSize("12px");
        Span walkTime = new Span("approx. 10 min walk");
        walkTime.getStyle().set("font-size", "var(--lumo-font-size-xs)").set("color", "var(--lumo-tertiary-text-color)");

        // Lageplan Link
        Anchor mapLink = new Anchor("#", "Show Floor Plan");
        mapLink.getStyle().set("font-size", "var(--lumo-font-size-xs)");
        Icon mapIcon = VaadinIcon.MAP_MARKER.create();
        mapIcon.setSize("12px");
        HorizontalLayout mapWrapper = new HorizontalLayout(mapIcon, mapLink);
        mapWrapper.setSpacing(false);
        mapWrapper.setAlignItems(FlexComponent.Alignment.CENTER);
        mapWrapper.addClickListener(e -> Notification.show("Opening floor plan for floor " + (roomEntity != null ? roomEntity.getFloor() : "unknown")));

        navigationRow.add(walkIcon, walkTime, new Span(" • "), mapWrapper);

        // Buchungscode
        Span code = new Span(booking.getBookingCode());
        code.getStyle()
                .set("font-family", "monospace")
                .set("font-weight", "bold")
                .set("background", "var(--lumo-primary-color-10pct)")
                .set("color", "var(--lumo-primary-text-color)")
                .set("padding", "2px 8px")
                .set("border-radius", "4px")
                .set("font-size", "var(--lumo-font-size-s)");

        var codeWrapper = new HorizontalLayout(new Span("Access Code:"), code);
        codeWrapper.setAlignItems(FlexComponent.Alignment.CENTER);
        codeWrapper.getStyle().set("margin-top", "4px");
        codeWrapper.setSpacing(true);

        var section = new VerticalLayout(titleRow, locationRow, navigationRow, codeWrapper);
        section.setPadding(false);
        section.setSpacing(false);
        return section;
    }

    private Button createEditButton() {
        Button edit = new Button(VaadinIcon.EDIT.create());
        // Prüfen ob Buchung bereits verangen
        if (booking.getEndTime().isBefore(LocalDateTime.now())) {
            edit.setEnabled(false);
            edit.setTooltipText("Past bookings cannot be edited.");
        } else {
            edit.addClickListener(e -> {
                openEditDialog(booking);
            });
        }

        return edit;
    }

    public Button createDeleteButton() {
        Button delete = new Button(VaadinIcon.TRASH.create());
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);

        // Prüfen ob Buchung bereits verangen
        if (booking.getEndTime().isBefore(LocalDateTime.now())) {
            delete.setEnabled(false);
            delete.setTooltipText("Past bookings cannot be deleted.");
        } else {
            delete.addClickListener(e -> {
                openDeleteDialog(booking);
            });
        }

        return delete;
    }

    private Span createTimeBadge(String text) {
        var badge = new Span(text);
        badge.addClassName("booking-time-badge");
        return badge;
    }

    // Dialog Fenster für Edit
    private void openEditDialog(Booking booking) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Edit Booking: " + booking.getPurpose());
        dialog.setWidth("800px");

        QuickBookingContainer editForm = new QuickBookingContainer(bookingService, meetingRoomService, null);
        // Daten laden
        editForm.setBooking(booking);
        // Button + Titel verstecken
        editForm.setEditMode(true);

        dialog.add(editForm);

        Button saveButton = new Button("Save", event -> {
            try {
                editForm.updateBookingObject(booking);

                bookingService.updateBooking(booking);

                // Container benachrichtigen
                if (runnable != null) {
                    runnable.run();
                }

                dialog.close();
                Notification.show("Booking updated");
            } catch (Exception e) {
                Notification.show("Error updating booking: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Cancel", event -> dialog.close());

        dialog.getFooter().add(cancelButton, saveButton);
        dialog.open();
    }

    private void openDeleteDialog(Booking booking) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Delete Booking: " + booking.getPurpose());

        Span text = new Span("Are you sure you want to delete Booking " + booking.getPurpose() + " ?");
        dialog.add(text);

        Button deleteButton = new Button("Delete", event -> {
            bookingService.deleteBooking(booking);
            //updateList();
            dialog.close();
            Notification.show("Booking deleted");
            // Container benachrichtigen
            if (runnable != null) {
                runnable.run();
            }
        });
        deleteButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        Button cancelButton = new Button("Cancel", event -> dialog.close());

        dialog.getFooter().add(cancelButton, deleteButton);
        dialog.open();
    }

}
