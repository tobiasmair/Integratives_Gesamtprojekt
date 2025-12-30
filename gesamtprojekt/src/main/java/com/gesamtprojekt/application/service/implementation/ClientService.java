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
    public Client createClient(String username, String password, String role) {
        if (clientRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Benutzername bereits vergeben.");
        }

        String hashedPassword = passwordEncoder.encode(password);

        Client newUser = new Client(username, hashedPassword, role);
        clientRepository.save(newUser);

        return newUser;
    }

    // Nach Namen oder Rolle filtern
    public List<Client> findAllUsers(String stringFilter, String roleFilter) {
        String role = (roleFilter != null && !roleFilter.equals("All Roles")) ? roleFilter : "";

        if (stringFilter == null || stringFilter.isEmpty() && role.isEmpty()) {
            return clientRepository.findAll();
        } else {
            //return clientRepository.search(stringFilter);
            return clientRepository.searchByFilters(stringFilter, role);
        }
    }

    // Anzahl Client zurückgeben
    public long countUsers() {
        return clientRepository.count();
    }

    // CLient löschen
    public void deleteClient(Client client) {
        clientRepository.delete(client);
    }

    // Client updaten
    public void updateClient(Client client) {
        clientRepository.save(client);
    }

}
