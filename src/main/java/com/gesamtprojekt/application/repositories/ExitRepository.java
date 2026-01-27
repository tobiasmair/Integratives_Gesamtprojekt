package com.gesamtprojekt.application.repositories;

import com.gesamtprojekt.application.model.Exit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExitRepository extends JpaRepository<Exit, Long> {
}