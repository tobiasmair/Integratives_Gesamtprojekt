package com.gesamtprojekt.application.repositories;

import com.gesamtprojekt.application.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UsersRepository extends JpaRepository<Client, Long> {

    //List<Client> findUsersBy(Long userId);

}
