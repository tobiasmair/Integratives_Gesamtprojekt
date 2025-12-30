
package com.gesamtprojekt.application.ui.components.navigation;

import com.gesamtprojekt.application.security.SecurityService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;

public class SideNavbar extends FlexLayout {

    private final Button collapseButton;
    private final SecurityService securityService;

    public SideNavbar(Runnable onToggleCollapse, SecurityService securityService) {
        this.securityService = securityService;

        addClassName("drawer");
        setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        setSizeFull();

        collapseButton = buildCollapseButton(onToggleCollapse);

        Header header = buildHeader(collapseButton);
        Scroller scroller = buildScroller(buildNavigation());
        Footer footer = buildFooter();

        add(header, buildProfileSection(), scroller, footer);
    }

    public void setCollapsed(boolean collapsed) {
        collapseButton.setIcon(collapsed
                ? VaadinIcon.ANGLE_RIGHT.create()
                : VaadinIcon.ANGLE_LEFT.create());
    }

    private Button buildCollapseButton(Runnable onToggleCollapse) {
        Button btn = new Button(VaadinIcon.ANGLE_LEFT.create());
        btn.addClassName("collapse-btn");
        btn.addClickListener(e -> onToggleCollapse.run());
        return btn;
    }

    private Header buildHeader(Button collapse) {
        Span appName = new Span("MCI - meeting room booker");
        appName.addClassNames(LumoUtility.FontWeight.SEMIBOLD, LumoUtility.FontSize.SMALL);
        appName.addClassName("app-name");

        Header header = new Header(appName, collapse);
        header.addClassName("drawer-header");
        return header;
    }

    private Scroller buildScroller(Component content) {
        Scroller scroller = new Scroller(content);
        scroller.addClassName("drawer-scroller");
        scroller.setSizeFull();
        return scroller;
    }

    private Component buildProfileSection() {
        // UserDetails von Spring Security
        return securityService.getAuthenticatedClient().map(user -> {

            if (user == null) return new Div();

            String fullName = user.getUsername();
            String roleText = user.getRole();

            Avatar avatar = buildAvatar(fullName);
            avatar.getStyle().set("margin-right", "var(--lumo-space-s)");

            Span name = new Span(fullName);
            name.addClassName("profile-name");

            Span role = new Span(roleText);
            role.addClassName("profile-role");

            VerticalLayout text = new VerticalLayout(name, role);
            text.setPadding(false);
            text.setSpacing(false);
            text.setMargin(false);
            text.addClassName("profile-text");

            Div box = new Div(avatar, text);
            box.addClassName("profile-box");
            box.getStyle().setHeight("50px");
            return box;

        }).orElse(new Div());
    }

    private Avatar buildAvatar(String fullName) {
        Avatar avatar = new Avatar();
        avatar.setName(fullName);
        avatar.setAbbreviation(getInitials(fullName));
        avatar.setColorIndex(3); // fixe Farbe, später dynamisch
        return avatar;
    }

    private String getInitials(String name) {
        if (name == null || name.isBlank()) return "?";

        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, 1).toUpperCase();

        return ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
    }

    private SideNav buildNavigation() {
        SideNav nav = new SideNav();
        nav.addClassName("sidenav");

        addDefaultItems(nav);

        if (isAdminUser()) {
            addAdminSection(nav);
        }

        return nav;
    }

    private boolean isAdminUser() {
        return securityService.isAdmin();
    }

    private void addDefaultItems(SideNav nav) {
        nav.addItem(new SideNavItem("My Dashboard", "", VaadinIcon.DASHBOARD.create()));
        nav.addItem(new SideNavItem("Calendar", "calendar", VaadinIcon.CALENDAR.create()));
        nav.addItem(new SideNavItem("Browse Rooms", "browserooms", VaadinIcon.SEARCH.create()));
    }

    private void addAdminSection(SideNav nav) {
        nav.addItem(new SideNavItem("Statistics & Reports", "statistics", VaadinIcon.CHART.create()));
        nav.addItem(new SideNavItem("Room Management", "roommanagement", VaadinIcon.BUILDING.create()));
        nav.addItem(new SideNavItem("User Management", "usermanagement", VaadinIcon.USERS.create()));
        nav.addItem(new SideNavItem("System Settings", "systemsettings", VaadinIcon.TOOLS.create()));
    }

    private Footer buildFooter() {
        Footer footer = new Footer();
        footer.addClassName("drawer-footer");

        Component logoutItem = buildFooterItem(VaadinIcon.SIGN_OUT, "Logout");
        logoutItem.getElement().addEventListener("click", e -> {
            securityService.logout();
        });
        logoutItem.getStyle().set("cursor", "pointer");

        Component profileItem = buildFooterItem(VaadinIcon.USER, "My Profile");
        profileItem.getElement().addEventListener("click", e -> {
            UI.getCurrent().navigate("profile");
        });

        footer.add(
                profileItem,
                logoutItem
        );
        footer.addClassNames(
                LumoUtility.Display.FLEX,
                LumoUtility.FlexDirection.COLUMN,
                Gap.SMALL
        );
        return footer;
    }

    private Component buildFooterItem(VaadinIcon icon, String text) {
        Div item = new Div();
        item.addClassName("footer-item");

        item.getElement().setProperty("title", text);

        var iconComponent = icon.create();
        iconComponent.addClassName("footer-icon");

        Span label = new Span(text);
        label.addClassName("footer-text");

        item.add(iconComponent, label);
        return item;
    }

}