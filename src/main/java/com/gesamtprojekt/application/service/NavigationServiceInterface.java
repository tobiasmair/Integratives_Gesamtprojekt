package com.gesamtprojekt.application.service;

import com.gesamtprojekt.application.model.Exit;
import com.gesamtprojekt.application.model.MeetingRoom;

public interface NavigationServiceInterface {
    int calculateTravelTime(Exit startExit, MeetingRoom endRoom);
    boolean isBookingPossible(Exit startExit, MeetingRoom endRoom, int bookingTime);
}