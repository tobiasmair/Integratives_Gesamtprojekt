package com.gesamtprojekt.application.ui.components.dashboard;

import com.gesamtprojekt.application.exceptions.MissingStartExitException;
import com.gesamtprojekt.application.model.Booking;
import com.gesamtprojekt.application.model.Exit;
import com.gesamtprojekt.application.model.MeetingRoom;
import com.gesamtprojekt.application.security.SecurityService;
import com.gesamtprojekt.application.service.implementation.BookingService;
import com.gesamtprojekt.application.service.implementation.DefaultNavigationService;
import com.gesamtprojekt.application.service.implementation.ExitService;
import com.gesamtprojekt.application.service.implementation.MeetingRoomService;
import com.gesamtprojekt.application.util.BookingValidator;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
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
import java.util.Optional;

public class QuickBookingContainer extends Div {

    private final BookingService bookingService;
    private final MeetingRoomService meetingRoomService;
    private final SecurityService securityService;
    private final ExitService exitService;
    private final DefaultNavigationService defaultNavigationService;

    private final Tab allTab = new Tab("All available rooms");
    private final Tabs tabs = new Tabs(allTab);

    private final RadioButtonGroup<MeetingRoom> roomGroup = new RadioButtonGroup<>();

    private final DatePicker datePicker = new DatePicker("Date");
    private final TimePicker startTime = new TimePicker("Start");
    private final TimePicker endTime = new TimePicker("End");
    private final TextArea purposeField = new TextArea("Meeting purpose");

    private final ComboBox<Exit> exitDropdown = new ComboBox<>("Select Exit");

    private H3 title;
    private Button bookButton;

    private Long currentEditingBookingId = null;

    private LocalTime opensAt = BookingValidator.OPENS_AT;
    private LocalTime closesAt = BookingValidator.CLOSES_AT;

    private final Span travelTimeInfo = new Span();

    private final TextField roomSearchField = new TextField("Search room number");
    private List<MeetingRoom> allAvailableRooms = new java.util.ArrayList<>();

