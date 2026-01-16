package com.gesamtprojekt.application.ui.components.dashboard;

import com.gesamtprojekt.application.model.Booking;
import com.gesamtprojekt.application.model.MeetingRoom;
import com.gesamtprojekt.application.service.implementation.BookingService;
import com.gesamtprojekt.application.service.implementation.MeetingRoomService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.shared.Registration;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

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
        add(createRow(title, room, dateText, timeRange, status));
    }

    private HorizontalLayout createRow(
            String title,
            String room,
            String dateText,
            String timeRange,
            String status
    ) {
        var left = createTextBlock(title, room, dateText, status);
        var edit = createEditButton();
        var delete = createDeleteButton();
        var badge = createTimeBadge(timeRange);

        var row = new HorizontalLayout(left, edit, delete, badge);
        row.setWidthFull();
        row.setAlignItems(HorizontalLayout.Alignment.CENTER);
        row.expand(left);

        return row;
    }

    private VerticalLayout createTextBlock(String title, String room, String dateText, String status) {
        var t = new Span(title);
        t.addClassName("booking-title");

        var r = new Span(room);
        r.addClassName("booking-subtitle");

        var d = new Span(dateText);
        d.addClassName("booking-date");

        var s = new Span(status);
        s.addClassName("booking-status");

        var box = new VerticalLayout(t, r, d, s);
        box.setPadding(false);
        box.setSpacing(false);

        return box;
    }

    private Button createEditButton() {
        Button edit = new Button(VaadinIcon.EDIT.create());
        edit.addClickListener(e -> {
            openEditDialog(booking);
        });

        return edit;
    }

    public Button createDeleteButton() {
        Button delete = new Button(VaadinIcon.TRASH.create());
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
        delete.addClickListener(e -> openDeleteDialog(booking));

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

        //editForm.getStyle().set("box-shadow", "none");
        //editForm.getStyle().set("padding", "0");

        /*
        // Eingabefelder
        TextField purposeField = new TextField("Purpose");
        purposeField.setValue(booking.getPurpose() != null ? booking.getPurpose() : "");
        purposeField.setWidthFull();

        DatePicker datePicker = new DatePicker("Date");
        datePicker.setValue(booking.getStartTime().toLocalDate());

        TimePicker startTime = new TimePicker("Start Time");
        startTime.setValue(booking.getStartTime().toLocalTime());
        startTime.setStep(Duration.ofMinutes(30));

        TimePicker endTime = new TimePicker("End Time");
        endTime.setValue(booking.getEndTime().toLocalTime());
        endTime.setStep(Duration.ofMinutes(30));

        RadioButtonGroup<MeetingRoom> roomGroup = new RadioButtonGroup<>("Select Meeting Room");
        roomGroup.setRenderer(new ComponentRenderer<>(room -> new Span(room.getName() + " (Cap: " + room.getCapacity() + ")")));
        roomGroup.setWidthFull();

        // Laden freier Räume
        Runnable refreshRooms = () -> {
            if (datePicker.getValue() != null && startTime.getValue() != null && endTime.getValue() != null) {
                LocalDateTime start = datePicker.getValue().atTime(startTime.getValue());
                LocalDateTime end = datePicker.getValue().atTime(endTime.getValue());

                if (end.isBefore(start) || end.isEqual(start)) {
                    Notification.show("End time must be after start time.", 3000, Notification.Position.TOP_CENTER)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    roomGroup.setItems(List.of());
                    return;
                }

                List<MeetingRoom> availableRooms = meetingRoomService.findAvailableRoomsExcludingBooking(start, end, booking.getBookingId());
                roomGroup.setItems(availableRooms);
                if (availableRooms.isEmpty()) {
                    roomGroup.setLabel("No rooms available for the selected time.");
                } else {
                    roomGroup.setLabel("Select Meeting Room");
                }

                // Aktuellen Raum auswählen
                if (booking.getMeetingRoom() != null) {
                    availableRooms.stream()
                            .filter(room -> room.getRoomId().equals(booking.getMeetingRoom().getRoomId()))
                            .findFirst()
                            .ifPresent(roomGroup::setValue);
                }
            }
        };
        // Change Listener bei Änderungen
        datePicker.addValueChangeListener(e -> refreshRooms.run());
        startTime.addValueChangeListener(e -> refreshRooms.run());
        endTime.addValueChangeListener(e -> refreshRooms.run());

        refreshRooms.run(); // Initial laden

        VerticalLayout dialogLayout = new VerticalLayout(purposeField, datePicker, new HorizontalLayout(startTime, endTime), roomGroup);
        dialog.add(dialogLayout);

         */
        dialog.add(editForm);

        Button saveButton = new Button("Save", event -> {
            try {
                editForm.updateBookingObject(booking);

                /*
                booking.setPurpose(purposeField.getValue());
                booking.setStartTime(datePicker.getValue().atTime(startTime.getValue()));
                booking.setEndTime(datePicker.getValue().atTime(endTime.getValue()));
                booking.setMeetingRoom(roomGroup.getValue());

                 */

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
