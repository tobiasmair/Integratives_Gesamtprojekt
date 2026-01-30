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
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.shared.Registration;

import java.time.Duration;
import java.time.LocalDate;
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
        this(roomId, roomName, bookingService, meetingRoomService, securityService, null, null);
    }

    public RoomBookingDialog(Long roomId, String roomName, BookingService bookingService,
                             MeetingRoomService meetingRoomService, SecurityService securityService,
                             LocalDateTime startDateTime, LocalDateTime endDateTime) {
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
        initializeFields(startDateTime, endDateTime);
    }

    private VerticalLayout createContent(String roomName) {
        VerticalLayout content = new VerticalLayout();
        content.setPadding(true);
        content.setSpacing(true);

        H2 header = new H2("Book Room: " + (roomName != null ? roomName : ""));
        header.getStyle().set("margin", "0 0 10px 0");

        // Lageplan Link
        MeetingRoom room = meetingRoomService.findRoomForEdit(roomId);
        Button mapLink = new Button("Show Floor Plan");
        mapLink.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        mapLink.getStyle().set("font-size", "var(--lumo-font-size-xs)");
        Icon mapIcon = VaadinIcon.MAP_MARKER.create();
        mapIcon.setSize("12px");
        HorizontalLayout mapWrapper = new HorizontalLayout(mapIcon, mapLink);
        mapWrapper.setSpacing(false);
        mapWrapper.setAlignItems(HorizontalLayout.Alignment.CENTER);
        mapWrapper.addClickListener(e -> {
            if (room == null) {
                Notification.show("No floor plan available");
                return;
            }

            // Dialog erstellen
            Dialog dialog = new Dialog();
            dialog.setHeaderTitle("Floor Plan: " + room.getName());

            Image floorPlan = buildBlueprint(room.getName());

            // Layout im Dialog
            VerticalLayout dialogLayout = new VerticalLayout(floorPlan);
            dialogLayout.setPadding(false);
            dialogLayout.setAlignItems(FlexComponent.Alignment.CENTER);

            dialog.add(dialogLayout);

            // Schließen-Button im Footer
            Button closeButtonWrapper = new Button("Close", event -> dialog.close());
            dialog.getFooter().add(closeButtonWrapper);

            dialog.open();
        });

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

        content.add(header, mapWrapper, datePicker, timeRow, purposeField, buttonRow);
        content.setWidth("450px");
        return content;
    }

    private void initializeFields(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        // Öffnungszeiten setzen
        LocalTime opensAt = LocalTime.of(7, 0);
        LocalTime closesAt = LocalTime.of(22, 0);

        startTime.setMin(opensAt);
        startTime.setMax(closesAt.minusMinutes(30));

        endTime.setMin(opensAt.plusMinutes(30));
        endTime.setMax(closesAt);

        startTime.setStep(Duration.ofMinutes(30));
        endTime.setStep(Duration.ofMinutes(30));

        // Initialisierung Werte
        if (startDateTime != null) {
            datePicker.setValue(startDateTime.toLocalDate());
            startTime.setValue(startDateTime.toLocalTime());
            endTime.setValue(endDateTime.toLocalTime());
        } else {
            LocalTime now = roundToNextHalfHour(LocalTime.now());
            // vor 7 auf 7 setzen
            if (now.isBefore(opensAt)) now = opensAt;
            // nach 22 setze auf morgen 7 Uhr
            if (now.isAfter(closesAt.minusHours(1))) now = opensAt;

            datePicker.setValue(LocalDate.now());
            startTime.setValue(now);
            endTime.setValue(now.plusHours(1));
        }
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
