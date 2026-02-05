package com.gesamtprojekt.application.repositories;

import com.gesamtprojekt.application.model.Exit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExitRepository extends JpaRepository<Exit, Long> {

    // Finde den nächstgelegenen Ausgang zu einem Meetingraum
    Optional<Exit> findByIdAndIsActiveTrue(Long id);

    List<Exit> findAllByIsActiveTrue();
}
