package com.gesamtprojekt.application.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.server.menu.MenuConfiguration;
import com.vaadin.flow.theme.lumo.LumoUtility;
import static com.vaadin.flow.theme.lumo.LumoUtility.FlexDirection;
import static com.vaadin.flow.theme.lumo.LumoUtility.Gap;

@CssImport("./themes/gesamtprojekt/main-layout.css")
@Layout
@AnonymousAllowed
public class MainLayout extends AppLayout implements AfterNavigationObserver {

    //auskommentiert, weil im Moment 2 Titel!!!!
    //private com.vaadin.flow.component.html.H1 viewTitle;


    public MainLayout() {
        setPrimarySection(Section.DRAWER);
        addDrawerContent();
        //addHeaderContent();
        getStyle().setHeight("100%");
    }

    /*private void addHeaderContent() {

        viewTitle = new com.vaadin.flow.component.html.H1();
        viewTitle.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.Margin.NONE, LumoUtility.Padding.SMALL);
        addToNavbar(true, viewTitle);

    }*/

    private void addDrawerContent() {
        Span appName = new Span("MCI - meeting room booker");
        appName.addClassNames(LumoUtility.FontWeight.SEMIBOLD, LumoUtility.FontSize.SMALL);
        appName.addClassName("app-name");

        Button collapse = new Button(VaadinIcon.ANGLE_LEFT.create());
        collapse.addClassName("collapse-btn");
        collapse.addClickListener(e -> toggleCollapsed(collapse));

        Header header = new Header(appName, collapse);
        header.addClassName("drawer-header");

        Scroller scroller = new Scroller(createNavigation());
        scroller.addClassName("drawer-scroller");
        scroller.setSizeFull();

        Footer footer = createFooter();
        footer.addClassName("drawer-footer");

        FlexLayout drawer = new FlexLayout(header, createProfileSection(), scroller, footer);
        drawer.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        drawer.setSizeFull();
        drawer.addClassName("drawer");

        addToDrawer(drawer);
    }

    private Component createProfileSection() {
        var avatar = VaadinIcon.USER.create();
        avatar.getStyle().set("margin-right", "var(--lumo-space-s)");

        var name = new Span("Dr. Andrea Corradini");
        name.addClassName("profile-name");

        var role = new Span("Admin");
        role.addClassName("profile-role");

        var text = new VerticalLayout(name, role);
        text.setPadding(false);
        text.setSpacing(false);
        text.setMargin(false);

        var box = new com.vaadin.flow.component.html.Div(avatar, text);
        box.addClassName("profile-box");
        box.getStyle().setHeight("50px");
        return box;
    }

    private SideNav createNavigation() {
        SideNav nav = new SideNav();
        nav.addClassName("sidenav");

        nav.addItem(new SideNavItem("My Dashboard", "dashboard", VaadinIcon.HOME.create()));
        nav.addItem(new SideNavItem("Calendar", "calendar", VaadinIcon.CALENDAR.create()));
        nav.addItem(new SideNavItem("Browse Rooms", "browserooms", VaadinIcon.SEARCH.create()));

        SideNavItem admin = new SideNavItem("Admin", "statistics", VaadinIcon.COG.create());
        admin.addItem(new SideNavItem("Statistics & Reports", "statistics", VaadinIcon.CHART.create()));
        admin.addItem(new SideNavItem("Room Management", "roommanagement", VaadinIcon.BUILDING.create()));
        admin.addItem(new SideNavItem("User Management", "usermanagement", VaadinIcon.USERS.create()));
        admin.addItem(new SideNavItem("System Settings", "systemsettings", VaadinIcon.TOOLS.create()));

        nav.addItem(admin);


        return nav;
    }



    private Footer createFooter() {
        Footer layout = new Footer();

        layout.add(new Span("My Profile"));
        layout.add(new Span("Logout"));
        layout.add(new Span("Help"));
        layout.addClassNames(LumoUtility.Display.FLEX, FlexDirection.COLUMN, Gap.SMALL);

        return layout;
    }

    private void toggleCollapsed(Button collapseButton) {
        boolean collapsed = getElement().getClassList().contains("drawer-collapsed");

        if (collapsed) {
            getElement().getClassList().remove("drawer-collapsed");
            collapseButton.setIcon(VaadinIcon.ANGLE_LEFT.create());
        } else {
            getElement().getClassList().add("drawer-collapsed");
            collapseButton.setIcon(VaadinIcon.ANGLE_RIGHT.create());
        }
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        //viewTitle.setText(getCurrentPageTitle());
        //viewTitle.getStyle().setWidth("100%");
    }

    private String getCurrentPageTitle() {
        return MenuConfiguration.getPageHeader(getContent()).orElse("");
    }
}
