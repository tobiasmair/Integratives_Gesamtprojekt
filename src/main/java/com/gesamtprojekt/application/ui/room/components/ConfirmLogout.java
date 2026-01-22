package com.gesamtprojekt.application.ui.room.components;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;


/**
 * A dialog window that prompts the user to enter the rooms password on attempted logout
 * Safety-feature to ensure only entitled personal logs-out the room tablet
 */
public class ConfirmLogout extends Dialog {

    public interface ConfirmCallback {
        void onConfirm();
    }
    public ConfirmLogout(java.util.function.Predicate<String> passwordChecker, ConfirmCallback callback) {
        setHeaderTitle("Confirm Logout");

        PasswordField passwordField = new PasswordField("Enter Room Password");
        passwordField.setWidthFull();

        VerticalLayout layout = new VerticalLayout(new Span("Please enter the room password to logout."), passwordField);
        add(layout);

        // validates the credentials
        Button confirmButton = new Button("Logout", e -> {
            if (passwordChecker.test(passwordField.getValue())) {
                callback.onConfirm();
                close();
            } else {
                passwordField.setInvalid(true);
                passwordField.setErrorMessage("Invalid Password");
                Notification.show("Logout denied", 2000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        Button cancelButton = new Button("Cancel", e -> close());

        getFooter().add(cancelButton, confirmButton);

        // enable confirm by pressing enter
        passwordField.addKeyDownListener(com.vaadin.flow.component.Key.ENTER, e -> confirmButton.click());
    }


}
