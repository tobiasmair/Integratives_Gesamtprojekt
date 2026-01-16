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

    @ManyToOne
    @JoinColumn(name = "userId")
    private Client client;

    @ManyToOne(optional = false)
    @JoinColumn(name = "roomId")
    private MeetingRoom meetingRoom;
}
