package com.gesamtprojekt.application.ui.room;

import com.gesamtprojekt.application.model.Client;
import com.gesamtprojekt.application.model.MeetingRoom;
import com.gesamtprojekt.application.service.implementation.MeetingRoomService;
import com.gesamtprojekt.application.ui.room.components.ButtonContainer;
import com.gesamtprojekt.application.ui.room.components.CheckInDialog;
import com.gesamtprojekt.application.ui.room.components.RoomFooter;
import com.gesamtprojekt.application.ui.room.components.RoomHeader;
import com.gesamtprojekt.application.ui.room.screens.RoomDefaultScreen;
import com.gesamtprojekt.application.ui.room.screens.RoomLockScreen;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
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


@Route(value = "roomservice")
@PageTitle("Room Service")
@RolesAllowed("ROOM")
public class RoomServiceView extends VerticalLayout {

    private final BookingService bookingService;
    private final SecurityService securityService;
    private final MeetingRoomService meetingRoomService;
    private final VerticalLayout mainContent = new VerticalLayout();

    public RoomServiceView(BookingService bookingService, SecurityService securityService, MeetingRoomService meetingRoomService) {
        this.bookingService = bookingService;
        this.securityService = securityService;
        this.meetingRoomService = meetingRoomService;

        setSizeFull();
        setPadding(false);
        setSpacing(false);

        mainContent.setSizeFull();
        mainContent.setPadding(false);

        // center content
        mainContent.setAlignItems(Alignment.CENTER);
        mainContent.setJustifyContentMode(JustifyContentMode.CENTER);

        add(new RoomHeader(), mainContent, new RoomFooter(securityService));

        updateUIState();

        UI.getCurrent().setPollInterval(10000);
        UI.getCurrent().addPollListener(e -> updateUIState());
    }

    private void updateUIState() {
        mainContent.removeAll();
        LocalDateTime now = LocalDateTime.now();

        Optional<Client> authClient = securityService.getAuthenticatedClient();

        if (authClient.isEmpty()) {
            mainContent.add(new H1("Fehler: Nicht eingeloggt"));
            return;
        }

        // Nutze die Instanz-Variable 'meetingRoomService', nicht die Klasse!
        Optional<MeetingRoom> room = meetingRoomService.findRoomByClient(authClient.get());

        if (room.isEmpty()) {
            mainContent.add(new H1("Fehler: Diesem Account ist kein Raum zugeordnet"));
            return;
        }

        // 3. Jetzt haben wir die ECHTE roomId für die Buchungstabelle
        Long roomId = room.get().getRoomId();
        String roomDisplayName = room.get().getName();

        // 4. Buchungen für diesen speziellen Raum laden
        List<Booking> bookings = bookingService.findAllActiveBookingsForRoom(roomId);

        Optional<Booking> currentBooking = bookings.stream()
                .filter(b -> b.getIsActive() && b.getEndTime().isAfter(now))
                .filter(b -> b.getStartTime().minusMinutes(1).isBefore(now))
                .findFirst();

        if (currentBooking.isPresent()) {
            handleActiveBooking(currentBooking.get(), bookings, now);
        } else {
            // Hier übergeben wir den echten Raumnamen aus dem MeetingRoom Model
            mainContent.add(new RoomDefaultScreen(roomDisplayName, bookings));
        }
    }

    private void handleActiveBooking(Booking b, List<Booking> all, LocalDateTime now) {
        if (b.getCheckInTime() == null && now.isAfter(b.getStartTime().plusMinutes(5))) {
            cancelBooking(b);
            showDefault(all);
        } else if (b.getCheckInTime() != null) {
            showDashboard();
        } else {
            mainContent.add(new RoomLockScreen(() -> {
                new CheckInDialog(b, () -> {
                    b.setCheckInTime(LocalDateTime.now());
                    bookingService.updateBooking(b);
                    Notification.show("Check-in successful!", 3000, Notification.Position.TOP_CENTER)
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    updateUIState();
                }).open();
            }));
        }
    }

    private void showDefault(List<Booking> bookings) {
        String roomName = securityService.getAuthenticatedClient()
                .map(user -> user.getUsername()).orElse("Room name not found");
        mainContent.add(new RoomDefaultScreen(roomName, bookings));
    }

    private void showDashboard() {
        ButtonContainer dashboard = new ButtonContainer();
        dashboard.addButton("Light", VaadinIcon.LIGHTBULB);
        dashboard.addButton("AC", VaadinIcon.CONTROLLER);
        dashboard.addButton("Beamer", VaadinIcon.UPLOAD_ALT);
        mainContent.add(dashboard);
    }

    private void cancelBooking(Booking b) {
        b.setIsActive(false);
        bookingService.updateBooking(b);
        Notification.show("Booking canceled due to no-show.", 5000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
