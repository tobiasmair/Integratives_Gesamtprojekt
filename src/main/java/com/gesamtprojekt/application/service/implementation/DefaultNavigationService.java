package com.gesamtprojekt.application.service.implementation;

import com.gesamtprojekt.application.model.MeetingRoom;
import com.gesamtprojekt.application.model.Exit;
import com.gesamtprojekt.application.service.NavigationServiceInterface;
import org.springframework.stereotype.Service;

@Service
public class DefaultNavigationService implements NavigationServiceInterface {

    @Override
    public int calculateTravelTime(Exit startExit, MeetingRoom endRoom) {
        String endBuilding = endRoom.getLocation();
        int timeBetweenBuildings = startExit.getTimeToBuildings().getOrDefault(endBuilding, Integer.MAX_VALUE);
        int timeToRoom = endRoom.getTimeToExit();
        return timeBetweenBuildings + timeToRoom;
    }

    @Override
    public boolean isBookingPossible(Exit startExit, MeetingRoom endRoom, int bookingTime) {
        int travelTime = calculateTravelTime(startExit, endRoom);
        long currentTimeInSeconds = System.currentTimeMillis() / 1000; // Current time in seconds
        return (currentTimeInSeconds + travelTime) <= bookingTime;
    }
}