package com.gesamtprojekt.application.ui.login;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("login")
@PageTitle("Login | MCI Meeting Booker")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private LoginForm login = new LoginForm();

    public LoginView() {
        addClassName("login-screen");
        setSizeFull();

        Div card = new Div();
        card.addClassName("login-card");

        Image logo = new Image("/icons/mci_logo_transparent.png", "MCI Logo");
        logo.addClassName("login-logo");

        H1 title = new H1("MCI - Booking Service");
        title.getStyle().set("font-size", "var(--lumo-font-size-l)")
                .set("margin", "0 0 var(--lumo-space-m) 0")
                .set("color", "var(--mci-blue)");

        login.setForgotPasswordButtonVisible(false);
        login.setAction("login");

        HorizontalLayout footer = new HorizontalLayout();
        footer.addClassName("login-footer-btns");
        footer.setWidthFull();
        footer.setJustifyContentMode(JustifyContentMode.CENTER);

        Button registerButton = new Button("Create new Account");
        registerButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        registerButton.addClickListener(e -> UI.getCurrent().navigate("register"));

        footer.add(new Span("New here?"), registerButton);
        footer.setAlignItems(Alignment.CENTER);

        card.add(logo, title, login, footer);
        add(card);

    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (event.getLocation().getQueryParameters().getParameters().containsKey("error")) {
            login.setError(true);
        }
    }

}
