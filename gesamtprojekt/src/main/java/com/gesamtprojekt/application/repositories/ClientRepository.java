package com.gesamtprojekt.application.repositories;

import com.gesamtprojekt.application.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {

    Optional<Client> findByUsername(String username);

    @Query("select c from Client c " +
            "where lower(c.username) like lower(concat('%', :searchTerm, '%'))")
    List<Client> search(@Param("searchTerm") String searchTerm);

    @Query("select c from Client c " +
            "where (lower(c.username) like lower(concat('%', :searchTerm, '%'))) " +
            "and (:role = '' or c.role = :role)")
    List<Client> searchByFilters(@Param("searchTerm") String searchTerm,
                                 @Param("role") String role);

}
