package com.gesamtprojekt.application.repositories;

import com.gesamtprojekt.application.model.ExitDistance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ExitDistanceRepository extends JpaRepository<ExitDistance, Long> {

    /*
    @Query("""
        SELECT ed.timeInSeconds FROM ExitDistance ed
        WHERE ed.exitFrom.id = :exitFromId AND ed.exitTo.id = :exitToId
    """)
    Integer findTimeBetweenExits(@Param("exitFromId") Long exitFromId, @Param("exitToId") Long exitToId);
     */

    // in beide Richtungen
    @Query("""
        SELECT ed.timeInSeconds FROM ExitDistance ed
        WHERE (ed.exitFrom.exitId = :fromId AND ed.exitTo.exitId = :toId)
           OR (ed.exitFrom.exitId = :toId AND ed.exitTo.exitId = :fromId)
    """)
    Integer findTimeBetweenExits(@Param("fromId") Long fromId, @Param("toId") Long toId);
}