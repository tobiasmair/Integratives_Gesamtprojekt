package com.gesamtprojekt.application.ui.client.profile;

import com.gesamtprojekt.application.ui.client.MainLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "profile", layout = MainLayout.class)
@PageTitle("Profile")
@PermitAll
public class ProfileView extends VerticalLayout {

    public ProfileView() {

        add(new H1("Profile"));
    }

}
