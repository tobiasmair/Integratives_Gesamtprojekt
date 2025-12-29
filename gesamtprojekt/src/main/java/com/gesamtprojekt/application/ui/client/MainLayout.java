package com.gesamtprojekt.application.ui.client;

import com.gesamtprojekt.application.repositories.UsersRepository;
import com.gesamtprojekt.application.security.SecurityService;
import com.gesamtprojekt.application.service.implementation.AuthService;
import com.gesamtprojekt.application.ui.components.navigation.SideNavbar;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.server.menu.MenuConfiguration;
import jakarta.annotation.security.PermitAll;

@CssImport("./themes/gesamtprojekt/main-layout.css")
//@Layout
@PermitAll
public class MainLayout extends AppLayout implements AfterNavigationObserver {

    private final SecurityService securityService;
    private SideNavbar sideNavbar;
    private final UsersRepository usersRepository;

    public MainLayout(SecurityService securityService, UsersRepository usersRepository) {
        this.securityService = securityService;
        this.usersRepository = usersRepository;

        setPrimarySection(Section.DRAWER);
        getStyle().setHeight("100%");
        addDrawerContent();
    }

    private void addDrawerContent() {
        sideNavbar = new SideNavbar(this::toggleCollapsed, securityService, usersRepository);
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
}

