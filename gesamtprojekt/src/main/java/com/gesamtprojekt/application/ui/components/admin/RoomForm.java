package com.gesamtprojekt.application.ui.components.admin;

import com.gesamtprojekt.application.model.MeetingRoom;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;

public class RoomForm extends FormLayout {

    public final TextField name = new TextField("Room Name *");
    public final IntegerField capacity = new IntegerField("Capacity *");
    public final ComboBox<String> building = new ComboBox<>("Building");
    public final ComboBox<String> status = new ComboBox<>("Status");

    public RoomForm() {
        setResponsiveSteps(new ResponsiveStep("0", 2));
        setupFields();
        add(name, capacity, building, status);
    }

    public void setRoom(MeetingRoom r) {
        if (r == null) return;
        name.setValue(n(r.getName()));
        capacity.setValue(r.getCapacity());
        building.setValue(n(r.getLocation()));
        status.setValue(n(r.getStatus()));
    }

    public void apply(MeetingRoom r) {
        r.setName(name.getValue());
        r.setCapacity(capacity.getValue());
        r.setLocation(building.getValue());
        r.setStatus(status.getValue());
    }

    public boolean isValid() {
        return !name.isEmpty() && capacity.getValue() != null && !building.isEmpty();
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
    }

    private String n(String v) {
        return v == null ? "" : v;
    }
}
