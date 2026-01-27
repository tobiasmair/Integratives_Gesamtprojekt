package com.gesamtprojekt.application.service.implementation;

import com.gesamtprojekt.application.model.Exit;
import com.gesamtprojekt.application.repositories.ExitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExitService {

    private final ExitRepository exitRepository;

    public Exit findExitById(Long exitId) {
        return exitRepository.findById(exitId)
                .orElseThrow(() -> new RuntimeException("Start exit not found"));
    }
}