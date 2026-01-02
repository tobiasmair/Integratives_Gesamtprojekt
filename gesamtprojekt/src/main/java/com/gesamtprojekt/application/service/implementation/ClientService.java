package com.gesamtprojekt.application.service.implementation;

import com.gesamtprojekt.application.model.Client;
import com.gesamtprojekt.application.repositories.ClientRepository;
import com.gesamtprojekt.application.service.ClientServiceInterface;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ClientService implements ClientServiceInterface {

    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;

    // Neuen Client anlegen
    public Client createClient(String username, String password, String email, String department, String userType, String role) {
        if (clientRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username already in use.");
        }

        String hashedPassword = passwordEncoder.encode(password);

        Client newUser = new Client(username, hashedPassword, role, email, department, userType);
        clientRepository.save(newUser);

        return newUser;
    }

    // Nach Namen oder Rolle filtern
    public List<Client> findAllUsers(String stringFilter, String roleFilter) {
        String role = (roleFilter != null && !roleFilter.equals("All Roles")) ? roleFilter : "";

        if (stringFilter == null || stringFilter.isEmpty() && role.isEmpty()) {
            return clientRepository.findByIsActiveTrue();
        } else {
            //return clientRepository.search(stringFilter);
            return clientRepository.searchByFilters(stringFilter, role);
        }
    }

    // Anzahl Client zurückgeben
    public long countUsers() {
        return clientRepository.countByisActiveTrue();
    }

    public long countByUserTypeAndIsActiveTrue(String userType) {
        return clientRepository.countByUserTypeAndIsActiveTrue(userType);
    }

    // CLient löschen (isActive Flag setzen)
    public void deleteClient(Client client) {
        //clientRepository.delete(client);
        client.setIsActive(false);
        clientRepository.save(client);
    }

    // Client updaten
    public void updateClient(Client client) {
        clientRepository.save(client);
    }

    // Client updaten mit Passwort
    public void updateClientWithPassword(Client client, String newPassword) {
        String hashedPassword = passwordEncoder.encode(newPassword);
        client.setPassword(hashedPassword);
        clientRepository.save(client);
    }

}
