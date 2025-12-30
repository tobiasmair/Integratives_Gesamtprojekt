package com.gesamtprojekt.application.ui.registration;

import com.gesamtprojekt.application.service.implementation.ClientService;
import com.gesamtprojekt.application.ui.login.LoginView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("register")
@PageTitle("Registration")
@AnonymousAllowed
public class RegistrationView extends VerticalLayout {

    private final ClientService clientService;

    public RegistrationView(ClientService clientService) {
        this.clientService = clientService;

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        var title = new H2("Neues Konto erstellen");

        VerticalLayout customerForm = createCustomerForm();

        customerForm.setMaxWidth("400px");
        customerForm.setWidthFull();
        customerForm.setPadding(false);

        var loginLink = new Button("Zurück zum Login", e -> UI.getCurrent().navigate(LoginView.class));
        loginLink.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        add(title, customerForm, loginLink);
    }

    private VerticalLayout createCustomerForm() {

        var username = new TextField("Benutzername");
        var password = new PasswordField("Passwort");
        var confirmPassword = new PasswordField("Passwort wiederholen");

        username.setWidthFull();
        password.setWidthFull();
        confirmPassword.setWidthFull();

        var registerButton = new Button("Registrieren");
        registerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registerButton.setWidthFull();

        registerButton.addClickListener(e -> {
            if (password.getValue().equals(confirmPassword.getValue())) {
                try {
                    clientService.createClient(username.getValue(), password.getValue(), "USER");
                    Notification.show("Registrierung erfolgreich!", 3000, Notification.Position.TOP_CENTER)
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    UI.getCurrent().navigate(LoginView.class);
                } catch (Exception ex) {
                    Notification.show("Fehler: " + ex.getMessage(), 5000, Notification.Position.TOP_CENTER)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            } else {
                Notification.show("Passwörter stimmen nicht überein!");
            }
        });

        VerticalLayout fields = new VerticalLayout(username, password, confirmPassword, registerButton);
        fields.setPadding(false);
        fields.setSpacing(true);
        fields.setAlignItems(Alignment.CENTER);

        return fields;
    }
}