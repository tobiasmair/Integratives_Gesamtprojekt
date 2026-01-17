package com.gesamtprojekt.application.repositories;

import com.gesamtprojekt.application.model.Client;
import com.gesamtprojekt.application.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Alle Benachrichtigungen eines Users
    List<Notification> findByBooking_ClientOrderByCreatedAtDesc(Client client);

    // Anzahl ungelesenen Benachrichtigungen
    long countByBooking_ClientAndIsReadFalse(Client client);

    // ungelesene Benachrichtigungen für User
    List<Notification> findByBooking_ClientAndIsReadFalse(Client client);

}
