package com.gesamtprojekt.application.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "meetingRoom")
public class MeetingRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roomId;

    @Column(name = "name")
    private String name;

    @Column(name = "capacity")
    private Integer capacity;

    @Column(name = "location")
    private String location;

    @Column(name = "status")
    private String status;

    @Column(name = "hasDoorControl")
    private Boolean hasDoorControl;

    @Column(name = "hasLightControl")
    private Boolean hasLightControl;

    @Column(name = "hasVentilationControl")
    private Boolean hasVentilationControl;

    @Column(name = "isActive")
    private Boolean isActive = true;

    // Relation to Booking
    @OneToMany(mappedBy = "meetingRoom")
    private List<Booking> bookings  = new ArrayList<>();

    // Relation to Equipment
    @ManyToMany
    @JoinTable(
            name = "equipment",
            joinColumns = @JoinColumn(name = "roomId"),
            inverseJoinColumns = @JoinColumn(name = "equipmentId")
    )
    private List<Equipment> equipment = new ArrayList<>();
}
