package com.gesamtprojekt.application.ui.components.calendar;

import com.gesamtprojekt.application.model.Booking;
import com.gesamtprojekt.application.model.MeetingRoom;
import com.gesamtprojekt.application.security.SecurityService;
import com.gesamtprojekt.application.service.implementation.BookingService;
import com.gesamtprojekt.application.service.implementation.MeetingRoomService;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.shared.Registration;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class RoomBookingDialog extends Dialog {

    private final BookingService bookingService;
    private final MeetingRoomService meetingRoomService;
    private final SecurityService securityService;
    private final Long roomId;

    private final DatePicker datePicker = new DatePicker("Date");
    private final TimePicker startTime = new TimePicker("Start");
    private final TimePicker endTime = new TimePicker("End");
    private final TextArea purposeField = new TextArea("Meeting purpose");

    public RoomBookingDialog(Long roomId, String roomName, BookingService bookingService,
                             MeetingRoomService meetingRoomService, SecurityService securityService) {
        this.roomId = roomId;
        this.bookingService = bookingService;
        this.meetingRoomService = meetingRoomService;
        this.securityService = securityService;

        setCloseOnEsc(true);
        setCloseOnOutsideClick(false);
        getElement().getStyle()
            .set("overflow", "visible")
            .set("max-height", "none");

        add(createContent(roomName));
        initializeFields();
    }

    private VerticalLayout createContent(String roomName) {
        VerticalLayout content = new VerticalLayout();
        content.setPadding(true);
        content.setSpacing(true);

        H2 header = new H2("Book Room: " + (roomName != null ? roomName : ""));
        header.getStyle().set("margin", "0 0 10px 0");

        datePicker.setWidthFull();

        HorizontalLayout timeRow = new HorizontalLayout(startTime, endTime);
        timeRow.setWidthFull();
        timeRow.setDefaultVerticalComponentAlignment(Alignment.BASELINE);

        purposeField.setPlaceholder("Brief description of the meeting");
        purposeField.setWidthFull();
        purposeField.setHeight("60px");
        purposeField.getStyle().set("resize", "none");

        Button bookButton = new Button("Book", e -> saveBooking());
        bookButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Cancel", e -> close());

        HorizontalLayout buttonRow = new HorizontalLayout(cancelButton, bookButton);
        buttonRow.setWidthFull();
        buttonRow.setJustifyContentMode(JustifyContentMode.END);

        content.add(header, datePicker, timeRow, purposeField, buttonRow);
        content.setWidth("450px");
        return content;
    }

    private void initializeFields() {
        LocalTime nowRounded = roundToNextHalfHour(LocalTime.now());

        datePicker.setValue(java.time.LocalDate.now());
        startTime.setValue(nowRounded);
        endTime.setValue(nowRounded.plusHours(1));

        startTime.setStep(Duration.ofMinutes(30));
        endTime.setStep(Duration.ofMinutes(30));
    }

    private void saveBooking() {
        try {
            if (datePicker.getValue() == null || startTime.getValue() == null || endTime.getValue() == null) {
                throw new RuntimeException("Please fill in all required fields.");
            }

            LocalDateTime start = datePicker.getValue().atTime(startTime.getValue());
            LocalDateTime end = datePicker.getValue().atTime(endTime.getValue());

            if (end.isBefore(start) || end.isEqual(start)) {
                throw new RuntimeException("End time must be after start time.");
            }

            List<MeetingRoom> availableRooms = meetingRoomService.findAvailableRoomsInTimeframe(start, end);
            boolean isAvailable = availableRooms.stream()
                    .anyMatch(room -> room.getRoomId().equals(roomId));

            if (!isAvailable) {
                throw new RuntimeException("Room is not available in the selected timeframe.");
            }

            MeetingRoom room = meetingRoomService.findRoomForEdit(roomId);

            Booking booking = new Booking();
            booking.setMeetingRoom(room);
            booking.setClient(securityService.getAuthenticatedClient()
                    .orElseThrow(() -> new RuntimeException("User not authenticated.")));
            booking.setStartTime(start);
            booking.setEndTime(end);
            booking.setPurpose(purposeField.getValue());
            booking.setBookingStatus("CONFIRMED");

            bookingService.createBooking(booking);

            Notification.show("Room booked successfully!", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            fireEvent(new BookingCreatedEvent(this));

            close();

        } catch (Exception ex) {
            Notification.show(ex.getMessage(), 5000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private LocalTime roundToNextHalfHour(LocalTime time) {
        int minutes = time.getMinute();
        if (minutes == 0) {
            return time.withSecond(0).withNano(0);
        } else if (minutes <= 30) {
            return time.withMinute(30).withSecond(0).withNano(0);
        } else {
            return time.plusHours(1).withMinute(0).withSecond(0).withNano(0);
        }
    }

    public static class BookingCreatedEvent extends ComponentEvent<RoomBookingDialog> {
        public BookingCreatedEvent(RoomBookingDialog source) {
            super(source, false);
        }
    }

    public Registration addBookingCreatedListener(ComponentEventListener<BookingCreatedEvent> listener) {
        return addListener(BookingCreatedEvent.class, listener);
    }
}
