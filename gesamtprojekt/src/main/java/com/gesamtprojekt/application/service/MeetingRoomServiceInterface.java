package com.gesamtprojekt.application.service;

import com.gesamtprojekt.application.model.MeetingRoom;

import java.util.List;

public interface MeetingRoomServiceInterface {

    // Liste aller Räume basierend auf Suchbegriff und Filtern
    List<MeetingRoom> findAllRooms(String search, String building, String status);

    // Raum anlegen
    MeetingRoom createRoom(MeetingRoom room);

    // Raum aktualisieren
    void updateRoom(MeetingRoom room);

    // Raum löschen (häufig Soft-Delete)
    void deleteRoom(MeetingRoom room);

    // Statistiken
    long countRooms();

    int sumCapacity();

    long countBuildings();
}
