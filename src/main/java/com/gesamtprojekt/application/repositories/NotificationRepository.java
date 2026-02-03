package com.gesamtprojekt.application.repositories;

import com.gesamtprojekt.application.model.Client;
import com.gesamtprojekt.application.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;


public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Alle Benachrichtigungen eines Users
    List<Notification> findByBooking_ClientOrderByCreatedAtDesc(Client client);

    // Anzahl ungelesenen Benachrichtigungen
    long countByBooking_ClientAndIsReadFalse(Client client);

    // ungelesene Benachrichtigungen für User
    List<Notification> findByBooking_ClientAndIsReadFalse(Client client);


    @Query("""
        select n
        from Notification n
        where n.booking.client = :client
          and n.booking.isActive = true
          and n.booking.endTime >= :now
        order by n.createdAt desc
    """)
    List<Notification> findRelevantByClientOrderByCreatedAtDesc(
            @Param("client") Client client,
            @Param("now") LocalDateTime now
    );

    @Query("""
        select count(n)
        from Notification n
        where n.booking.client = :client
          and n.isRead = false
          and n.booking.isActive = true
          and n.booking.endTime >= :now
    """)
    long countRelevantUnreadByClient(
            @Param("client") Client client,
            @Param("now") LocalDateTime now
    );

    @Query("""
        select n
        from Notification n
        where n.booking.client = :client
          and n.isRead = false
          and n.booking.isActive = true
          and n.booking.endTime >= :now
        order by n.createdAt desc
    """)
    List<Notification> findRelevantUnreadByClient(
            @Param("client") Client client,
            @Param("now") LocalDateTime now
    );
}
