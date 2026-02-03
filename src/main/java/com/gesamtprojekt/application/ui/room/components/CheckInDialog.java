package com.gesamtprojekt.application.ui.room.components;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.textfield.TextField;
import com.gesamtprojekt.application.model.Booking;

/**
 * A dialog window that prompts the user to enter the Room-Code
 * Unlocks the room only if the code is correct
 */
public class CheckInDialog extends Dialog {
    public CheckInDialog(Booking booking, Runnable onValidCode) {
        setHeaderTitle("Confirm Booking");

        TextField codeField = new TextField("Booking Code");
        codeField.setPlaceholder("____-____");
        codeField.setWidthFull();

        // checks on click if the entered code matches the code in the Database
        Button confirmBtn = new Button("Unlock", e -> {
            if (codeField.getValue().equals(booking.getBookingCode())) {
                onValidCode.run();  // proceeds check-in
                close();
            } else {
                // feedback of wrong entry
                codeField.setInvalid(true);
                codeField.setErrorMessage("Invalid Code!");
            }
        });
        confirmBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        getFooter().add(new Button("Cancel", i -> close()), confirmBtn);
        add(codeField);
    }
}
