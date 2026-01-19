package com.gesamtprojekt.application.security;

import com.gesamtprojekt.application.model.Client;
import com.gesamtprojekt.application.repositories.ClientRepository;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.spring.security.AuthenticationContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@Component
public class SecurityService {

    private static final String LOGOUT_SUCCESS_URL = "/";
    private final AuthenticationContext authenticationContext;
    private final ClientRepository clientRepository;

    private final PasswordEncoder passwordEncoder;

    public SecurityService(AuthenticationContext authenticationContext,
                           ClientRepository clientRepository,
                           PasswordEncoder passwordEncoder) {
        this.authenticationContext = authenticationContext;
        this.clientRepository = clientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Client Klasse zurückgeben
    public Optional<Client> getAuthenticatedClient() {
        return authenticationContext.getAuthenticatedUser(UserDetails.class)
                .flatMap(userDetails -> clientRepository.findByUsernameAndIsActiveTrue(userDetails.getUsername()));
    }

    // Logout
    public void logout() {
        UI.getCurrent().getPage().setLocation(LOGOUT_SUCCESS_URL);
        SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
        logoutHandler.logout(
                VaadinServletRequest.getCurrent().getHttpServletRequest(), null,
                null);
    }

    // Hilfsmethode für Sidebar
    public boolean isAdmin() {
        return getAuthenticatedClient()
                .map(client -> "ADMIN".equalsIgnoreCase(client.getRole()))
                .orElse(false);
    }

    public boolean isClient() {
        return getAuthenticatedClient()
                .map(client -> "USER".equalsIgnoreCase(client.getRole()))
                .orElse(false);
    }

    public boolean isRoomUser() {
        return getAuthenticatedClient()
                .map(client -> "ROOM".equalsIgnoreCase(client.getRole()))
                .orElse(false);
    }

    // used for password check in logout of Room Screen
    public boolean checkPassword(String rawPassword) {
        return getAuthenticatedClient()
                .map(client -> passwordEncoder.matches(rawPassword, client.getPassword()))
                .orElse(false);
    }

}