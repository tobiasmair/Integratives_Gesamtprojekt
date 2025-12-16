package com.gesamtprojekt.application.ui.admin.usermanagement;

import com.gesamtprojekt.application.ui.MainLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route(value = "usermanagement", layout = MainLayout.class)
@PageTitle("User Management")
@AnonymousAllowed
public class UserManagementView extends VerticalLayout {

    public UserManagementView() {

        add(new H1("User Management"));
    }
}
