package com.gesamtprojekt.application.service.implementation;

import com.gesamtprojekt.application.model.MeetingRoom;
import com.gesamtprojekt.application.repositories.MeetingRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MeetingRoomService {

    private final MeetingRoomRepository meetingRoomRepository;

    // Aktive Räume finden
    public List<MeetingRoom> findAvailableRooms() {
        return meetingRoomRepository.findByIsActiveTrue();
    }

    // Aktive Räume in Zeitabschnitt
    public List<MeetingRoom> findAvailableRoomsInTimeframe(
            LocalDateTime startTime,
            LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            return findAvailableRooms();
        }
        return meetingRoomRepository.findAvailableRoomsInTimeframe(startTime, endTime);
    }

    public List<MeetingRoom> findAvailableRoomsExcludingBooking(
            LocalDateTime startTime,
            LocalDateTime endTime,
            Long excludeBookingId) {
        if (startTime == null || endTime == null) {
            return findAvailableRooms();
        }
        return meetingRoomRepository.findAvailableRoomsExcludingBooking(startTime, endTime, excludeBookingId);
    }

}
