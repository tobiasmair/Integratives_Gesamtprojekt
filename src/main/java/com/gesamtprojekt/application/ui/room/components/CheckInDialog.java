package com.gesamtprojekt.application.ui.room.components;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.textfield.TextField;
import com.gesamtprojekt.application.model.Booking;

public class CheckInDialog extends Dialog {
    public CheckInDialog(Booking booking, Runnable onValidCode) {
        setHeaderTitle("Confirm Booking");

        TextField codeField = new TextField("Booking Code");
        codeField.setPlaceholder("____-____");
        codeField.setWidthFull();

        Button confirmBtn = new Button("Unlock", e -> {
            if (codeField.getValue().equals(booking.getBookingCode())) {
                onValidCode.run();
                close();
            } else {
                codeField.setInvalid(true);
                codeField.setErrorMessage("Invalid Code!");
            }
        });
        confirmBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        getFooter().add(new Button("Cancel", i -> close()), confirmBtn);
        add(codeField);
    }
}
