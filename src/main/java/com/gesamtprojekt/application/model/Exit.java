package com.gesamtprojekt.application.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "exit")
public class Exit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long exitId;

    @Column(name = "name")
    private String name;

    @ElementCollection
    @CollectionTable(name = "exit_building_times", joinColumns = @JoinColumn(name = "exit_id"))
    @MapKeyColumn(name = "building_name")
    @Column(name = "time_to_building")
    private Map<String, Integer> timeToBuildings = new HashMap<>();
}