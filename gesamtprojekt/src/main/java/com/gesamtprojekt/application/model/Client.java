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

    @Column(name = "role")
    private String role;

    // Relation to Booking
    @OneToMany(mappedBy = "client")
    private List<Booking> bookings  = new ArrayList<>();

    public Client(String username, String password, String role) {
        setUsername(username);
        setPassword(password);
        setRole(role);
    }
}
