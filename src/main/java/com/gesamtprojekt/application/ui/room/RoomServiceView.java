package com.gesamtprojekt.application.ui.room;

import com.gesamtprojekt.application.model.Client;
import com.gesamtprojekt.application.model.MeetingRoom;
import com.gesamtprojekt.application.service.implementation.MeetingRoomService;
import com.gesamtprojekt.application.ui.room.components.*;
import com.gesamtprojekt.application.ui.room.screens.RoomDefaultScreen;
import com.gesamtprojekt.application.ui.room.screens.RoomLockScreen;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.gesamtprojekt.application.model.Booking;
import com.gesamtprojekt.application.security.SecurityService;
import com.gesamtprojekt.application.service.implementation.BookingService;
import jakarta.annotation.security.RolesAllowed;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Main view for the room terminal / tablet
 * This view coordinates the lifecycle of the room regarding the state of a booking: Default-Screen, Lock-Screen and Control-Dashboard.
 */
@Route(value = "roomservice")
@PageTitle("Room | MCI Meeting Booker")
@RolesAllowed("ROOM")
public class RoomServiceView extends VerticalLayout {

    private final BookingService bookingService;
    private final SecurityService securityService;
    private final MeetingRoomService meetingRoomService;
    private final VerticalLayout mainContent = new VerticalLayout();

    /** * STATE-TRACKING: Avoids unnecessary UI-refresh.
     * only if the state of the booking changes, the UI gets refreshed
     */
    private String currentViewStatus = "";
    private Long currentActiveBookingId = null;

    public RoomServiceView(BookingService bookingService,
                           SecurityService securityService,
                           MeetingRoomService meetingRoomService) {
        this.bookingService = bookingService;
        this.securityService = securityService;
        this.meetingRoomService = meetingRoomService;

        setSizeFull();
        setPadding(false);
        setSpacing(false);

        mainContent.setSizeFull();
        mainContent.setPadding(false);
        mainContent.setAlignItems(Alignment.CENTER);
        mainContent.setJustifyContentMode(JustifyContentMode.CENTER);

        add(new RoomHeader(), mainContent, new RoomFooter(securityService));

        updateUIState();

        // Automatic refresh every 10 seconds
        UI.getCurrent().setPollInterval(10000);
        UI.getCurrent().addPollListener(e -> updateUIState());
    }

    /**
     * Checks the current time and the booking and decides which screen to display
     * (Default-Screen, Lock-Screen or Dashboard-Screen)
     */
    private void updateUIState() {
        LocalDateTime now = LocalDateTime.now();

        // get room client
        Optional<Client> authClient = securityService.getAuthenticatedClient();
        if (authClient.isEmpty()) return;

        // map the according room
        Optional<MeetingRoom> room = meetingRoomService.findRoomByClient(authClient.get());
        if (room.isEmpty()) return;

        Long roomId = room.get().getRoomId();
        List<Booking> bookings = bookingService.findAllActiveBookingsForRoom(roomId);

        // check if there are active bookings for this room
        Optional<Booking> currentBooking = bookings.stream()
                .filter(b -> b.getIsActive() && b.getEndTime().isAfter(now))
                .filter(b -> b.getStartTime().minusMinutes(1).isBefore(now))
                .findFirst();

        String newStatus;
        Long newBookingId = null;

        // get state of the booking
        if (currentBooking.isPresent()) {
            Booking b = currentBooking.get();
            newBookingId = b.getBookingId();

            if (b.getCheckInTime() != null) {
                long minutesLeft = java.time.Duration.between(now, b.getEndTime()).toMinutes();

                // Dashboard View
                if (minutesLeft <= 5) {
                    // if time till end of booking <= 5 mins - show reminder
                    newStatus = "DASHBOARD_WARNING";
                } else {
                    // show normal dashboard panel
                    newStatus = "DASHBOARD";
                }
            } else if (now.isAfter(b.getStartTime().plusMinutes(5))) {
                // Cancel booking if user doesn't check in the room in time (bookingStart + 5 mins)
                newStatus = "AUTO_CANCEL";
            } else {
                // show check in screen
                newStatus = "LOCKED";
            }
        } else {
            // show room name and (if any) future bookings
            newStatus = "DEFAULT";
        }

        // Refresh UI if there is a change in the database
        if (!newStatus.equals(currentViewStatus) ||
                (newBookingId != null && !newBookingId.equals(currentActiveBookingId))) {

            mainContent.removeAll();
            currentViewStatus = newStatus;
            currentActiveBookingId = newBookingId;

            switch (newStatus) {
                case "DEFAULT" -> mainContent.add(new RoomDefaultScreen(room.get().getName(), bookings));
                case "AUTO_CANCEL" -> {
                    cancelBooking(currentBooking.get());
                    updateUIState(); // switch to default screen
                }
                case "LOCKED" -> showLockScreen(currentBooking.get());
                default -> {
                    showDashboard(currentBooking.get());
                }
            }
        }
    }

