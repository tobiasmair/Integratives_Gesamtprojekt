package com.gesamtprojekt.application.ui.client;

import com.gesamtprojekt.application.model.Client;
import com.gesamtprojekt.application.security.SecurityService;
import com.gesamtprojekt.application.service.implementation.NotificationService;
import com.gesamtprojekt.application.ui.components.buttons.NotificationBell;
import com.gesamtprojekt.application.ui.components.navigation.SideNavbar;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.server.menu.MenuConfiguration;
import jakarta.annotation.security.PermitAll;

import java.util.Optional;

@CssImport("./themes/gesamtprojekt/main-layout.css")
//@Layout
@PermitAll
public class MainLayout extends AppLayout implements AfterNavigationObserver {

    private final SecurityService securityService;
    private final NotificationService notificationService;
    private SideNavbar sideNavbar;
    private NotificationBell notificationBell;

    public MainLayout(SecurityService securityService, NotificationService notificationService) {
        this.securityService = securityService;
        this.notificationService = notificationService;

        setPrimarySection(Section.DRAWER);
        getStyle().setHeight("100%");

        addHeaderContent();
        addDrawerContent();
    }

    private void addDrawerContent() {
        sideNavbar = new SideNavbar(this::toggleCollapsed, securityService);
        addToDrawer(sideNavbar);
        syncCollapsedIcon();
    }

    private void toggleCollapsed() {
        boolean collapsed = isCollapsed();
        setCollapsed(!collapsed);
        syncCollapsedIcon();
    }

    private boolean isCollapsed() {
        return getElement().getClassList().contains("drawer-collapsed");
    }

    private void setCollapsed(boolean collapsed) {
        if (collapsed) getElement().getClassList().add("drawer-collapsed");
        else getElement().getClassList().remove("drawer-collapsed");
    }

    private void syncCollapsedIcon() {
        sideNavbar.setCollapsed(isCollapsed());
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        getCurrentPageTitle();
    }

    private String getCurrentPageTitle() {
        return MenuConfiguration.getPageHeader(getContent()).orElse("");
    }

    private void addHeaderContent() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setPadding(true);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        Client user = securityService.getAuthenticatedClient()
                .orElseThrow(() -> new IllegalStateException("No authenticated user found"));

        // Glocken Symbol
        this.notificationBell = new NotificationBell(notificationService, user);
        this.notificationBell.setUnreadCount(notificationService.countUnread(user));

        header.add(notificationBell);
        addToNavbar(header);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        // alle 30 Sek nach updates fragen
        attachEvent.getUI().setPollInterval(30000);

        // Listener der die Glocke aktualisiert
        attachEvent.getUI().addPollListener(e -> {
            if (notificationBell != null) {
                notificationBell.updateBadge();
            }
        });
    }

    // Polling stoppen, wenn der User die Seite verlässt
    @Override
    protected void onDetach(DetachEvent detachEvent) {
        detachEvent.getUI().setPollInterval(-1);
        super.onDetach(detachEvent);
    }
}

