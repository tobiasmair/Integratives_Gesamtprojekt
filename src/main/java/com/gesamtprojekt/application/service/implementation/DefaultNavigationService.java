package com.gesamtprojekt.application.service.implementation;

import com.gesamtprojekt.application.model.ExitDistance;
import com.gesamtprojekt.application.model.MeetingRoom;
import com.gesamtprojekt.application.model.Exit;
import com.gesamtprojekt.application.repositories.ExitDistanceRepository;
import com.gesamtprojekt.application.service.NavigationServiceInterface;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class DefaultNavigationService implements NavigationServiceInterface {

    private final ExitDistanceRepository exitDistanceRepository;

    public DefaultNavigationService(ExitDistanceRepository exitDistanceRepository) {
        this.exitDistanceRepository = exitDistanceRepository;
    }

    @Override
    public int calculateTravelTime(Exit startExit, MeetingRoom endRoom) {
        if (startExit == null || endRoom == null) {
            return Integer.MAX_VALUE;
        }

        int timeBetweenExits = exitDistanceRepository.findTimeBetweenExits(startExit.getId(), endRoom.getNearestExit().getId());

        int timeFromExitToRoom = endRoom.getTimeToNearestExit();

        return timeBetweenExits + timeFromExitToRoom;
    }

    @Override
    public boolean isBookingPossible(Exit startExit, MeetingRoom endRoom, int bookingTime) {
        int travelTime = calculateTravelTime(startExit, endRoom);
        long currentTimeInSeconds = System.currentTimeMillis() / 1000; // Current time in seconds
        return (currentTimeInSeconds + travelTime) <= bookingTime;
    }
}