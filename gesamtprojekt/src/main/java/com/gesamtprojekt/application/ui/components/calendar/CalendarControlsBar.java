package com.gesamtprojekt.application.ui.components.calendar;

import com.gesamtprojekt.application.ui.components.dashboard.BookingChangedEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.shared.Registration;
import lombok.Getter;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class CalendarControlsBar extends VerticalLayout {

    private DatePicker date = new DatePicker("Date");
    private TimePicker start;
    private TimePicker end;
    private ComboBox<String> building = new ComboBox<>("Building");
    private ComboBox<String> floor = new ComboBox<>("Floor");
    private ComboBox<String> capacity = new ComboBox<>("Min Capacity");
    private ComboBox<String> equipment = new ComboBox<>("Equipment");

    public CalendarControlsBar() {
        setWidthFull();
        setPadding(false);
        setSpacing(true);
        addClassName("calendar-controls");

        add(buildTopRow());
        add(buildFiltersRow());

        addValueChangeListeners();
    }

    // Getter
    public LocalDateTime getStartDateTime() { return date.getValue().atTime(start.getValue()); }
    public LocalDateTime getEndDateTime() { return date.getValue().atTime(end.getValue()); }
    public String getBuilding() { return building.getValue(); }
    public String getFloor() { return floor.getValue(); }
    public String getCapacity() { return capacity.getValue(); }

    private void addValueChangeListeners() {
        date.addValueChangeListener(e -> fireEvent(new FilterChangedEvent(this)));
        start.addValueChangeListener(e -> fireEvent(new FilterChangedEvent(this)));
        end.addValueChangeListener(e -> fireEvent(new FilterChangedEvent(this)));
        building.addValueChangeListener(e -> fireEvent(new FilterChangedEvent(this)));
        floor.addValueChangeListener(e -> fireEvent(new FilterChangedEvent(this)));
        capacity.addValueChangeListener(e -> fireEvent(new FilterChangedEvent(this)));
    }

    private FormLayout buildTopRow() {
        LocalTime nowRounded = roundToNextHalfHour(LocalTime.now());

        // Nächste gerundete Stunde als Startzeit
        start = time("Start Time", nowRounded);
        end = time("End Time", nowRounded.plusHours(1));

        start.setStep(Duration.ofMinutes(30));
        end.setStep(Duration.ofMinutes(30));

        date.setValue(LocalDate.now());
        date.setWidthFull();

        FormLayout top = new FormLayout(date, start, end);
        top.setWidthFull();
        top.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("650px", 3)
        );
        return top;
    }

    private FormLayout buildFiltersRow() {
        building = combo("Building", List.of("All Buildings", "MCI I", "MCI II", "MCI III"));
        floor = combo("Floor", List.of("Any Floor", "1", "2", "3"));
        capacity = combo("Min Capacity", List.of("Any", "5+", "10+", "20+", "50+"));
        equipment = combo("Equipment", List.of("Any", "Wifi", "Whiteboard", "Smart TV"));

        //ComboBox<String> fav = combo("Favourites", List.of("All rooms", "Only favourites"));

        FormLayout filters = new FormLayout(building, floor, capacity, equipment);
        filters.setWidthFull();
        filters.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("650px", 3),
                new FormLayout.ResponsiveStep("950px", 5)
        );
        return filters;
    }

    private TimePicker time(String label, LocalTime value) {
        TimePicker tp = new TimePicker(label);
        tp.setValue(value);
        tp.setWidthFull();
        return tp;
    }

    private ComboBox<String> combo(String label, List<String> items) {
        ComboBox<String> cb = new ComboBox<>(label);
        cb.setItems(items);
        if (!items.isEmpty()) {
            cb.setValue(items.get(0));
        }
        cb.setWidthFull();
        return cb;
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

    public Registration addFilterChangedListener(ComponentEventListener<FilterChangedEvent> listener) {
        return addListener(FilterChangedEvent.class, listener);
    }

}
