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
    private final ComboBox<String> reminderField = new ComboBox<>("Reminder");
    private final ComboBox<Integer> attendeesField = new ComboBox<>("Nr. of Attendees");

    public RoomBookingDialog(Long roomId, String roomName, BookingService bookingService,
                             MeetingRoomService meetingRoomService, SecurityService securityService) {
        this.roomId = roomId;
        this.bookingService = bookingService;
        this.meetingRoomService = meetingRoomService;
        this.securityService = securityService;

        setWidth("600px");
        setCloseOnEsc(true);
        setCloseOnOutsideClick(false);

        add(createContent(roomName));
        initializeFields();
    }

    private VerticalLayout createContent(String roomName) {
        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);

        H2 header = new H2("Book Room: " + (roomName != null ? roomName : ""));
        header.getStyle().set("margin", "0 0 20px 0");

        HorizontalLayout dateTimeRow = new HorizontalLayout(datePicker, startTime, endTime);
        dateTimeRow.setWidthFull();

        purposeField.setPlaceholder("Brief description of the meeting");
        purposeField.setWidthFull();
        purposeField.setMinHeight("80px");

        reminderField.setItems(
                "No reminder",
                "15 min before",
                "30 min before",
                "60 min before",
                "2 hours before",
                "1 day before"
        );
        reminderField.setValue("15 min before");
        reminderField.setWidthFull();
        reminderField.setClearButtonVisible(true);

        attendeesField.setItems(1, 2, 3, 4, 5, 6, 8, 10, 12, 15, 20);
        attendeesField.setValue(1);
        attendeesField.setWidthFull();

        HorizontalLayout reminderRow = new HorizontalLayout(reminderField, attendeesField);
        reminderRow.setWidthFull();
        reminderRow.setSpacing(true);
        reminderRow.setFlexGrow(1, reminderField);
        reminderRow.setFlexGrow(1, attendeesField);

        Button bookButton = new Button("Book the room", e -> saveBooking());
        bookButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        bookButton.setWidthFull();

        Button cancelButton = new Button("Cancel", e -> close());
        cancelButton.setWidthFull();

        HorizontalLayout buttonRow = new HorizontalLayout(cancelButton, bookButton);
        buttonRow.setWidthFull();
        buttonRow.setFlexGrow(1, cancelButton);
        buttonRow.setFlexGrow(1, bookButton);

        content.add(header, dateTimeRow, purposeField, reminderRow, buttonRow);
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
            booking.setAttendees(attendeesField.getValue());
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
