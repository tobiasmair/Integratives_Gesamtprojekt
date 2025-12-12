package com.gesamtprojekt.application.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "booking")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long booingId;

    @Column(name = "startTime")
    private LocalDateTime startTime;

    @Column(name = "endTime")
    private LocalDateTime endTime;

    @Column(name = "checkInTime")
    private LocalDateTime checkInTime;

    @Column(name = "bookingStatus")
    private String bookingStatus;

    @ManyToOne(optional = false)
    @JoinColumn(name = "userId")
    private Client client;

    @ManyToOne(optional = false)
    @JoinColumn(name = "roomId")
    private MeetingRoom meetingRoom;
}
