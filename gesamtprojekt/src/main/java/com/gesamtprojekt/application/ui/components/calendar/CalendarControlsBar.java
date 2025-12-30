package com.gesamtprojekt.application.ui.components.calendar;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.timepicker.TimePicker;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class CalendarControlsBar extends VerticalLayout {

    public CalendarControlsBar() {
        setWidthFull();
        setPadding(false);
        setSpacing(true);
        addClassName("calendar-controls");

        add(buildTopRow());
        add(buildFiltersRow());
    }

    private FormLayout buildTopRow() {
        DatePicker date = new DatePicker("Date");
        date.setValue(LocalDate.now());
        date.setWidthFull();

        TimePicker start = time("Start Time", LocalTime.of(12, 0));
        TimePicker end = time("End Time", LocalTime.of(12, 0));

        FormLayout top = new FormLayout(date, start, end);
        top.setWidthFull();
        top.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("650px", 3)
        );
        return top;
    }

    private FormLayout buildFiltersRow() {
        ComboBox<String> building = combo("Building", List.of("All Buildings", "MCI 1", "MCI 2"));
        ComboBox<String> floor = combo("Floor", List.of("Any Floor", "1", "2", "3"));
        ComboBox<String> capacity = combo("Min Capacity", List.of("Any", "5+", "10+", "20+", "50+"));
        ComboBox<String> equipment = combo("Equipment", List.of("Any", "Wifi", "Whiteboard", "Smart TV"));
        ComboBox<String> fav = combo("Favourites", List.of("All rooms", "Only favourites"));

        FormLayout filters = new FormLayout(building, floor, capacity, equipment, fav);
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
        cb.setValue(items.get(0));
        cb.setWidthFull();
        return cb;
    }
}
