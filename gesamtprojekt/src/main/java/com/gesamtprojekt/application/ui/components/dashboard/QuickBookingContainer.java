package com.gesamtprojekt.application.ui.components.dashboard;


import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.button.Button;



import java.time.Duration;
import java.time.LocalTime;
import java.util.List;

public class QuickBookingContainer extends Div {

    private final Tab favTab = new Tab("Favourite rooms");
    private final Tab allTab = new Tab("All available rooms");
    private final Tabs tabs = new Tabs(favTab, allTab);

    private final RadioButtonGroup<RoomItem> roomGroup = new RadioButtonGroup<>();

    private record RoomItem(String id, String name, String capacity, boolean favourite) {}

    public QuickBookingContainer() {
        addClassName("quick-booking-container");
        add(createContent());
        setupRoomGroup();
        loadFavouriteRooms();
    }

    private VerticalLayout createContent() {
        var content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(false);
        content.setWidthFull();

        content.add(createHeader());
        content.add(createDateTimeRow());
        content.add(createRoomsSection());
        content.add(createMeetingPurposeField());
        content.add(createReminderRow());
        content.add(createBookButton());
        return content;
    }

    private H3 createHeader() {
        var title = new H3("Quick book");
        title.getStyle().set("margin", "0");
        return title;
    }

    private HorizontalLayout createDateTimeRow() {
        var datePicker = new DatePicker("Date");
        var start = createStartTimePicker();
        var end = createEndTimePicker();
        var row = new HorizontalLayout(datePicker, start, end);
        row.setWidthFull();
        row.setSpacing(true);

        return row;
    }

    private TimePicker createStartTimePicker() {
        TimePicker timePicker = new TimePicker();
        timePicker.setLabel("Start");
        timePicker.setStep(Duration.ofMinutes(30));
        timePicker.setValue(LocalTime.of(12, 30));
        //dd(timePicker);
        return timePicker;
    }

    private TimePicker createEndTimePicker() {
        TimePicker timePicker = new TimePicker();
        timePicker.setLabel("End");
        timePicker.setStep(Duration.ofMinutes(30));
        timePicker.setValue(LocalTime.of(12, 30));
        //add(timePicker);
        return timePicker;
    }

    private Div createRoomsSection() {
        var box = new Div();
        box.addClassName("quick-rooms-box");

        tabs.addSelectedChangeListener(e -> onTabChanged());
        roomGroup.setLabel("Select a room");
        roomGroup.setWidthFull();

        box.add(tabs, roomGroup);
        return box;
    }

    private void onTabChanged() {
        if (tabs.getSelectedTab() == favTab) {
            loadFavouriteRooms();
            return;
        }
        loadAllRooms();
    }

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

    private void setupRoomGroup() {
        roomGroup.setLabel(null);
        roomGroup.addClassName("rooms-radio");
        roomGroup.setRenderer(new ComponentRenderer<>(this::createRoomRow));
    }

    private TextArea createMeetingPurposeField() {
        var purpose = new TextArea("Meeting purpose");
        purpose.setPlaceholder("Brief description of the meeting");
        purpose.setWidthFull();
        purpose.setMinHeight("80px");
        return purpose;
    }

    private ComboBox<String> createReminderField() {
        var reminder = new ComboBox<String>("Reminder");
        reminder.setItems(
                "No reminder",
                "15 min before",
                "30 min before",
                "60 min before",
                "2 hours before",
                "1 day before"
        );
        reminder.setValue("15 min before");
        reminder.setWidthFull();
        reminder.setClearButtonVisible(true);
        return reminder;
    }

    private ComboBox<Integer> createAttendeesField() {
        var attendees = new ComboBox<Integer>("Nr. of Attendees");
        attendees.setItems(1, 2, 3, 4, 5, 6, 8, 10, 12, 15, 20);
        attendees.setValue(1);
        attendees.setWidthFull();
        return attendees;
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
        return btn;
    }

}
