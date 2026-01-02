package com.gesamtprojekt.application.ui.components.admin;

import com.gesamtprojekt.application.model.Client;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
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
}