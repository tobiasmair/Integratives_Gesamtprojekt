package com.gesamtprojekt.application.repositories;

import com.gesamtprojekt.application.model.Exit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExitRepository extends JpaRepository<Exit, Long> {

    // Finde den nächstgelegenen Ausgang zu einem Meetingraum
    @Query("""
        SELECT e FROM Exit e
        WHERE e.isActive = true
        AND e.id = :exitId
    """)
    Exit findExitById(@Param("exitId") Long exitId);

    // Finde die Zeit zwischen zwei Ausgängen
    @Query("""
    SELECT ed.timeInSeconds FROM ExitDistance ed
    WHERE ed.exitFrom.exitId = :exitFromId AND ed.exitTo.exitId = :exitToId
    """)
    Integer findTimeBetweenExits(@Param("exitFromId") Long exitFromId, @Param("exitToId") Long exitToId);

    @Query("SELECT e FROM Exit e WHERE e.isActive = true")
    List<Exit> findAllExits();
}