package com.gesamtprojekt.application.ui.registration;

import com.gesamtprojekt.application.service.implementation.ClientService;
import com.gesamtprojekt.application.ui.components.admin.RegistrationForm;
import com.gesamtprojekt.application.ui.login.LoginView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
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

        var title = new H2("Create new account");

        VerticalLayout customerForm = createCustomerForm();

        customerForm.setMaxWidth("400px");
        customerForm.setWidthFull();
        customerForm.setPadding(false);

        var loginLink = new Button("Back to Login", e -> UI.getCurrent().navigate(LoginView.class));
        loginLink.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        add(title, customerForm, loginLink);
    }

    private VerticalLayout createCustomerForm() {
        RegistrationForm form = new RegistrationForm();

        // Rollen Auswahl ausblenden
        form.role.setVisible(false);

        var registerButton = new Button("Register");
        registerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registerButton.setWidthFull();

        registerButton.addClickListener(e -> {
            if (form.password.getValue().equals(form.confirmPassword.getValue())) {
                try {
                    clientService.createClient(form.username.getValue(), form.password.getValue(), form.email.getValue(), form.department.getValue(), form.userType.getValue(),"USER");
                    Notification.show("Registration success!", 3000, Notification.Position.TOP_CENTER)
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    UI.getCurrent().navigate(LoginView.class);
                } catch (Exception ex) {
                    Notification.show("Error: " + ex.getMessage(), 5000, Notification.Position.TOP_CENTER)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            } else {
                Notification.show("Passwords do not match!");
            }
        });

        VerticalLayout fields = new VerticalLayout(form, registerButton);
        fields.setPadding(false);
        fields.setSpacing(true);
        fields.setAlignItems(Alignment.CENTER);

        return fields;
    }
}