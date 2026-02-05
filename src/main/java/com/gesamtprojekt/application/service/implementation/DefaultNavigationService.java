package com.gesamtprojekt.application.service.implementation;

import com.gesamtprojekt.application.model.MeetingRoom;
import com.gesamtprojekt.application.model.Exit;
import com.gesamtprojekt.application.repositories.ExitDistanceRepository;
import com.gesamtprojekt.application.service.NavigationServiceInterface;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DefaultNavigationService implements NavigationServiceInterface {

    private final ExitDistanceRepository exitDistanceRepository;

    public DefaultNavigationService(ExitDistanceRepository exitDistanceRepository) {
        this.exitDistanceRepository = exitDistanceRepository;
    }

    @Override
    public int calculateTravelTime(Exit startExit, MeetingRoom endRoom) {
        if (startExit == null || endRoom == null || endRoom.getNearestExit() == null) {
            return Integer.MAX_VALUE;
        }

        try {
            // ID extrahieren
            Long startId = startExit.getExitId();
            Long endId = endRoom.getNearestExit().getExitId();

            // Wenn man schon am richtigen Ausgang steht
            if (startId.equals(endId)) {
                return endRoom.getTimeToNearestExit() != null ? endRoom.getTimeToNearestExit() : 0;
            }

            // Datenbank abfragen
            Integer timeBetweenExits = exitDistanceRepository.findTimeBetweenExits(startId, endId);

            if (timeBetweenExits == null) {
                return Integer.MAX_VALUE;
            }

            // Raumzeit addieren
            int roomTime = endRoom.getTimeToNearestExit() != null ? endRoom.getTimeToNearestExit() : 0;

            // Zeit zwischen Gebäuden + Raum bis Exit
            return timeBetweenExits + roomTime;

        } catch (Exception e) {
            e.printStackTrace();
            return Integer.MAX_VALUE;
        }
    }

    @Override
    public boolean isBookingPossible(Exit startExit, MeetingRoom endRoom, int bookingTime) {
        int travelTime = calculateTravelTime(startExit, endRoom);
        long currentTimeInSeconds = System.currentTimeMillis() / 1000; // Current time in seconds
        return (currentTimeInSeconds + travelTime) <= bookingTime;
    }
}