    /**
     * shows Lock-Screen with code field (Check-in Dialogue)
     */
    private void showLockScreen(Booking b) {
        mainContent.add(new RoomLockScreen(() -> {
            new CheckInDialog(b, () -> {
                b.setCheckInTime(LocalDateTime.now());
                bookingService.updateBooking(b);
                Notification.show("Check-in successful!", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                // reset state so updateUIState() shows the Dashboard
                currentViewStatus = "";
                updateUIState();
            }).open();
        }));
    }

    /**
     * displays the Dashboard-Screen with Info- and Control Elements
     */
    private void showDashboard(Booking b) {
        HorizontalLayout dashboardLayout = new HorizontalLayout();
        dashboardLayout.setAlignItems(Alignment.CENTER);
        dashboardLayout.setSpacing(true);

        BookingInfoBox infoBox = new BookingInfoBox(b, () -> confirmFinishBooking(b));

        ButtonContainer buttons = new ButtonContainer();
        buttons.addDoorLockButton();
        buttons.addButton("Light", VaadinIcon.LIGHTBULB, "Light on", "Light off");
        buttons.addButton("Blinds", VaadinIcon.SUN_O, "Blinds up", "Blinds down");
        buttons.addButton("Whiteboard", VaadinIcon.PRESENTATION, "Whiteboard on", "Whiteboard off");
        buttons.addButton("Beamer", VaadinIcon.FILM, "Beamer on", "Beamer off");
        buttons.addButton("AC", VaadinIcon.CONTROLLER, "AC on", "AC off");

        dashboardLayout.add(infoBox, buttons);
        mainContent.add(dashboardLayout);
    }

    /**
     * Double-Check if user really wants to finish the booking early
     */
    private void confirmFinishBooking(Booking b) {
        com.vaadin.flow.component.dialog.Dialog confirmDialog = new com.vaadin.flow.component.dialog.Dialog();
        confirmDialog.setHeaderTitle("End Meeting?");
        confirmDialog.add(new com.vaadin.flow.component.html.Paragraph("Do you really want to finish the booking early?"));

        com.vaadin.flow.component.button.Button confirmBtn = new com.vaadin.flow.component.button.Button("Yes, Finish", e -> {
            finishBooking(b);
            confirmDialog.close();
        });
        confirmBtn.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_PRIMARY, com.vaadin.flow.component.button.ButtonVariant.LUMO_ERROR);

        confirmDialog.getFooter().add(new com.vaadin.flow.component.button.Button("Cancel", e -> confirmDialog.close()), confirmBtn);
        confirmDialog.open();
    }

    /**
     * Sets 'endTime' on current time and ends the booking
     */
    private void finishBooking(Booking b) {
        b.setEndTime(LocalDateTime.now());
        bookingService.updateBooking(b);
        Notification.show("Meeting finished early.", 3000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        updateUIState();
    }

    /**
     * deactivates the booking on no-show
     */
    private void cancelBooking(Booking b) {
        b.setIsActive(false);
        bookingService.updateBooking(b);
        Notification.show("Booking canceled due to no-show.", 5000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
