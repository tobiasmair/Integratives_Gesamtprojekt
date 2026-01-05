package com.gesamtprojekt.application.ui.components.admin;

import com.gesamtprojekt.application.model.MeetingRoom;
import com.gesamtprojekt.application.service.implementation.EquipmentService;
import com.gesamtprojekt.application.service.implementation.MeetingRoomService;
import com.gesamtprojekt.application.service.implementation.RoomImageStorageService;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;

import java.util.List;

public class RoomTableSection extends VerticalLayout {

    private final MeetingRoomService meetingRoomService;
    private final EquipmentService equipmentService;
    private final RoomImageStorageService imageStorage;


    private final Grid<MeetingRoom> grid = new Grid<>(MeetingRoom.class, false);
    private final TextField searchField = new TextField();
    private final ComboBox<String> buildingFilter =
            new ComboBox<>("", List.of("All Buildings", "MCI I", "MCI II", "MCI III"));
    private final ComboBox<String> statusFilter =
            new ComboBox<>("", List.of("All Status", "ACTIVE", "INACTIVE"));
    private final Button addRoomBtn = new Button("Add Room");

    public RoomTableSection(MeetingRoomService meetingRoomService, EquipmentService equipmentService, RoomImageStorageService imageStorage) {
        this.meetingRoomService = meetingRoomService;
        this.equipmentService = equipmentService;
        this.imageStorage = imageStorage;

        setSizeFull();
        setPadding(false);

        add(buildToolbar(), buildGrid());
        updateList();
    }

    private HorizontalLayout buildToolbar() {
        searchField.setPlaceholder("Search rooms by name...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setWidth("400px");
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(e -> updateList());

        buildingFilter.setValue("All Buildings");
        buildingFilter.addValueChangeListener(e -> updateList());

        statusFilter.setValue("All Status");
        statusFilter.addValueChangeListener(e -> updateList());

        addRoomBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addRoomBtn.addClickListener(e -> addRoomDialog());

        HorizontalLayout toolbar = new HorizontalLayout(searchField, buildingFilter, statusFilter, addRoomBtn);
        toolbar.setWidthFull();
        toolbar.setAlignItems(Alignment.BASELINE);
        toolbar.setFlexGrow(1, searchField);
        return toolbar;
    }

    private Grid<MeetingRoom> buildGrid() {
        grid.setSizeFull();
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);

        grid.addColumn(MeetingRoom::getName).setHeader("Room").setSortable(true);
        grid.addColumn(MeetingRoom::getCapacity).setHeader("Capacity");
        grid.addColumn(MeetingRoom::getLocation).setHeader("Building");
        grid.addColumn(MeetingRoom::getFloor).setHeader("Floor");
        grid.addColumn(MeetingRoom::getStatus).setHeader("Status");

        grid.addComponentColumn(room -> {
            Button edit = new Button(VaadinIcon.EDIT.create());
            edit.addClickListener(e -> openEditDialog(room));

            Button delete = new Button(VaadinIcon.TRASH.create());
            delete.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            delete.addClickListener(e -> openDeleteDialog(room));

            return new HorizontalLayout(edit, delete);
        }).setHeader("Actions");

        return grid;
    }

    private void updateList() {
        grid.setItems(meetingRoomService.findAllRooms(
                searchField.getValue(),
                buildingFilter.getValue(),
                statusFilter.getValue()
        ));
    }

    private void openEditDialog(MeetingRoom room) {
        MeetingRoom roomForEdit = meetingRoomService.findRoomForEdit(room.getRoomId());

        Dialog dialog = new Dialog();
        RoomForm form = new RoomForm(equipmentService, imageStorage);
        form.setRoom(roomForEdit);

        dialog.setHeaderTitle("Edit Room: " + roomForEdit.getName());
        dialog.add(new VerticalLayout(form));

        Button saveButton = new Button("Save", event -> saveEdit(roomForEdit, form, dialog));
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Cancel", event -> dialog.close());
        dialog.getFooter().add(cancelButton, saveButton);
        dialog.open();
    }


    private void saveEdit(MeetingRoom room, RoomForm form, Dialog dialog) {
        if (!form.isValid()) return;

        form.apply(room);
        meetingRoomService.updateRoom(room);
        updateList();
        dialog.close();

        Notification.show("Room updated.", 3000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void openDeleteDialog(MeetingRoom room) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Delete Room: " + room.getName());

        Span text = new Span("Are you sure you want to delete Room " + room.getName() + " ?");
        dialog.add(text);

        Button deleteButton = new Button("Delete", event -> deleteRoom(room, dialog));
        deleteButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        Button cancelButton = new Button("Cancel", event -> dialog.close());

        dialog.getFooter().add(cancelButton, deleteButton);
        dialog.open();
    }

    private void deleteRoom(MeetingRoom room, Dialog dialog) {
        meetingRoomService.deleteRoom(room);
        updateList();
        dialog.close();

        Notification.show("Room deleted.", 3000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void addRoomDialog() {
        Dialog dialog = new Dialog();
        RoomForm form = new RoomForm(equipmentService, imageStorage);

        dialog.setHeaderTitle("Create new Room");
        dialog.add(new VerticalLayout(form));

        Button saveButton = new Button("Save", event -> createRoom(form, dialog));
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Cancel", event -> dialog.close());
        dialog.getFooter().add(cancelButton, saveButton);
        dialog.open();
    }

    private void createRoom(RoomForm form, Dialog dialog) {
        if (!form.isValid()) return;

        MeetingRoom room = new MeetingRoom();
        form.apply(room);

        try {
            meetingRoomService.createRoom(room);
            updateList();
            dialog.close();

            Notification.show("Room created.", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (Exception ex) {
            Notification.show("Error: " + ex.getMessage(), 5000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}
