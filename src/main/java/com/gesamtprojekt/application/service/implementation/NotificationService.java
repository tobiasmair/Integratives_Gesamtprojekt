package com.gesamtprojekt.application.service.implementation;

import com.gesamtprojekt.application.model.Booking;
import com.gesamtprojekt.application.model.Client;
import com.gesamtprojekt.application.model.MeetingRoom;
import com.gesamtprojekt.application.model.Notification;
import com.gesamtprojekt.application.repositories.NotificationRepository;
import com.gesamtprojekt.application.service.dto.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public void createNotification(Booking booking, NotificationType type) {
        Notification notification = new Notification();
        notification.setType(type);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setRead(false);
        notification.setBooking(booking);

        notificationRepository.save(notification);
    }

    // Alle Benachrichtigungen für den aktuellen User laden
    public List<Notification> findAllByUser(Client client) {
        return notificationRepository.findByBooking_ClientOrderByCreatedAtDesc(client);
    }

    // Zählen der ungelesenen Nachrichten
    public long countUnread(Client client) {
        if (client == null) return 0;
        return notificationRepository.countByBooking_ClientAndIsReadFalse(client);
    }

    // Alle Nachrichten eines Users als gelesen markieren
    @Transactional
    public void markAllAsRead(Client client) {
        List<Notification> unreadNotifications = notificationRepository.findByBooking_ClientAndIsReadFalse(client);

        if (!unreadNotifications.isEmpty()) {
            unreadNotifications.forEach(n -> n.setRead(true));
            notificationRepository.saveAll(unreadNotifications);
        }
    }
}