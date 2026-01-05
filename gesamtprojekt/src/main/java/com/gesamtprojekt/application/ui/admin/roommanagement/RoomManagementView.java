package com.gesamtprojekt.application.ui.admin.roommanagement;

import com.gesamtprojekt.application.service.implementation.EquipmentService;
import com.gesamtprojekt.application.service.implementation.MeetingRoomService;
import com.gesamtprojekt.application.service.implementation.RoomImageStorageService;
import com.gesamtprojekt.application.ui.client.MainLayout;
import com.gesamtprojekt.application.ui.components.admin.RoomManagementStatsBar;
import com.gesamtprojekt.application.ui.components.admin.RoomTableSection;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "roommanagement", layout = MainLayout.class)
@PageTitle("Room Management")
@RolesAllowed("ADMIN")
public class RoomManagementView extends VerticalLayout {

    public RoomManagementView(MeetingRoomService meetingRoomService, EquipmentService equipmentService, RoomImageStorageService imageStorage) {
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        var stats = new RoomManagementStatsBar(meetingRoomService);
        var table = new RoomTableSection(meetingRoomService, equipmentService, imageStorage);

        add(stats, table);
        setFlexGrow(1, table);
    }
}
