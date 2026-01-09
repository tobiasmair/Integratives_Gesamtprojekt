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
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.shared.Registration;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class QuickBookingContainer extends Div {

    private final BookingService bookingService;
    private final MeetingRoomService meetingRoomService;
    private final SecurityService securityService;

    //private final Tab favTab = new Tab("Favourite rooms");
    //private final Tabs tabs = new Tabs(favTab, allTab);
    private final Tab allTab = new Tab("All available rooms");
    private final Tabs tabs = new Tabs(allTab);

    private final RadioButtonGroup<MeetingRoom> roomGroup = new RadioButtonGroup<>();

    private final DatePicker datePicker = new DatePicker("Date");
    private final TimePicker startTime = new TimePicker("Start");
    private final TimePicker endTime = new TimePicker("End");
    private final TextArea purposeField = new TextArea("Meeting purpose");
    private final ComboBox<String> reminderField = new ComboBox<String>("Reminder");
    private final ComboBox<Integer> attendeesField = new ComboBox<Integer>("Nr. of Attendees");

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

        this.title = createHeader();
        this.bookButton = createBookButton();

        //content.add(createHeader());
        content.add(title);
        content.add(createDateTimeRow());
        content.add(createRoomsSection());
        content.add(createMeetingPurposeField());
        content.add(createReminderRow());
        //content.add(createBookButton());
        content.add(bookButton);

        // Initialwerte setzen
        LocalTime nowRounded = roundToNextHalfHour(LocalTime.now());

        datePicker.setValue(java.time.LocalDate.now());

        // Nächste gerundete Stunde als Startzeit
        startTime.setValue(nowRounded);
        endTime.setValue(nowRounded.plusHours(1));

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
        return row;
    }

    private Div createRoomsSection() {
        var box = new Div();
        box.addClassName("quick-rooms-box");

        //tabs.addSelectedChangeListener(e -> onTabChanged());
        tabs.addSelectedChangeListener(e -> loadRooms());
        roomGroup.setLabel("Select a room");
        roomGroup.setWidthFull();

        box.add(tabs, roomGroup);
        return box;
    }

    /*
    private void onTabChanged() {
        if (tabs.getSelectedTab() == favTab) {
            loadFavouriteRooms();
            return;
        }
        loadAllRooms();
    }
     */

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

        if (end.isBefore(start) || end.isEqual(start)) {
            Notification.show("End time must be after start time.", 3000, Notification.Position.TOP_CENTER)
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

    /*
    private void loadFavouriteRooms() {
        setRooms(java.util.List.of(
                new RoomItem("A", "Meeting Room A", "Up to 90", true),
                new RoomItem("B", "Meeting Room B", "Up to 12", true),
                new RoomItem("C", "Meeting Room C", "Up to 20", true)
        ));
    }

    private void loadAllRooms() {
        setRooms(java.util.List.of(
                new RoomItem("A", "Meeting Room A", "Up to 90", true),
                new RoomItem("D", "Lecture Room D", "Up to 120", false),
                new RoomItem("E", "Focus Room E", "Up to 4", false)
        ));
    }

    private void setRooms(List<RoomItem> rooms) {
        roomGroup.setItems(rooms);
        roomGroup.setValue(rooms.isEmpty() ? null : rooms.getFirst());
    }

    private HorizontalLayout createRoomRow(RoomItem room) {
        var title = new Span(room.name());
        title.addClassName("room-title");

        var cap = new Span(room.capacity());
        cap.addClassName("room-capacity");

        var text = new VerticalLayout(title, cap);
        text.setPadding(false);
        text.setSpacing(false);

        Icon star = room.favourite() ? VaadinIcon.STAR.create() : VaadinIcon.STAR_O.create();
        star.addClassName("room-star");

        var row = new HorizontalLayout(text, star);
        row.addClassName("room-row");
        row.setWidthFull();
        row.setAlignItems(FlexComponent.Alignment.CENTER);
        row.expand(text);

        return row;
    }
     */

    private void setupRoomGroup() {
        roomGroup.setLabel(null);
        roomGroup.addClassName("rooms-radio");
        roomGroup.setRenderer(new ComponentRenderer<>(room -> {
            var title = new Span(room.getName());

            var cap = new Span("Capacity: " + room.getCapacity());

            var text = new VerticalLayout(title, cap);
            text.setPadding(false);
            text.setSpacing(false);

            var row = new HorizontalLayout(text);
            row.setWidthFull();
            row.setAlignItems(FlexComponent.Alignment.CENTER);
            row.expand(text);

            return row;
        }));
    }

    private TextArea createMeetingPurposeField() {
        purposeField.setPlaceholder("Brief description of the meeting");
        purposeField.setWidthFull();
        purposeField.setMinHeight("80px");
        return purposeField;
    }

    private ComboBox<String> createReminderField() {
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
        return reminderField;
    }

    private ComboBox<Integer> createAttendeesField() {
        attendeesField.setItems(1, 2, 3, 4, 5, 6, 8, 10, 12, 15, 20);
        attendeesField.setValue(1);
        attendeesField.setWidthFull();
        return attendeesField;
    }

    private HorizontalLayout createReminderRow() {
        var reminder = createReminderField();
        var attendees = createAttendeesField();

        var row = new HorizontalLayout(reminder, attendees);
        row.setWidthFull();
        row.setSpacing(true);
        row.setFlexGrow(1, reminder);
        row.setFlexGrow(1, attendees);

        return row;
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

            booking.setAttendees(attendeesField.getValue());

            booking.setBookingStatus("CONFIRMED");  // status

            bookingService.createBooking(booking);

            Notification.show("Room booked successfully!", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            // Event feuern
            fireEvent(new BookingChangedEvent(this));

            // Formular zurücksetzen
            LocalTime nowRounded = roundToNextHalfHour(LocalTime.now());
            startTime.setValue(nowRounded);
            endTime.setValue(nowRounded.plusHours(1));
            purposeField.clear();
            reminderField.setValue("15 min before");
            attendeesField.setValue(1);
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
        attendeesField.setValue(booking.getAttendees() != null ? booking.getAttendees() : 1);

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
        b.setAttendees(attendeesField.getValue());
        b.setMeetingRoom(roomGroup.getValue());
    }

}
