package com.gesamtprojekt.application.ui.components.dashboard;

import com.gesamtprojekt.application.model.Booking;
import com.gesamtprojekt.application.service.implementation.BookingService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class BookingItem extends Div {

    public final Booking booking;
    public final BookingService bookingService;

    public BookingItem(Booking booking, BookingService bookingService, String title, String room, String dateText, String timeRange, String status) {
        this.booking = booking;
        this.bookingService = bookingService;

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

        // Hier component für edit Dialog

        dialog.setHeaderTitle("Edit Booking: " + booking.getPurpose());

        VerticalLayout dialogLayout = new VerticalLayout();
        dialog.add(dialogLayout);

        Button saveButton = new Button("Save", event -> {

            // Hier Logik für update Booking

            dialog.close();
            Notification.show("Booking updated");
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
        });
        deleteButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        Button cancelButton = new Button("Cancel", event -> dialog.close());

        dialog.getFooter().add(cancelButton, deleteButton);
        dialog.open();
    }
}