    public QuickBookingContainer(BookingService bookingService, MeetingRoomService meetingRoomService, SecurityService securityService, ExitService exitService, DefaultNavigationService defaultNavigationService) {
        this.bookingService = bookingService;
        this.meetingRoomService = meetingRoomService;
        this.securityService = securityService;
        this.exitService = exitService;
        this.defaultNavigationService = defaultNavigationService;

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
        content.getStyle().set("overflow", "hidden");

        exitDropdown.setVisible(false);
        exitDropdown.setWidthFull();
        exitDropdown.setPlaceholder("Select your starting exit");
        exitDropdown.setItemLabelGenerator(exit ->
                exit.getBuilding().getName() + " - " + exit.getName()
        );

        this.title = createHeader();
        this.bookButton = createBookButton();

        var dateTimeRow = createDateTimeRow();
        var roomsSection = createRoomsSection();
        var purposeField = createMeetingPurposeField();

        // Navigation
        travelTimeInfo.getStyle()
                .set("font-size", "var(--lumo-font-size-s)")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("margin-top", "4px");
        travelTimeInfo.setVisible(false);

        content.add(title);
        content.add(dateTimeRow);
        content.add(roomsSection);
        content.add(exitDropdown);
        content.add(travelTimeInfo);
        content.add(purposeField);
        content.add(bookButton);

        content.setFlexGrow(0, title);
        content.setFlexGrow(0, dateTimeRow);
        content.setFlexGrow(1, roomsSection);
        content.setFlexGrow(0, purposeField);
        content.setFlexGrow(0, bookButton);

        // Initialwerte setzen
        LocalTime nowRounded = BookingValidator.roundToNextHalfHour(LocalTime.now());
        nowRounded = BookingValidator.clampToOpeningHours(nowRounded);

        datePicker.setValue(LocalDate.now());
        datePicker.setMin(LocalDate.now());

        // Einschränkungen basierend auf Öffnungszeiten
        startTime.setMin(opensAt);
        startTime.setMax(closesAt.minusMinutes(30));
        endTime.setMin(opensAt.plusMinutes(30));
        endTime.setMax(closesAt);

        startTime.setValue(nowRounded);

        // End Time setzen: Start + 1 Stunde, maximal Schließzeit
        LocalTime endTimeCalculated = nowRounded.plusHours(1);
        if (endTimeCalculated.isAfter(closesAt) || endTimeCalculated.isBefore(nowRounded)) {
            endTimeCalculated = closesAt;
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

        // Suchfeld konfigurieren
        roomSearchField.setPlaceholder("e.g. 301...");
        roomSearchField.setClearButtonVisible(true);
        roomSearchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        roomSearchField.setWidthFull();
        roomSearchField.setValueChangeMode(com.vaadin.flow.data.value.ValueChangeMode.EAGER);
        roomSearchField.addValueChangeListener(e -> filterRooms());

        //tabs.addSelectedChangeListener(e -> onTabChanged());
        tabs.addSelectedChangeListener(e -> loadRooms());
        roomGroup.setLabel("Select a room");
        roomGroup.setWidthFull();

        Scroller scroller = new Scroller(roomGroup);
        scroller.setScrollDirection(Scroller.ScrollDirection.VERTICAL);
        scroller.setWidthFull();
        scroller.getStyle().set("flex-grow", "1");

        box.add(tabs, roomSearchField, scroller);
        return box;
    }

    // Wenn sich Datum ändert -> lade die Räume neu
    private void setupEventListeners() {
        datePicker.addValueChangeListener(e -> {
            loadRooms();
            updateExitVisibility();
        });

        startTime.addValueChangeListener(e -> {
            if (startTime.getValue() == null) return;
            LocalTime s = BookingValidator.clampToOpeningHours(startTime.getValue());
            if (!s.equals(startTime.getValue())) {
                startTime.setValue(s);
            }
            LocalTime proposedEnd = s.plusHours(1);
            if (proposedEnd.isAfter(closesAt)) {
                proposedEnd = closesAt;
            }
            endTime.setValue(proposedEnd);

            loadRooms();
            updateExitVisibility();
        });

        endTime.addValueChangeListener(e -> loadRooms());

        // Reisezeitberechnung
        exitDropdown.addValueChangeListener(event -> {
            Exit selectedExit = event.getValue();
            MeetingRoom selectedRoom = roomGroup.getValue();

            if (selectedExit == null || selectedRoom == null) {
                travelTimeInfo.setVisible(false);
                return;
            }

            try {
                int totalSeconds = defaultNavigationService.calculateTravelTime(selectedExit, selectedRoom);

                if (totalSeconds < Integer.MAX_VALUE) {
                    int minutes = (int) Math.ceil(totalSeconds / 60.0);

                    // Ankunftszeit berechnen
                    LocalTime arrivalTime = LocalTime.now().plusMinutes(minutes);

                    travelTimeInfo.setText(String.format(
                            "⏱ Estimated walk: %d min | Estimated arrival: %s",
                            minutes,
                            arrivalTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
                    ));

                    // Farbe je nach Zeitpunkt (Rot = zu spät)
                    LocalTime meetingStart = startTime.getValue();
                    if (arrivalTime.isAfter(meetingStart)) {
                        travelTimeInfo.getStyle().set("color", "var(--lumo-error-text-color)");
                        travelTimeInfo.setText(travelTimeInfo.getText() + " (Late!)");
                    } else {
                        travelTimeInfo.getStyle().set("color", "var(--lumo-success-text-color)");
                    }

                    travelTimeInfo.setVisible(true);
                } else {
                    travelTimeInfo.setText("No travel path found.");
                    travelTimeInfo.getStyle().set("color", "var(--lumo-error-text-color)");
                    travelTimeInfo.setVisible(true);
                }
            } catch (Exception ex) {
                travelTimeInfo.setVisible(false);
            }
        });
    }

    private void updateExitVisibility() {
        // im Edit Modus
        if (currentEditingBookingId != null && exitDropdown.isReadOnly()) {
            return;
        }

        if (datePicker.getValue() == null || startTime.getValue() == null) {
            exitDropdown.setVisible(false);
            return;
        }

        LocalDateTime start = datePicker.getValue().atTime(startTime.getValue());
        // Sichtbar bei Meeting in unter 60 Minuten
        boolean lessThan60 = start.isBefore(LocalDateTime.now().plusMinutes(60));

        if (lessThan60) {
            if (exitDropdown.getListDataView().getItems().count() == 0) {
                exitDropdown.setItems(exitService.getAllExits());
            }
            exitDropdown.setVisible(true);
        } else {
            exitDropdown.setVisible(false);
            exitDropdown.clear();
        }

        if (!exitDropdown.isVisible()) {
            travelTimeInfo.setVisible(false);
        }
    }

    // Nur freie Räume laden
    public void loadRooms() {
        if (datePicker.getValue() == null || startTime.getValue() == null || endTime.getValue() == null) {
            return;
        }

        LocalDateTime start = datePicker.getValue().atTime(startTime.getValue());
        LocalDateTime end = datePicker.getValue().atTime(endTime.getValue());
        LocalDateTime now = LocalDateTime.now();

        // Validierung über die Utility-Klasse
        boolean isValid = BookingValidator.isTimeRangeValid(start, end, currentEditingBookingId != null);

        if (!isValid) {
            allAvailableRooms = List.of();
            roomGroup.setItems(List.of());
            return;
        }

        if (currentEditingBookingId != null) {
            allAvailableRooms = meetingRoomService.findAvailableRoomsExcludingBooking(start, end, currentEditingBookingId);
        } else {
            allAvailableRooms = meetingRoomService.findAvailableRoomsInTimeframe(start, end);
        }

        filterRooms();
    }

    private void filterRooms() {
        String filter = roomSearchField.getValue().trim().toLowerCase();

        List<MeetingRoom> filteredRooms = allAvailableRooms.stream()
                .filter(room -> filter.isEmpty() || room.getName().toLowerCase().contains(filter))
                .toList();

        MeetingRoom selectedRoom = roomGroup.getValue();
        roomGroup.setItems(filteredRooms);

        if (selectedRoom != null && filteredRooms.contains(selectedRoom)) {
            roomGroup.setValue(selectedRoom);
        } else if (!filteredRooms.isEmpty() && filter.isEmpty()) {
            roomGroup.setValue(filteredRooms.get(0));
        }
    }

    private void setupRoomGroup() {
        roomGroup.setLabel(null);
        roomGroup.addClassName("rooms-radio");
        //roomGroup.setWidthFull();

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

            //row.getStyle().set("flex-shrink", "0");

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

            // Validierung, ob Buchung kurzfristig ist und ggf. Start-Exit benötigt wird
            try {
                bookingService.createBooking(
                        booking,
                        Optional.ofNullable(exitDropdown.getValue()).map(Exit::getId)
                );

                Notification.show("Room booked successfully!", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            } catch (MissingStartExitException ex) {

                exitDropdown.setItems(ex.getAvailableExits());
                exitDropdown.setVisible(true);

                Notification.show(
                        "This booking starts soon. Please select your starting exit.",
                        5000,
                        Notification.Position.TOP_CENTER
                ).addThemeVariants(NotificationVariant.LUMO_WARNING);

                return; // Buchung abbrechen

            } catch (Exception ex) {
                Notification.show(ex.getMessage(), 5000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }


            Notification.show("Room booked successfully!", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            // Event feuern
            fireEvent(new BookingChangedEvent(this));

            // Formular zurücksetzen
            LocalTime nowRounded = BookingValidator.roundToNextHalfHour(LocalTime.now());
            nowRounded = BookingValidator.clampToOpeningHours(nowRounded);

            startTime.setValue(nowRounded);

            // Endzeit berechnen: Start + 1 Stunde, aber maximal closesAt
            LocalTime endTimeReset = nowRounded.plusHours(1);
            if (endTimeReset.isAfter(closesAt) || endTimeReset.isBefore(nowRounded)) {
                endTimeReset = closesAt;
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

        // Exit Logik im Edit Modus
        if (booking.getStartExit() != null) {
            // gespeicherten Exit im Dropdown setzen
            exitDropdown.setItems(List.of(booking.getStartExit()));
            exitDropdown.setValue(booking.getStartExit());
            exitDropdown.setVisible(true);
            exitDropdown.setReadOnly(true); // Verhindert Änderungen

            // Reisezeit-Info für den alten Exit anzeigen
            updateTravelTimeDisplay(booking.getStartExit(), booking.getMeetingRoom());
        }
    }

    private void updateTravelTimeDisplay(Exit selectedExit, MeetingRoom selectedRoom) {
        if (selectedExit == null || selectedRoom == null) {
            travelTimeInfo.setVisible(false);
            return;
        }

        try {
            int totalSeconds = defaultNavigationService.calculateTravelTime(selectedExit, selectedRoom);
            if (totalSeconds < Integer.MAX_VALUE) {
                int minutes = (int) Math.ceil(totalSeconds / 60.0);
                LocalTime arrivalTime = LocalTime.now().plusMinutes(minutes);

                travelTimeInfo.setText(String.format(
                        "⏱ Estimated walk: %d min | Estimated arrival: %s",
                        minutes,
                        arrivalTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
                ));

                // Logik für Farbe (Rot wenn zu spät)
                LocalTime meetingStart = startTime.getValue();
                if (arrivalTime.isAfter(meetingStart)) {
                    travelTimeInfo.getStyle().set("color", "var(--lumo-error-text-color)");
                } else {
                    travelTimeInfo.getStyle().set("color", "var(--lumo-success-text-color)");
                }
                travelTimeInfo.setVisible(true);
            }
        } catch (Exception ignored) {
            travelTimeInfo.setVisible(false);
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
