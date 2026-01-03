package com.gesamtprojekt.application.ui.components.admin;

import com.gesamtprojekt.application.model.MeetingRoom;
import com.gesamtprojekt.application.service.implementation.MeetingRoomService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;

public class RoomDialog extends Dialog {

    public RoomDialog(String title, MeetingRoomService service, MeetingRoom room, Runnable onSaved) {
        setHeaderTitle(title);

        var form = new RoomForm();
        form.setRoom(room);

        add(form);
        getFooter().add(cancelBtn(), saveBtn(form, service, room, onSaved));
    }

    private Button cancelBtn() {
        return new Button("Cancel", e -> close());
    }

    private Button saveBtn(RoomForm form, MeetingRoomService service, MeetingRoom room, Runnable onSaved) {
        var save = new Button("Save");
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        save.addClickListener(e -> doSave(form, service, room, onSaved));
        return save;
    }

    private void doSave(RoomForm form, MeetingRoomService service, MeetingRoom room, Runnable onSaved) {
        if (!form.isValid()) return;
        var target = room == null ? new MeetingRoom() : room;
        form.apply(target);
        service.updateRoom(target);
        onSaved.run();
        close();
    }
}
