package com.gesamtprojekt.application.model;

import com.gesamtprojekt.application.service.dto.NotificationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "notification")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @Column(name = "createdAt")
    private LocalDateTime createdAt;

    @Column(name = "isRead")
    private boolean isRead = false;

    // Über Booking zugriff zu Room
    @ManyToOne
    @JoinColumn(name = "bookingId")
    private Booking booking;

    // Get Methoden für UI
    public Client getClient() {
        return booking != null ? booking.getClient() : null;
    }

    public String getRoomName() {
        return booking != null && booking.getMeetingRoom() != null
                ? booking.getMeetingRoom().getName() : "Unknown Room";
    }

    public String getBookingCode() {
        return booking != null ? booking.getBookingCode() : "0000-0000";
    }
}
