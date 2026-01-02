package com.gesamtprojekt.application.repositories;

import com.gesamtprojekt.application.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {

    List<Client> findByIsActiveTrue();

    Optional<Client> findByUsername(String username);

    Optional<Client> findByUsernameAndIsActiveTrue(String username);

    @Query("select c from Client c " +
            "where c.isActive = true " +
            "and (lower(c.username) like lower(concat('%', :searchTerm, '%')) " +
            "or lower(c.email) like lower(concat('%', :searchTerm, '%'))) " +
            "and (:role = '' or c.role = :role)")
    List<Client> searchByFilters(@Param("searchTerm") String searchTerm, @Param("role") String role);

    // Anzahl aktive Clients zählen
    long countByisActiveTrue();

    // Anzahl aktive Clients nach Benutzertyp zählen
    long countByUserTypeAndIsActiveTrue(String userType);

}
