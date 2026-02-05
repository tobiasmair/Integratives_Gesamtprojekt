package com.gesamtprojekt.application.ui.client.calendar;

import com.gesamtprojekt.application.events.RoomChangedBroadcaster;
import com.gesamtprojekt.application.security.SecurityService;
import com.gesamtprojekt.application.service.implementation.*;
import com.gesamtprojekt.application.ui.client.MainLayout;
import com.gesamtprojekt.application.ui.components.calendar.CalendarControlsBar;
import com.gesamtprojekt.application.ui.components.calendar.CalendarRoomsSection;
import com.gesamtprojekt.application.ui.components.calendar.ViewMode;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.Registration;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "calendar", layout = MainLayout.class)
@PageTitle("Calendar | MCI Meeting Booker")
@RolesAllowed({"USER", "ADMIN"})
public class CalendarView extends VerticalLayout {

    private Registration broadcasterRegistration;
    private CalendarControlsBar controls;
    private CalendarRoomsSection rooms;

    public CalendarView(MeetingRoomService meetingRoomService, BookingService bookingService,
                        SecurityService securityService, EquipmentService equipmentService, ExitService exitService, DefaultNavigationService defaultNavigationService) {
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        controls = new CalendarControlsBar(equipmentService);
        rooms = new CalendarRoomsSection(meetingRoomService, bookingService, securityService, exitService, defaultNavigationService);

        // Initiales laden der Daten
        rooms.reload(
                controls.getStartDateTime(),
                controls.getEndDateTime(),
                controls.getBuilding(),
                controls.getFloor(),
                controls.getCapacity(),
                controls.getEquipment()
        );

        controls.addFilterChangedListener(e -> {
            rooms.reload(
                    controls.getStartDateTime(),
                    controls.getEndDateTime(),
                    controls.getBuilding(),
                    controls.getFloor(),
                    controls.getCapacity(),
                    controls.getEquipment()
            );
        });

        controls.addModeChangedListener(e -> {
            if (e.getMode() == ViewMode.CALENDAR) {
                rooms.setHeading("Available Meeting Rooms");
            } else {
                rooms.setHeading("Browse All Rooms");
            }
        });

        rooms.addBookingCreatedListener(e -> {
            System.out.println("DEBUG: BookingCreatedInSectionEvent received in CalendarView, reloading grid");
            rooms.reload(
                    controls.getStartDateTime(),
                    controls.getEndDateTime(),
                    controls.getBuilding(),
                    controls.getFloor(),
                    controls.getCapacity(),
                    controls.getEquipment()
            );
        });

        rooms.addClassName("calendar-rooms-scroll");

        add(controls, rooms);
        setFlexGrow(1, rooms);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        broadcasterRegistration = RoomChangedBroadcaster.register(event -> {
            attachEvent.getUI().access(() -> {
                rooms.reload(
                        controls.getStartDateTime(),
                        controls.getEndDateTime(),
                        controls.getBuilding(),
                        controls.getFloor(),
                        controls.getCapacity(),
                        controls.getEquipment()
                );
            });
        });
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        if (broadcasterRegistration != null) {
            broadcasterRegistration.remove();
        }
    }
}
