package com.gesamtprojekt.application.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "exitDistance")
public class ExitDistance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "exit_from_id", nullable = false)
    private Exit exitFrom;

    @ManyToOne
    @JoinColumn(name = "exit_to_id", nullable = false)
    private Exit exitTo;

    @Column(name = "time_in_seconds", nullable = false)
    private Integer timeInSeconds; // Vorberechnete Zeit in Sekunden
}
