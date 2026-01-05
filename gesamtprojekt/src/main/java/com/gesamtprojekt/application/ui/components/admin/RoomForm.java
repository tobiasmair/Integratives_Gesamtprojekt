package com.gesamtprojekt.application.ui.components.admin;

import com.gesamtprojekt.application.model.Equipment;
import com.gesamtprojekt.application.model.MeetingRoom;
import com.gesamtprojekt.application.service.implementation.EquipmentService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.checkbox.CheckboxGroupVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import java.util.Comparator;


import java.util.LinkedHashSet;
import java.util.List;

public class RoomForm extends FormLayout {

    private final EquipmentService equipmentService;

    public final TextField name = new TextField("Room Name *");
    public final IntegerField capacity = new IntegerField("Capacity *");
    public final ComboBox<String> building = new ComboBox<>("Building *");
    public final IntegerField floor = new IntegerField("Floor");

    public final ComboBox<String> status = new ComboBox<>("Status");

    private final CheckboxGroup<Equipment> equipmentGroup = new CheckboxGroup<>();

    public RoomForm(EquipmentService equipmentService) {
        this.equipmentService = equipmentService;

        setResponsiveSteps(new ResponsiveStep("0", 2));
        setupFields();

        add(name, capacity, building, floor, status);
        add(new Hr(), 2);
        add(equipmentSection(), 2);
    }

    public void setRoom(MeetingRoom r) {
        if (r == null) return;

        name.setValue(n(r.getName()));
        capacity.setValue(r.getCapacity());
        building.setValue(n(r.getLocation()));
        status.setValue(n(r.getStatus()));
        floor.setValue(r.getFloor());
        setSelectedEquipmentFromRoom(r);
    }


    public void apply(MeetingRoom r) {
        r.setName(name.getValue());
        r.setCapacity(capacity.getValue());
        r.setLocation(building.getValue());
        r.setFloor(floor.getValue());
        r.setStatus(status.getValue());
        r.setEquipment(new java.util.LinkedHashSet<>(equipmentGroup.getValue()));
    }

    public boolean isValid() {
        return !name.isEmpty() && capacity.getValue() != null && !building.isEmpty();
    }

    private Component equipmentSection() {
        Span title = new Span("Amenities & Equipment");
        title.getStyle().set("font-weight", "600");

        equipmentGroup.setItemLabelGenerator(Equipment::getDescription);
        reloadEquipmentSorted();

        equipmentGroup.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);
        equipmentGroup.getStyle().set("columns", "2"); // 2 columns left

        var left = new VerticalLayout(title, equipmentGroup);
        left.setPadding(false);
        left.setSpacing(true);
        left.setWidthFull();

        TextField newEquipment = new TextField("New equipment");
        newEquipment.setPlaceholder("e.g. Projector");
        newEquipment.setWidthFull();

        Button addBtn = new Button("Add equipment");
        addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addBtn.setWidthFull();
        addBtn.addClickListener(e -> addEquipment(newEquipment));

        var right = new VerticalLayout(newEquipment, addBtn);
        right.setPadding(false);
        right.setSpacing(true);
        right.setWidth("260px");

        HorizontalLayout wrap = new HorizontalLayout(left, right);
        wrap.setWidthFull();
        wrap.setAlignItems(FlexComponent.Alignment.START);
        wrap.setFlexGrow(1, left);

        return wrap;
    }


    private List<Equipment> loadEquipment() {
        return equipmentService.findAll();
    }

    private void setupFields() {
        name.setPlaceholder("Enter room name");
        name.setRequiredIndicatorVisible(true);

        capacity.setMin(1);
        capacity.setRequiredIndicatorVisible(true);

        building.setItems("MCI I", "MCI II", "MCI III");
        building.setRequiredIndicatorVisible(true);

        status.setItems("ACTIVE", "INACTIVE");
        status.setValue("ACTIVE");

        floor.setStepButtonsVisible(true);
        floor.setMin(-10);
        floor.setMax(50);
        floor.setHelperText("e.g. 0 = Ground floor, 1 = 1st floor");


    }

    private void reloadEquipmentSorted() {
        List<Equipment> items = equipmentService.findAll();
        items.sort(Comparator.comparing(e -> n(e.getDescription()).toLowerCase()));
        equipmentGroup.setItems(items);
    }

    private void addEquipment(TextField field) {
        String val = field.getValue() == null ? "" : field.getValue().trim();
        if (val.isEmpty()) return;

        Equipment eq = new Equipment();
        eq.setDescription(val);

        Equipment created = equipmentService.create(eq);

        field.clear();
        reloadEquipmentSorted();

        var selected = new LinkedHashSet<>(equipmentGroup.getValue());
        selected.add(created);
        equipmentGroup.setValue(selected);
    }

    private void setSelectedEquipmentFromRoom(MeetingRoom room) {
        List<Equipment> all = equipmentService.findAll();
        all.sort(Comparator.comparing(e -> n(e.getDescription()).toLowerCase()));
        equipmentGroup.setItems(all);

        var selectedIds = room.getEquipment().stream()
                .map(Equipment::getEquipmentId)
                .filter(id -> id != null)
                .collect(java.util.stream.Collectors.toSet());

        var selected = all.stream()
                .filter(e -> e.getEquipmentId() != null && selectedIds.contains(e.getEquipmentId()))
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));

        equipmentGroup.setValue(selected);
    }


    private String n(String v) {
        return v == null ? "" : v;
    }
}
