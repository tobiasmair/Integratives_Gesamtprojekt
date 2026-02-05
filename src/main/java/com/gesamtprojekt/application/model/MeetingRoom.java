package com.gesamtprojekt.application.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Set;

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

    // Raum-Name
    @Column(name = "name")
    private String name;

    // Sitz-/Personenkapazität
    @Column(name = "capacity")
    private Integer capacity;

    // Gebäude/Standort
    @Column(name = "location")
    private String location;

    // Stockwerk
    @Column(name = "floor")
    private Integer floor;

    // Status (ACTIVE / INACTIVE)
    @Column(name = "status")
    private String status;

    // Image storage
    @Column(name = "image_path")
    private String imagePath;

    @Column(name = "image_mime")
    private String imageMime;

    @Column(name = "image_original_name")
    private String imageOriginalName;

    // Steuerungs-Flags
    @Column(name = "hasDoorControl")
    private Boolean hasDoorControl = false;

    @Column(name = "hasBlindControl")
    private Boolean hasBlindControl = false;

    @Column(name = "hasLightControl")
    private Boolean hasLightControl = false;

    @Column(name = "hasVentilationControl")
    private Boolean hasVentilationControl = false;

    @Column(name = "hasBeamerControl")
    private Boolean hasBeamerControl = false;

    @Column(name = "hasWhiteboard")
    private Boolean hasWhiteboard = false;

    @Column(name = "hasVacuumRobot")
    private Boolean hasVacuumRobot = false;

    // wird für Soft-Delete/Filter verwendet
    @Column(name = "isActive")
    private Boolean isActive = true;

    // Relation zu Client (1:1)
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "clientId", referencedColumnName = "userId")
    private Client roomUser;

    // Relation zu Buchungen (1:n)
    @OneToMany(mappedBy = "meetingRoom")
    private List<Booking> bookings  = new ArrayList<>();

    // Relation zu Equipment (n:m)
    @ManyToMany
    @JoinTable(
            name = "meeting_room_equipment",
            joinColumns = @JoinColumn(name = "room_id", referencedColumnName = "roomId"),
            inverseJoinColumns = @JoinColumn(name = "equipment_id", referencedColumnName = "equipmentId")
    )
    private Set<Equipment> equipment = new LinkedHashSet<>();

    // fester naechster Exit (n:1)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "exit_id", referencedColumnName = "exit_id")
    private Exit nearestExit;

    // Zeit zum naechsten Ausgang in Sekunden
    @Column(name = "time_to_nearest_exit")
    private Integer timeToNearestExit;
}
