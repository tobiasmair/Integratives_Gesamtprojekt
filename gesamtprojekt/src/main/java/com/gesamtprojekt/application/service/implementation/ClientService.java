package com.gesamtprojekt.application.service.implementation;

import com.gesamtprojekt.application.model.Client;
import com.gesamtprojekt.application.repositories.UsersRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ClientService {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;

    public Client createCustomer(String username, String password, String role) {
        if (usersRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Benutzername bereits vergeben.");
        }

        String hashedPassword = passwordEncoder.encode(password);

        Client newUser = new Client(username, hashedPassword, role);
        usersRepository.save(newUser);

        return newUser;
    }
}
