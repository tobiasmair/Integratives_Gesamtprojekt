package com.gesamtprojekt.application.ui.components.calendar;

import com.gesamtprojekt.application.model.Equipment;
import com.gesamtprojekt.application.service.implementation.EquipmentService;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.shared.Registration;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

public class CalendarControlsBar extends VerticalLayout {

    private DatePicker date = new DatePicker("Date");
    private TimePicker start;
    private TimePicker end;
    private ComboBox<String> building = new ComboBox<>("Building");
    private ComboBox<String> floor = new ComboBox<>("Floor");
    private ComboBox<String> capacity = new ComboBox<>("Min Capacity");
    private MultiSelectComboBox<String> equipment = new MultiSelectComboBox<>("Equipment");
    private final EquipmentService equipmentService;

    private ViewMode currentMode = ViewMode.CALENDAR;
    private Button calendarModeBtn;
    private Button browseModeBtn;
    private FormLayout topRow;

    public CalendarControlsBar(EquipmentService equipmentService) {
        this.equipmentService = equipmentService;
        setWidthFull();
        setPadding(false);
        setSpacing(true);
        addClassName("calendar-controls");

        add(buildModeToggle());
        topRow = buildTopRow();
        add(topRow);
        add(buildFiltersRow());

        addValueChangeListeners();
    }


    public LocalDateTime getStartDateTime() {
        if (currentMode == ViewMode.BROWSE || date.getValue() == null || start.getValue() == null) {
            return null;
        }
        return date.getValue().atTime(start.getValue());
    }
    public LocalDateTime getEndDateTime() {
        if (currentMode == ViewMode.BROWSE || date.getValue() == null || end.getValue() == null) {
            return null;
        }
        return date.getValue().atTime(end.getValue());
    }
    public String getBuilding() { return building.getValue(); }
    public String getFloor() { return floor.getValue(); }
    public String getCapacity() { return capacity.getValue(); }
    public Set<String> getEquipment() { return equipment.getValue(); }

    private void addValueChangeListeners() {
        date.addValueChangeListener(e -> fireEvent(new FilterChangedEvent(this)));
        start.addValueChangeListener(e -> fireEvent(new FilterChangedEvent(this)));
        end.addValueChangeListener(e -> fireEvent(new FilterChangedEvent(this)));
        building.addValueChangeListener(e -> fireEvent(new FilterChangedEvent(this)));
        floor.addValueChangeListener(e -> fireEvent(new FilterChangedEvent(this)));
        capacity.addValueChangeListener(e -> fireEvent(new FilterChangedEvent(this)));
        equipment.addValueChangeListener(e -> fireEvent(new FilterChangedEvent(this)));
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
        building = combo("Building", List.of("All Buildings", "MCI I", "MCI II", "MCI III", "MCI IV", "MCI V"));
        floor = combo("Floor", List.of("Any Floor", "1", "2", "3"));
        capacity = combo("Min Capacity", List.of("Any", "5+", "10+", "20+", "50+"));

        // Equipment aus Datenbank laden
        List<String> equipmentItems = equipmentService.findAll().stream()
                .map(Equipment::getDescription)
                .filter(desc -> desc != null && !desc.trim().isEmpty())
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();

        equipment.setLabel("Equipment");
        equipment.setItems(equipmentItems);
        equipment.setWidthFull();

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

    public Registration addModeChangedListener(ComponentEventListener<ModeChangedEvent> listener) {
        return addListener(ModeChangedEvent.class, listener);
    }

    private HorizontalLayout buildModeToggle() {
        HorizontalLayout toggleBar = new HorizontalLayout();
        toggleBar.setWidthFull();
        toggleBar.setPadding(false);
        toggleBar.setSpacing(true);
        toggleBar.getStyle().set("margin-bottom", "10px");

        calendarModeBtn = new Button("Book by Date", new Icon(VaadinIcon.CALENDAR));
        calendarModeBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        calendarModeBtn.addClickListener(e -> switchMode(ViewMode.CALENDAR));

        browseModeBtn = new Button("Browse Rooms", new Icon(VaadinIcon.SEARCH));
        browseModeBtn.addClickListener(e -> switchMode(ViewMode.BROWSE));

        toggleBar.add(calendarModeBtn, browseModeBtn);
        return toggleBar;
    }

    private void switchMode(ViewMode newMode) {
        if (currentMode == newMode) return;

        currentMode = newMode;

        // Button Styling anpassen
        if (newMode == ViewMode.CALENDAR) {
            calendarModeBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            browseModeBtn.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
            topRow.setVisible(true);
        } else {
            browseModeBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            calendarModeBtn.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
            topRow.setVisible(false);
        }

        fireEvent(new ModeChangedEvent(this, newMode));
        fireEvent(new FilterChangedEvent(this));
    }

}
