package com.gesamtprojekt.application.repositories;

import com.gesamtprojekt.application.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsersRepository extends JpaRepository<Client, Long> {

    Optional<Client> findByUsername(String username);

}
