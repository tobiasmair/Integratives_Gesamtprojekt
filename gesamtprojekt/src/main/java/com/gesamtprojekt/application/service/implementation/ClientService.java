package com.gesamtprojekt.application.service.implementation;

import com.gesamtprojekt.application.model.Client;
import com.gesamtprojekt.application.repositories.ClientRepository;
import com.gesamtprojekt.application.service.ClientServiceInterface;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ClientService implements ClientServiceInterface {

    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;

    public Client createClient(String username, String password, String role) {
        if (clientRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Benutzername bereits vergeben.");
        }

        String hashedPassword = passwordEncoder.encode(password);

        Client newUser = new Client(username, hashedPassword, role);
        clientRepository.save(newUser);

        return newUser;
    }
}
