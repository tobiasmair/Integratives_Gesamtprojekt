package com.gesamtprojekt.application.ui.client.help;

import com.gesamtprojekt.application.ui.client.MainLayout;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "help", layout = MainLayout.class)
@PageTitle("Help | MCI Meeting Booker")
@RolesAllowed({"USER", "ADMIN", "ROOM"})
public class HelpView extends VerticalLayout {

    private final VerticalLayout contentArea = new VerticalLayout();

    public HelpView() {
        setAlignItems(Alignment.CENTER);
        setPadding(true);
        setSpacing(true);
        setSizeFull();

        VerticalLayout mainContent = new VerticalLayout();
        mainContent.setPadding(false);
        mainContent.setSpacing(true);
        mainContent.setAlignItems(Alignment.STRETCH);

        H1 title = new H1("Help & Support");

        Tab faqTab = new Tab("FAQ's");
        Tab infoTab = new Tab("Info");
        Tab impressumTab = new Tab("Impressum");

        Tabs tabs = new Tabs(faqTab, infoTab, impressumTab);
        tabs.setWidthFull();

        contentArea.setPadding(false);
        contentArea.setSpacing(true);
        contentArea.setWidthFull();

        tabs.addSelectedChangeListener(event -> {
            Tab selectedTab = event.getSelectedTab();
            updateContent(selectedTab);
        });

        updateContent(faqTab);

        mainContent.add(title, tabs, contentArea);
        add(mainContent);
    }

    private void updateContent(Tab selectedTab) {
        contentArea.removeAll();

        if (selectedTab.getLabel().equals("FAQ's")) {
            contentArea.add(buildFaqContent());
        } else if (selectedTab.getLabel().equals("Info")) {
            contentArea.add(buildInfoContent());
        } else if (selectedTab.getLabel().equals("Impressum")) {
            contentArea.add(buildImpressumContent());
        }
    }

    private VerticalLayout buildFaqContent() {
        VerticalLayout faqLayout = new VerticalLayout();
        faqLayout.setPadding(false);
        faqLayout.setSpacing(true);

        H2 faqTitle = new H2("Frequently Asked Questions");
        faqLayout.add(faqTitle);

        /* FAQ 1
        Paragraph p1 = new Paragraph(
                "To book a meeting room, navigate to 'Book Rooms' in the sidebar.\n" +
                        "Select your desired date and time slot in the calendar view.\n" +
                        "Available rooms will be displayed, and you can confirm your booking by clicking on the room."
        );
        p1.getStyle().set("white-space", "pre-line");

        Details faq1 = new Details("How do I book a meeting room?", p1);
        faq1.setWidthFull();


// FAQ 2
        Paragraph p2 = new Paragraph(
                "Yes, you can cancel your bookings from the Dashboard.\n" +
                        "Navigate to 'My Dashboard' to view all your bookings.\n" +
                        "Click on a booking to see details and cancel it if needed.\n" +
                        "Please note that modifications require canceling the existing booking and creating a new one."
        );
        p2.getStyle().set("white-space", "pre-line");

        Details faq2 = new Details("Can I cancel or modify my booking?", p2);
        faq2.setWidthFull();


// FAQ 3
        Paragraph p3 = new Paragraph(
                "You can book rooms up to 3 months in advance.\n" +
                        "This ensures fair availability for all users and allows for proper room management."
        );
        p3.getStyle().set("white-space", "pre-line");

        Details faq3 = new Details("How far in advance can I book a room?", p3);
        faq3.setWidthFull();


// FAQ 4
        Paragraph p4 = new Paragraph(
                "If you arrive at your booked room and it's occupied, please check the room door display.\n" +
                        "If there's a conflict, contact the IT support team immediately at support@mci.edu.\n" +
                        "Our team will help resolve the issue as quickly as possible."
        );
        p4.getStyle().set("white-space", "pre-line");

        Details faq4 = new Details("What should I do if the room is occupied at my booking time?", p4);
        //faq4.setBorrowed(false);
        faq4.setWidthFull();


// FAQ 5
        Paragraph p5 = new Paragraph(
                "To change your password, click on 'My Profile' in the footer navigation.\n" +
                        "In the profile section, you'll find a password field.\n" +
                        "Enter your new password, confirm it, and click 'Save Profile' to update."
        );
        p5.getStyle().set("white-space", "pre-line");

        Details faq5 = new Details("How do I change my password?", p5);
        faq5.setWidthFull();


// FAQ 6
        Paragraph p6 = new Paragraph(
                "For technical support or any issues with the booking system,\n" +
                        "please contact our IT support team at support@mci.edu.\n" +
                        "Our team is available during business hours to assist you."
        );
        p6.getStyle().set("white-space", "pre-line");

        Details faq6 = new Details("Who can I contact for technical support?", p6);
        faq6.setWidthFull();


// FAQ 7
        Paragraph p7 = new Paragraph(
                "Yes, you can book multiple rooms for the same time slot if needed.\n" +
                        "Simply make separate bookings for each room you require.\n" +
                        "All your bookings will be visible in your dashboard."
        );
        p7.getStyle().set("white-space", "pre-line");

        Details faq7 = new Details("Can I book multiple rooms at the same time?", p7);
        faq7.setWidthFull();


// FAQ 8
        Paragraph p8 = new Paragraph(
                "The room door display shows the current booking status,\n" +
                        "upcoming bookings for the day, and real-time availability.\n" +
                        "This helps you quickly see if a room is available for immediate use."
        );
        p8.getStyle().set("white-space", "pre-line");

        Details faq8 = new Details("What information is shown on the room door display?", p8);
        faq8.setWidthFull();*/


// FAQ 9
        Paragraph p9 = new Paragraph(
                "Yes, that is possible. Some rooms can be connected to book meeting rooms for very large groups.\n" +
                        "At the moment, rooms must be booked individually via the booking app.\n" +
                        "The following rooms can be connected:\n" +
                        "301 + 302\n" +
                        "405 + 406"
        );
        p9.getStyle().set("white-space", "pre-line");

        Details faq9 = new Details(
                "Is it possible to combine two or more rooms into one larger room?",
                p9
        );
        faq9.setWidthFull();

        faqLayout.add(faq9);

        return faqLayout;
    }

    private VerticalLayout buildInfoContent() {
        VerticalLayout infoLayout = new VerticalLayout();
        infoLayout.setPadding(false);

        H2 infoTitle = new H2("Information");
        Paragraph infoText = new Paragraph("Info content coming soon...");

        infoLayout.add(infoTitle, infoText);
        return infoLayout;
    }

    private VerticalLayout buildImpressumContent() {
        VerticalLayout impressumLayout = new VerticalLayout();
        impressumLayout.setPadding(false);

        H2 impressumTitle = new H2("Impressum");
        Paragraph impressumText = new Paragraph("Impressum content coming soon...");

        impressumLayout.add(impressumTitle, impressumText);
        return impressumLayout;
    }
}
