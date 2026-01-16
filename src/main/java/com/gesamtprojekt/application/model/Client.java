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
@Table(name = "client")
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "role")  // ADMIN, USER, ROOM
    private String role;

    @Column(name = "department")    // DIBSE, MCI 1, MCI 2, MCI 3
    private String department;

    @Column(name = "userType")  // LECTURER, STUDENT, EXTERNAL, ROOM_SCREEN
    private String userType;

    @Column(name = "isActive")
    private Boolean isActive = true;

    // Relation to Booking
    @OneToMany(mappedBy = "client")
    private List<Booking> bookings  = new ArrayList<>();

    public Client(String username, String password, String role, String email, String department, String userType) {
        setUsername(username);
        setPassword(password);
        setRole(role);
        setEmail(email);
        setDepartment(department);
        setUserType(userType);
        setIsActive(true);
    }
}
