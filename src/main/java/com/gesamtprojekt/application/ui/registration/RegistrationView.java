package com.gesamtprojekt.application.ui.registration;

import com.gesamtprojekt.application.service.implementation.ClientService;
import com.gesamtprojekt.application.ui.components.admin.RegistrationForm;
import com.gesamtprojekt.application.ui.login.LoginView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("register")
@PageTitle("Registration | MCI Meeting Booker")
@AnonymousAllowed
public class RegistrationView extends VerticalLayout {

    private final ClientService clientService;

    public RegistrationView(ClientService clientService) {
        this.clientService = clientService;
        addClassName("register-screen");
        setSizeFull();

        Div card = new Div();
        card.addClassName("login-card");
        card.getStyle().set("max-width", "500px");

        var title = new H2("Sign Up");
        title.getStyle().set("color", "var(--mci-blue)").set("margin-top", "0");

        VerticalLayout customerForm = createCustomerForm();

        var loginLink = new Button("Back to Login", e -> UI.getCurrent().navigate(LoginView.class));
        loginLink.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        loginLink.setWidthFull();

        card.add(title, customerForm, loginLink);
        add(card);
    }

    private VerticalLayout createCustomerForm() {
        RegistrationForm form = new RegistrationForm();

        // Rollen Auswahl ausblenden
        form.role.setVisible(false);

        var registerButton = new Button("Register");
        registerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registerButton.setWidthFull();

        registerButton.addClickListener(e -> {
            if (form.isValid()) {
                try {
                    clientService.createClient(form.username.getValue(), form.password.getValue(), form.email.getValue(), form.department.getValue(), form.userType.getValue(),"USER");
                    Notification.show("Registration success!", 3000, Notification.Position.TOP_CENTER)
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    UI.getCurrent().navigate(LoginView.class);
                } catch (Exception ex) {
                    Notification.show("Error: " + ex.getMessage(), 5000, Notification.Position.TOP_CENTER)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            }
        });

        VerticalLayout fields = new VerticalLayout(form, registerButton);
        fields.setPadding(false);
        fields.setSpacing(true);
        fields.setAlignItems(Alignment.CENTER);

        return fields;
    }
}