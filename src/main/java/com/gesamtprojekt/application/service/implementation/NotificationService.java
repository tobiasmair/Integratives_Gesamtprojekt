package com.gesamtprojekt.application.service.implementation;

import com.gesamtprojekt.application.model.Booking;
import com.gesamtprojekt.application.model.Client;
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

    // Nur relevante Notifications (aktive, nicht beendete Buchungen)
    public List<Notification> findAllByUser(Client client) {
        if (client == null) return List.of();
        return notificationRepository.findRelevantByClientOrderByCreatedAtDesc(client, LocalDateTime.now());
    }

    public long countUnread(Client client) {
        if (client == null) return 0;
        return notificationRepository.countRelevantUnreadByClient(client, LocalDateTime.now());
    }

    @Transactional
    public void markAllAsRead(Client client) {
        if (client == null) return;

        List<Notification> unreadRelevant =
                notificationRepository.findRelevantUnreadByClient(client, LocalDateTime.now());

        if (!unreadRelevant.isEmpty()) {
            unreadRelevant.forEach(n -> n.setRead(true));
            notificationRepository.saveAll(unreadRelevant);
        }
    }
}
