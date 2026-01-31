package com.gesamtprojekt.application.service.implementation;

import com.gesamtprojekt.application.model.Exit;
import com.gesamtprojekt.application.repositories.ExitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExitService {

    private final ExitRepository exitRepository;

    // Finde Ausgang nach ID
    public Exit findExitById(Long exitId) {
        return exitRepository.findById(exitId)
                .orElseThrow(() -> new RuntimeException("Exit not found with ID: " + exitId));
    }

    // Finde die Zeit zwischen zwei Ausgängen
    public Integer getDistanceBetweenExits(Long exitFromId, Long exitToId) {
        return exitRepository.findTimeBetweenExits(exitFromId, exitToId);
    }

    public List<Exit> findAllExits() {
        return exitRepository.findAllExits();
    }
}