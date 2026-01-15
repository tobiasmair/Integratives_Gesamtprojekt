package com.gesamtprojekt.application.ui.client.profile;

import com.gesamtprojekt.application.model.Client;
import com.gesamtprojekt.application.security.SecurityService;
import com.gesamtprojekt.application.service.implementation.ClientService;
import com.gesamtprojekt.application.ui.client.MainLayout;
import com.gesamtprojekt.application.ui.components.admin.RegistrationForm;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "profile", layout = MainLayout.class)
@PageTitle("Profile")
@RolesAllowed({"USER", "ADMIN", "ROOM"})
public class ProfileView extends VerticalLayout {

    private final ClientService clientService;
    private final SecurityService securityService;
    private final RegistrationForm registrationForm = new RegistrationForm();
    private Client currentClient;

    public ProfileView(ClientService clientService, SecurityService securityService) {
        this.clientService = clientService;
        this.securityService = securityService;

        setAlignItems(Alignment.CENTER);

        // Aktuellen Nutzer laden
        securityService.getAuthenticatedClient().ifPresent(client -> {
            this.currentClient = client;
            registrationForm.setClient(client);
        });

        // Felder für Ansicht anpassen
        configureFormForProfile();

        Button saveButton = new Button("Save Profile", e -> saveProfile());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        VerticalLayout content = new VerticalLayout(new H2("Personal Data"), registrationForm, saveButton);
        content.setMaxWidth("500px");
        content.setPadding(true);

        add(content);
    }

    private void configureFormForProfile() {
        // Username + Rolle nicht änderbar
        registrationForm.username.setReadOnly(true);
        registrationForm.role.setReadOnly(true);

        if ("ROOM".equals(currentClient.getRole())) {
            registrationForm.email.setReadOnly(true);
            registrationForm.department.setReadOnly(true);
            registrationForm.userType.setReadOnly(true);
        }

        registrationForm.password.setPlaceholder("Simply fill in to modify");
        registrationForm.confirmPassword.setPlaceholder("Confirm password");
    }

    private void saveProfile() {
        if (currentClient == null) return;

        String pass = registrationForm.password.getValue();

        boolean isRoom = "ROOM".equals(currentClient.getRole());

        if (isRoom || registrationForm.isValid()) {
            try {
                if (!isRoom) {
                    currentClient.setEmail(registrationForm.email.getValue());
                    currentClient.setDepartment(registrationForm.department.getValue());
                    currentClient.setUserType(registrationForm.userType.getValue());
                }

                // Speichern
                if (pass.isEmpty()) {
                    clientService.updateClient(currentClient);
                } else {
                    // Bei Room findet Passwort überprüfung hier statt. Ansonsten direkt in isValid()
                    if (isRoom && !registrationForm.password.getValue().equals(registrationForm.confirmPassword.getValue())) {
                        Notification.show("Passwords do not match!", 3000, Notification.Position.TOP_CENTER)
                                .addThemeVariants(NotificationVariant.LUMO_ERROR);
                        return;
                    }
                    clientService.updateClientWithPassword(currentClient, pass);
                }

                Notification.show("Profile successfully updated!")
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                // Felder leeren
                registrationForm.password.clear();
                registrationForm.confirmPassword.clear();

            } catch (Exception ex) {
                Notification.show("Error: " + ex.getMessage())
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        }
    }
}