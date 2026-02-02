package com.gesamtprojekt.application.service.implementation;

import com.gesamtprojekt.application.model.Exit;
import com.gesamtprojekt.application.repositories.ExitRepository;
import com.gesamtprojekt.application.repositories.ExitDistanceRepository;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExitService {

    private final ExitRepository exitRepository;
    private final ExitDistanceRepository exitDistanceRepository;

    // Finde Ausgang nach ID
    public Exit findExitById(Long exitId) {
        return exitRepository.findByIdAndIsActiveTrue(exitId)
                .orElseThrow(() -> new EntityNotFoundException("Exit not found with ID: " + exitId));
    }

    // Finde die Zeit zwischen zwei Ausgängen
    public Integer getDistanceBetweenExits(Long exitFromId, Long exitToId) {
        return exitDistanceRepository.findTimeBetweenExits(exitFromId, exitToId);
    }

    public List<Exit> getAllExits() {
        return exitRepository.findAllByIsActiveTrue();
    }

    @PostConstruct
    public void debugDb() {
        System.out.println("Exit count = " + exitRepository.count());
    }
}


