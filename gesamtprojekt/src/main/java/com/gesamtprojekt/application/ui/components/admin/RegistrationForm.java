package com.gesamtprojekt.application.ui.components.admin;

import com.gesamtprojekt.application.model.Client;
import com.gesamtprojekt.application.service.implementation.ClientService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;

public class RegistrationForm extends FormLayout {

    public TextField username = new TextField("Username");
    public EmailField email = new EmailField("E-Mail");
    public ComboBox<String> department = new ComboBox<>("Department");
    public ComboBox<String> userType = new ComboBox<>("User Type");
    public ComboBox<String> role = new ComboBox<>("Role");
    public PasswordField password = new PasswordField("Password");
    public PasswordField confirmPassword = new PasswordField("Repeat Password");

    public RegistrationForm() {
        // Dropdown-Werte
        role.setItems("ADMIN", "USER");
        department.setItems("DiBSE", "MCI 1", "MCI 2", "IT-Services");
        userType.setItems("STUDENT", "LECTURER", "STAFF", "EXTERNAL");

        // Keine leeren Werte zulassen
        username.setRequiredIndicatorVisible(true);
        email.setRequiredIndicatorVisible(true);
        department.setRequiredIndicatorVisible(true);
        userType.setRequiredIndicatorVisible(true);
        role.setRequiredIndicatorVisible(true);
        password.setRequiredIndicatorVisible(true);
        confirmPassword.setRequiredIndicatorVisible(true);

        email.setErrorMessage("Invalid email format");

        setResponsiveSteps(new ResponsiveStep("0", 1));
        add(username, email, department, userType, role, password, confirmPassword);
    }

    public void setClient(Client client) {
        if (client != null) {
            username.setValue(client.getUsername());
            email.setValue(client.getEmail());
            department.setValue(client.getDepartment());
            userType.setValue(client.getUserType());
            role.setValue(client.getRole());
        }
    }

    // Prüft ob alle Felder korrekt ausgefüllt sind
    public boolean isValid() {
        // Prüft auf leere Felder
        if (username.isEmpty() || email.isEmpty() || department.isEmpty() ||
                userType.isEmpty() || password.isEmpty()) {
            Notification.show("Please fill in all required fields!", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return false;
        }

        // Format Mail prüfen
        if (email.isInvalid()) {
            Notification.show("Please enter a valid email address!", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return false;
        }

        // Prüft Passwort-Match
        if (!password.getValue().equals(confirmPassword.getValue())) {
            Notification.show("Passwords do not match!", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return false;
        }
        return true;
    }
}