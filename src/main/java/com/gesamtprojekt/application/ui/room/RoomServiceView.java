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

    // in case Database is offline
    private boolean isOfflineMode = false;
    private Notification offlineNotification;

    // Class-level variables to preserve state during DB outages
    private Long cachedRoomId = null;
    private String cachedRoomName = "Meeting Room";

    /**
     * Checks the current time and the booking and decides which screen to display.
     * Includes advanced failover logic for database outages.
     */
    private void updateUIState() {
        try {
            LocalDateTime now = LocalDateTime.now();
            List<Booking> bookings;

            // Try to fetch Room and Client info (DB-dependent)
            try {
                Optional<Client> authClient = securityService.getAuthenticatedClient();
                if (authClient.isPresent()) {
                    Optional<MeetingRoom> room = meetingRoomService.findRoomByClient(authClient.get());
                    if (room.isPresent()) {
                        // Update cache variables if DB is reachable
                        this.cachedRoomId = room.get().getRoomId();
                        this.cachedRoomName = room.get().getName();
                    }
                }
                handleOfflineStatusChange(false); // DB connection is healthy
            } catch (Exception e) {
                handleOfflineStatusChange(true); // DB connection failed
                // If DB fails here, proceed using cachedRoomId from previous successful polls
            }

            // 2. Stop if no Room ID is available (neither from DB nor from Cache)
            if (cachedRoomId == null) return;

            // 3. Look up bookings from database (or local memory cache in case of error)
            try {
                bookings = bookingService.findAllActiveBookingsForRoom(cachedRoomId);
                handleOfflineStatusChange(false); // Database online
            } catch (Exception e) {
                handleOfflineStatusChange(true); // Database offline
                // Service returns the local in-memory data
                bookings = bookingService.getCachedBookings(cachedRoomId);
            }

            // 4. Determine relevant bookings (must be isActive & status must be CONFIRMED)
            Optional<Booking> currentBooking = bookings.stream()
                    .filter(b -> b.getIsActive() && "CONFIRMED".equals(b.getBookingStatus()))
                    .filter(b -> b.getEndTime().isAfter(now))
                    .filter(b -> b.getStartTime().minusMinutes(1).isBefore(now))
                    .findFirst();

            String newStatus;
            Long newBookingId = null;

            if (currentBooking.isPresent()) {
                Booking b = currentBooking.get();
                newBookingId = b.getBookingId();

                if (b.getCheckInTime() != null) {
                    long minutesLeft = java.time.Duration.between(now, b.getEndTime()).toMinutes();
                    // If time until end of the booking <= 5mins - show warning/reminder
                    newStatus = (minutesLeft <= 5) ? "DASHBOARD_WARNING" : "DASHBOARD";
                } else if (now.isAfter(b.getStartTime().plusMinutes(5))) {
                    // Change status to MISSED if user doesn't check in within 5 minutes
                    newStatus = "MISSED";
                } else {
                    // Show check-in / lock screen
                    newStatus = "CHECKIN";
                }
            } else {
                // No current booking: show room name and upcoming schedule
                newStatus = "DEFAULT";
            }

            // 5. Refresh UI only if the booking state or the specific booking ID has changed
            if (!newStatus.equals(currentViewStatus) ||
                    (newBookingId != null && !newBookingId.equals(currentActiveBookingId))) {

                mainContent.removeAll();
                currentViewStatus = newStatus;
                currentActiveBookingId = newBookingId;

                switch (newStatus) {
                    case "DEFAULT" -> mainContent.add(new RoomDefaultScreen(cachedRoomName, bookings));
                    case "MISSED" -> {
                        markAsMissed(currentBooking.get());
                        updateUIState(); // Recursive call to switch to DEFAULT screen immediately
                    }
                    case "CHECKIN" -> showLockScreen(currentBooking.get());
                    default -> {
                        if (newStatus.startsWith("DASHBOARD")) {
                            showDashboard(currentBooking.get());
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Global Catch for unexpected errors to prevent the UI from freezing
            // This log is only for debugging; visual feedback is handled via handleOfflineStatusChange
            System.err.println("Critical UI Update Error: " + e.getMessage());
        }
    }

    /**
     * controls display of notifications for offline-mode
     */
    private void handleOfflineStatusChange(boolean nowOffline) {
        if (nowOffline && !isOfflineMode) {
            isOfflineMode = true;
            offlineNotification = new Notification("⚠️ Database Connection Lost - Using Offline Data", 0);
            offlineNotification.addThemeVariants(NotificationVariant.LUMO_CONTRAST);
            offlineNotification.setPosition(Notification.Position.BOTTOM_START);
            offlineNotification.open();
        } else if (!nowOffline && isOfflineMode) {
            isOfflineMode = false;
            if (offlineNotification != null) {
                offlineNotification.close();
                Notification.show("Connection restored. Syncing data...", 3000, Notification.Position.BOTTOM_START)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            }
        }
    }

    /**
     * shows Lock-Screen with code field (Check-in Dialogue)
     */
    private void showLockScreen(Booking b) {
        mainContent.add(new RoomLockScreen(() -> {
            new CheckInDialog(b, () -> {
                try {
                    b.setCheckInTime(LocalDateTime.now());
                    bookingService.updateBooking(b);
                    Notification.show("Check-in successful!", 3000, Notification.Position.TOP_CENTER)
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                } catch (Exception e) {
                    // Lokaler Check-In trotz DB-Fehler erlauben
                    Notification.show("Offline Check-in: Sync pending...", 5000, Notification.Position.MIDDLE)
                            .addThemeVariants(NotificationVariant.LUMO_WARNING);
                }
                currentViewStatus = ""; // Status zurücksetzen für Refresh
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
     * Sets 'endTime' on current time and sets status to 'COMPLETED'
     */
    private void finishBooking(Booking b) {
        try {
            b.setEndTime(LocalDateTime.now());
            b.setBookingStatus("COMPLETED"); // Status Update
            bookingService.updateBooking(b);
            Notification.show("Meeting completed.", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (Exception e) {
            // allow to finish booking, even if the database is offline
            Notification.show("Offline: Saved locally, syncing later...", 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_WARNING);
        }
        currentViewStatus = "";
        updateUIState();
    }

    /**
     * Sets status to MISSED on no-show
     */
    private void markAsMissed(Booking b) {
        try {
            b.setBookingStatus("MISSED"); // Status Update
            b.setEndTime(LocalDateTime.now());
            bookingService.updateBooking(b);
            Notification.show("Booking canceled due to no-show.", 5000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (Exception e) {
            Notification.show("Offline: Sync pending...", 5000, Notification.Position.MIDDLE);
        }
    }
}
