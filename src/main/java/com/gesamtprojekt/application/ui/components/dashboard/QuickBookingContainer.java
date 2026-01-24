package com.gesamtprojekt.application.ui.components.dashboard;

import com.gesamtprojekt.application.model.Booking;
import com.gesamtprojekt.application.model.MeetingRoom;
import com.gesamtprojekt.application.security.SecurityService;
import com.gesamtprojekt.application.service.implementation.BookingService;
import com.gesamtprojekt.application.service.implementation.MeetingRoomService;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.timepicker.TimePicker;

import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.component.orderedlayout.Scroller;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class QuickBookingContainer extends Div {

    private final BookingService bookingService;
    private final MeetingRoomService meetingRoomService;
    private final SecurityService securityService;

    private final Tab allTab = new Tab("All available rooms");
    private final Tabs tabs = new Tabs(allTab);

    private final RadioButtonGroup<MeetingRoom> roomGroup = new RadioButtonGroup<>();

    private final DatePicker datePicker = new DatePicker("Date");
    private final TimePicker startTime = new TimePicker("Start");
    private final TimePicker endTime = new TimePicker("End");
    private final TextArea purposeField = new TextArea("Meeting purpose");

    private H3 title;
    private Button bookButton;

    private Long currentEditingBookingId = null;

    public QuickBookingContainer(BookingService bookingService, MeetingRoomService meetingRoomService, SecurityService securityService) {
        this.bookingService = bookingService;
        this.meetingRoomService = meetingRoomService;
        this.securityService = securityService;

        addClassName("quick-booking-container");
        add(createContent());
        setupRoomGroup();
        setupEventListeners();
        loadRooms();
    }

    private VerticalLayout createContent() {
        var content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(false);
        content.setWidthFull();
        content.setHeightFull();

        this.title = createHeader();
        this.bookButton = createBookButton();

        var dateTimeRow = createDateTimeRow();
        var roomsSection = createRoomsSection();
        var purposeField = createMeetingPurposeField();

        content.add(title);
        content.add(dateTimeRow);
        content.add(roomsSection);
        content.add(purposeField);
        content.add(bookButton);

        content.setFlexGrow(0, title);
        content.setFlexGrow(0, dateTimeRow);
        content.setFlexGrow(1, roomsSection);
        content.setFlexGrow(0, purposeField);
        content.setFlexGrow(0, bookButton);

        // Initialwerte setzen
        LocalTime nowRounded = roundToNextHalfHour(LocalTime.now());

        datePicker.setValue(java.time.LocalDate.now());
        datePicker.setMin(LocalDate.now());

        // Nächste gerundete Stunde als Startzeit
        startTime.setValue(nowRounded);

        // End Time: 1 Stunde später, aber maximal 23:30 (um Mitternachts-Problem zu vermeiden)
        LocalTime endTimeCalculated = nowRounded.plusHours(1);
        if (endTimeCalculated.isBefore(nowRounded) || endTimeCalculated.equals(LocalTime.MIDNIGHT)) {
            // Über Mitternacht - setze auf 23:30 statt
            endTimeCalculated = LocalTime.of(23, 30);
        }
        endTime.setValue(endTimeCalculated);

        startTime.setStep(Duration.ofMinutes(30));
        endTime.setStep(Duration.ofMinutes(30));

        return content;
    }

    private H3 createHeader() {
        var title = new H3("Quick book");
        title.getStyle().set("margin", "0");
        return title;
    }

    private HorizontalLayout createDateTimeRow() {
        var row = new HorizontalLayout(datePicker, startTime, endTime);
        row.setWidthFull();
        // Responsive untereinander
        row.getStyle().set("flex-wrap", "wrap");

        // volle breite auf mobilen Geräten
        datePicker.getStyle().set("min-width", "120px").set("flex", "1");
        startTime.getStyle().set("min-width", "100px").set("flex", "1");
        endTime.getStyle().set("min-width", "100px").set("flex", "1");

        return row;
    }

    private Div createRoomsSection() {
        var box = new Div();
        box.addClassName("quick-rooms-box");
        box.getStyle()
            .set("display", "flex")
            .set("flex-direction", "column")
            .set("flex-grow", "1")
            .set("overflow", "hidden");

        //tabs.addSelectedChangeListener(e -> onTabChanged());
        tabs.addSelectedChangeListener(e -> loadRooms());
        roomGroup.setLabel("Select a room");
        roomGroup.setWidthFull();

        Scroller scroller = new Scroller(roomGroup);
        scroller.setScrollDirection(Scroller.ScrollDirection.VERTICAL);
        scroller.setWidthFull();
        scroller.getStyle().set("flex-grow", "1");

        box.add(tabs, scroller);
        return box;
    }

    // Wenn sich Datum ändert -> lade die Räume neu
    private void setupEventListeners() {
        datePicker.addValueChangeListener(e -> loadRooms());
        startTime.addValueChangeListener(e -> loadRooms());
        endTime.addValueChangeListener(e -> loadRooms());
    }

    // Nur freie Räume laden
    public void loadRooms() {
        if (datePicker.getValue() == null || startTime.getValue() == null || endTime.getValue() == null) {
            return;
        }

        LocalDateTime start = datePicker.getValue().atTime(startTime.getValue());
        LocalDateTime end = datePicker.getValue().atTime(endTime.getValue());
        LocalDateTime now = LocalDateTime.now();

        if (end.isBefore(start) || end.isEqual(start)) {
            Notification.show("End time must be after start time.", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            roomGroup.setItems(List.of());
            return;
        }

        if (start.isBefore(now)) {
            Notification.show("Start time must be in the future.", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            roomGroup.setItems(List.of());
            return;
        }

        List<MeetingRoom> availableRooms = meetingRoomService.findAvailableRoomsInTimeframe(start, end);

        roomGroup.setItems(availableRooms);
        if (!availableRooms.isEmpty()) {
            roomGroup.setValue(availableRooms.get(0));
        }
    }

    private void setupRoomGroup() {
        roomGroup.setLabel(null);
        roomGroup.addClassName("rooms-radio");
        roomGroup.setRenderer(new ComponentRenderer<>(room -> {
            Span roomName = new Span(room.getName());

            Span locationInfo = new Span(room.getLocation() + " | Floor " + room.getFloor());
            locationInfo.getStyle().set("margin-left", "auto");

            HorizontalLayout topRow = new HorizontalLayout(roomName, locationInfo);
            topRow.setWidthFull();
            topRow.setPadding(false);

            var cap = new Span("Capacity: " + room.getCapacity());

            VerticalLayout container = new VerticalLayout(topRow, cap);
            container.setPadding(false);
            container.setSpacing(false);
            container.setWidthFull();

            HorizontalLayout row = new HorizontalLayout(container);
            row.setWidthFull();
            row.setAlignItems(FlexComponent.Alignment.CENTER);

            return row;
        }));
    }

    private TextArea createMeetingPurposeField() {
        purposeField.setPlaceholder("Brief description of the meeting");
        purposeField.setWidthFull();
        purposeField.setMinHeight("80px");
        return purposeField;
    }

    private Button createBookButton() {
        var btn = new Button("Book the room");
        btn.setWidthFull();
        btn.addClassName("book-room-button");

        btn.addClickListener(e -> {
            saveBooking();
        });

        return btn;
    }

    // Buchung in DB speichern
    private void saveBooking() {
        try {
            if (roomGroup.getValue() == null || datePicker.getValue() == null) {
                throw new RuntimeException("Please select a room and a date.");
            }

            Booking booking = new Booking();
            booking.setMeetingRoom(roomGroup.getValue());   // RoomId
            booking.setClient(securityService.getAuthenticatedClient().orElseThrow());  // ClientId

            // Umwandlung in LocalDateTime
            booking.setStartTime(datePicker.getValue().atTime(startTime.getValue()));   // startTime
            booking.setEndTime(datePicker.getValue().atTime(endTime.getValue()));   // endTime

            booking.setPurpose(purposeField.getValue());  // purpose

            booking.setBookingStatus("CONFIRMED");  // status

            bookingService.createBooking(booking);

            Notification.show("Room booked successfully!", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            // Event feuern
            fireEvent(new BookingChangedEvent(this));

            // Formular zurücksetzen
            LocalTime nowRounded = roundToNextHalfHour(LocalTime.now());
            startTime.setValue(nowRounded);

            // End Time: 1 Stunde später, aber maximal 23:30 (um Mitternachts-Problem zu vermeiden)
            LocalTime endTimeReset = nowRounded.plusHours(1);
            if (endTimeReset.isBefore(nowRounded) || endTimeReset.equals(LocalTime.MIDNIGHT)) {
                endTimeReset = LocalTime.of(23, 30);
            }
            endTime.setValue(endTimeReset);

            purposeField.clear();
            loadRooms();

        } catch (Exception ex) {
            Notification.show(ex.getMessage(), 5000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    // Zeit auf nächste halbe Stunde aufrunden
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

    // View registrieren
    public Registration addBookingChangedListener(ComponentEventListener<BookingChangedEvent> listener) {
        return addListener(BookingChangedEvent.class, listener);
    }


    /*
    Edit-Modus Abschnitt
     */
    public void setEditMode(boolean isEditMode) {
        if (bookButton != null) {
            bookButton.setVisible(!isEditMode);
            title.setVisible(!isEditMode);
        }
    }

    // Felder befüllen mit Buchungsdaten
    public void setBooking(Booking booking) {
        this.currentEditingBookingId = booking.getBookingId();

        // Felder befüllen
        datePicker.setValue(booking.getStartTime().toLocalDate());
        startTime.setValue(booking.getStartTime().toLocalTime());
        endTime.setValue(booking.getEndTime().toLocalTime());
        purposeField.setValue(booking.getPurpose() != null ? booking.getPurpose() : "");

        // Räume laden (aktuellen Raum berücksichtigen)
        List<MeetingRoom> availableRooms = loadAvailableRooms();

        // Aktuellen Raum auswählen
        if (booking.getMeetingRoom() != null) {
            availableRooms.stream()
                    .filter(room -> room.getRoomId().equals(booking.getMeetingRoom().getRoomId()))
                    .findFirst()
                    .ifPresent(roomGroup::setValue);
        }
    }

    // Räume laden mit findAvailableRoomsExcludingBooking
    private List<MeetingRoom> loadAvailableRooms() {
        LocalDateTime start = datePicker.getValue().atTime(startTime.getValue());
        LocalDateTime end = datePicker.getValue().atTime(endTime.getValue());

        List<MeetingRoom> rooms = meetingRoomService.findAvailableRoomsExcludingBooking(
                start, end, currentEditingBookingId);

        roomGroup.setItems(rooms);
        return rooms;
    }

    // Buchungsobjekt mit Werten aus Formular updaten
    public void updateBookingObject(Booking b) {
        b.setPurpose(purposeField.getValue());
        b.setStartTime(datePicker.getValue().atTime(startTime.getValue()));
        b.setEndTime(datePicker.getValue().atTime(endTime.getValue()));
        b.setMeetingRoom(roomGroup.getValue());
    }

}
