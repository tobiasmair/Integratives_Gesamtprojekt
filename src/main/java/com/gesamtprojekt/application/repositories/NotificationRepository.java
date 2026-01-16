package com.gesamtprojekt.application.repositories;

import com.gesamtprojekt.application.model.Client;
import com.gesamtprojekt.application.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Alle Benachrichtigungen eines Users
    List<Notification> findByClientOrderByCreatedAtDesc(Client client);

    // Anzahl ungelesenen Benachrichtigungen
    long countByClientAndIsReadFalse(Client client);

    // ungelesene Benachrichtigungen für User
    List<Notification> findByClientAndIsReadFalse(Client client);

}
