package com.gesamtprojekt.application.service.implementation;

import com.gesamtprojekt.application.model.ExitDistance;
import com.gesamtprojekt.application.model.MeetingRoom;
import com.gesamtprojekt.application.model.Exit;
import com.gesamtprojekt.application.repositories.ExitDistanceRepository;
import com.gesamtprojekt.application.service.NavigationServiceInterface;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Transactional(readOnly = true)
public class DefaultNavigationService implements NavigationServiceInterface {

    private final ExitDistanceRepository exitDistanceRepository;

    public DefaultNavigationService(ExitDistanceRepository exitDistanceRepository) {
        this.exitDistanceRepository = exitDistanceRepository;
    }

    @Override
    public int calculateTravelTime(Exit startExit, MeetingRoom endRoom) {
        // 1. Grundlegende Null-Checks
        if (startExit == null || endRoom == null || endRoom.getNearestExit() == null) {
            return Integer.MAX_VALUE;
        }

        try {
            // 2. IDs sicher extrahieren (vermeidet Proxy-Probleme bei Objekten)
            Long startId = startExit.getExitId();
            Long endId = endRoom.getNearestExit().getExitId();

            // 3. Wenn man schon am richtigen Ausgang steht
            if (startId.equals(endId)) {
                return endRoom.getTimeToNearestExit() != null ? endRoom.getTimeToNearestExit() : 0;
            }

            // 4. Datenbank abfragen
            Integer timeBetweenExits = exitDistanceRepository.findTimeBetweenExits(startId, endId);

            // 5. WICHTIG: Wenn die DB NULL liefert, nicht addieren!
            if (timeBetweenExits == null) {
                return Integer.MAX_VALUE;
            }

            // 6. Raumzeit addieren (ebenfalls Null-sicher)
            int roomTime = endRoom.getTimeToNearestExit() != null ? endRoom.getTimeToNearestExit() : 0;

            return timeBetweenExits + roomTime;

        } catch (Exception e) {
            // Logge den Fehler, damit du ihn in der Konsole siehst
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