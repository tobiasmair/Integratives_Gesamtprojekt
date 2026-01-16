package com.gesamtprojekt.application.ui.components.admin;

import com.gesamtprojekt.application.model.MeetingRoom;
import com.gesamtprojekt.application.service.implementation.EquipmentService;
import com.gesamtprojekt.application.service.implementation.MeetingRoomService;
import com.gesamtprojekt.application.service.implementation.RoomImageStorageService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class RoomDialog extends Dialog {

    private final MeetingRoomService roomService;
    private final Runnable onSaved;
    private final MeetingRoom existingRoom;

    private final RoomForm form;

    public RoomDialog(String title,
                      MeetingRoomService roomService,
                      EquipmentService equipmentService,
                      RoomImageStorageService imageStorage,
                      MeetingRoom room,
                      Runnable onSaved) {
        this.roomService = roomService;
        this.onSaved = onSaved;
        this.existingRoom = room;

        this.form = new RoomForm(equipmentService, imageStorage);

        setHeaderTitle(title);
        add(new VerticalLayout(form));

        form.setRoom(room);
        getFooter().add(cancelBtn(), saveBtn());
    }

    private Button cancelBtn() {
        return new Button("Cancel", e -> close());
    }

    private Button saveBtn() {
        Button save = new Button("Save", e -> save());
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        return save;
    }

    private void save() {
        if (!form.isValid()) return;

        MeetingRoom target = existingRoom == null ? new MeetingRoom() : existingRoom;
        form.apply(target);

        try {
            persist(target);
            onSaved.run();
            notifyOk(existingRoom == null ? "Room created." : "Room updated.");
            close();
        } catch (Exception ex) {
            notifyErr("Error: " + ex.getMessage());
        }
    }

    private void persist(MeetingRoom room) {
        if (existingRoom == null) roomService.createRoom(room);
        else roomService.updateRoom(room);
    }

    private void notifyOk(String msg) {
        Notification.show(msg, 3000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void notifyErr(String msg) {
        Notification.show(msg, 5000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
