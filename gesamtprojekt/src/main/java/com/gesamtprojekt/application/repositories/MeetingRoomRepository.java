package com.gesamtprojekt.application.repositories;

import com.gesamtprojekt.application.model.MeetingRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingRoomRepository extends JpaRepository<MeetingRoom, Long> {
}
