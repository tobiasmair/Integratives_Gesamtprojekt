package com.gesamtprojekt.application.ui.root;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("") // Root-URL
@AnonymousAllowed
public class RootRedirectView extends Div {

    public RootRedirectView() {
        UI.getCurrent().navigate("dashboard");
    }
}
