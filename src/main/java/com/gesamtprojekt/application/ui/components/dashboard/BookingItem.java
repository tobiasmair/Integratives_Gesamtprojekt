package com.gesamtprojekt.application.ui.components.dashboard;

import com.gesamtprojekt.application.model.Booking;
import com.gesamtprojekt.application.model.MeetingRoom;
import com.gesamtprojekt.application.service.implementation.BookingService;
import com.gesamtprojekt.application.service.implementation.DefaultNavigationService;
import com.gesamtprojekt.application.service.implementation.ExitService;
import com.gesamtprojekt.application.service.implementation.MeetingRoomService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.time.LocalDateTime;
import java.util.Objects;

public class BookingItem extends Div {

    private final Booking booking;
    private final BookingService bookingService;
    private final MeetingRoomService meetingRoomService;
    private final ExitService exitService;
    private final DefaultNavigationService defaultNavigationService;
    private final Runnable runnable;

    public BookingItem(Booking booking, BookingService bookingService, MeetingRoomService meetingRoomService,
                       ExitService exitService, DefaultNavigationService defaultNavigationService,
                       Runnable runnable, String title, String room, String dateText, String timeRange, String status) {
        this.booking = booking;
        this.bookingService = bookingService;
        this.meetingRoomService = meetingRoomService;
        this.exitService = exitService;
        this.defaultNavigationService = defaultNavigationService;
        this.runnable = runnable;

        addClassName("booking-item");

        // Optisches Feedback für vergangene Buchungen
        if (booking.getEndTime().isBefore(LocalDateTime.now()) || Objects.equals(booking.getBookingStatus(), "MISSED")) {
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

        // Datum
        var dateRow = new HorizontalLayout();
        dateRow.setSpacing(true);
        dateRow.setAlignItems(FlexComponent.Alignment.CENTER);

        Icon dateIcon = VaadinIcon.CALENDAR.create();
        dateIcon.setSize("12px");

        Span dateSpan = new Span(dateText);
        dateSpan.addClassName("booking-date");
        dateSpan.getStyle()
                .set("font-size", "var(--lumo-font-size-s)")
                .set("font-weight", "600");

        dateRow.add(dateIcon, dateSpan);

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

        // Google Map Link
        if (roomEntity != null && roomEntity.getNearestExit().getBuilding() != null && roomEntity.getNearestExit().getBuilding().getGoogleMapsUrl() != null) {
            com.vaadin.flow.component.html.Anchor mapsLink = new com.vaadin.flow.component.html.Anchor(
                    roomEntity.getNearestExit().getBuilding().getGoogleMapsUrl(),
                    "View on Maps"
            );
            mapsLink.setTarget("_blank"); // In neuem Tab öffnen
            mapsLink.getStyle()
                    .set("font-size", "var(--lumo-font-size-xxs)")
                    .set("margin-left", "8px")
                    .set("color", "var(--lumo-primary-color)");

            locationRow.add(mapsLink);
        }

        // Gehzeit & Lageplan
        var navigationRow = new HorizontalLayout();
        navigationRow.setSpacing(true);
        navigationRow.setAlignItems(FlexComponent.Alignment.CENTER);

        Icon walkIcon = VaadinIcon.MALE.create();
        walkIcon.setSize("12px");

        // Dynamische Gehzeit
        Span walkTime = new Span();
        if (booking.getCalculatedTravelTime() != null && booking.getCalculatedTravelTime() > 0) {
            int minutes = (int) Math.ceil(booking.getCalculatedTravelTime() / 60.0);
            walkTime.setText("approx. " + minutes + " min walk");

            // Start-Exit + Gebäude anzeigen
            if (booking.getStartExit() != null) {
                walkTime.setText(walkTime.getText() + " (from " + booking.getStartExit().getName() + " " +  booking.getStartExit().getBuilding().getName() + ")");
            }
        } else {
            // Fallback für Buchungen ohne Startpunkt
            walkTime.setText("Standard travel info");
        }

        walkTime.getStyle().set("font-size", "var(--lumo-font-size-xs)")
                .set("color", "var(--lumo-tertiary-text-color)");

        // Lageplan Link
        Button mapLink = new Button("Show Floor Plan");
        mapLink.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        mapLink.getStyle().set("font-size", "var(--lumo-font-size-xs)");
        Icon mapIcon = VaadinIcon.MAP_MARKER.create();
        mapIcon.setSize("12px");
        HorizontalLayout mapWrapper = new HorizontalLayout(mapIcon, mapLink);
        mapWrapper.setSpacing(false);
        mapWrapper.setAlignItems(HorizontalLayout.Alignment.CENTER);
        mapWrapper.addClickListener(e -> {
            if (roomEntity == null) {
                Notification.show("No floor plan available");
                return;
            }

            // Dialog erstellen
            Dialog dialog = new Dialog();
            dialog.setHeaderTitle("Floor Plan: " + roomEntity.getName());

            Image floorPlan = buildBlueprint(roomEntity.getName());

            // Layout im Dialog
            VerticalLayout dialogLayout = new VerticalLayout(floorPlan);
            dialogLayout.setPadding(false);
            dialogLayout.setAlignItems(FlexComponent.Alignment.CENTER);

            dialog.add(dialogLayout);

            // Schließen-Button im Footer
            Button closeButton = new Button("Close", event -> dialog.close());
            dialog.getFooter().add(closeButton);

            dialog.open();
        });

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

        var section = new VerticalLayout(titleRow, dateRow, locationRow, navigationRow, codeWrapper);
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

        QuickBookingContainer editForm = new QuickBookingContainer(bookingService, meetingRoomService, null, exitService, defaultNavigationService);
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
                Notification.show("Booking updated", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (Exception e) {
                Notification.show("Error updating booking: " + e.getMessage(), 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
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

            // update bookingStatus to CANCELLED before deleting
            booking.setBookingStatus("CANCELLED");
            bookingService.deleteBooking(booking);

            //updateList();
            dialog.close();
            Notification.show("Booking deleted");
            Notification.show("Booking deleted", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
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

    // Blueprint Lageplan PNG suchen
    private Image buildBlueprint(String roomName) {
        String src = "room_blueprint/" + roomName + ".png";

        Image img = new Image(src, "Blueprint of " + roomName + src);

        img.setWidthFull();
        img.getStyle().set("max-height", "600px");
        img.getStyle().set("min-height", "300px");
        img.getStyle()
                .set("object-fit", "contain")
                .set("background-color", "#f5f5f5")
                .set("border-radius", "12px")
                .set("box-shadow", "var(--lumo-box-shadow-m)");

        // Fallback: Generischer Lageplan
        img.getElement().executeJs(
                "this.addEventListener('error', function() {" +
                        "  this.src = 'room_blueprint/Generic_Map.png';" +
                        "});"
        );

        return img;
    }

}
