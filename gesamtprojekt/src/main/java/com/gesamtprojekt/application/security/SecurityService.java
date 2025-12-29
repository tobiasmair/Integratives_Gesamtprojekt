package com.gesamtprojekt.application.security;

import com.gesamtprojekt.application.model.Client;
import com.gesamtprojekt.application.repositories.UsersRepository;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.spring.security.AuthenticationContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SecurityService {

    private static final String LOGOUT_SUCCESS_URL = "/";
    private final AuthenticationContext authenticationContext;
    private final UsersRepository usersRepository;

    public SecurityService(AuthenticationContext authenticationContext, UsersRepository usersRepository) {
        this.authenticationContext = authenticationContext;
        this.usersRepository = usersRepository;
    }

    /*
    public UserDetails getAuthenticatedUser() {
        SecurityContext context = SecurityContextHolder.getContext();
        Object principal = context.getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return (UserDetails) context.getAuthentication().getPrincipal();
        }
        // Anonymous or no authentication.
        return null;
    }
     */

    public Optional<UserDetails> getAuthenticatedUser() {
        SecurityContext context = SecurityContextHolder.getContext();
        Object principal = context.getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return Optional.of((UserDetails) principal);
        }
        return Optional.empty();
    }

    public Optional<Client> getAuthenticatedClient() {
        return authenticationContext.getAuthenticatedUser(UserDetails.class)
                .flatMap(userDetails -> usersRepository.findByUsername(userDetails.getUsername()));
    }

    public void logout() {
        UI.getCurrent().getPage().setLocation(LOGOUT_SUCCESS_URL);
        SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
        logoutHandler.logout(
                VaadinServletRequest.getCurrent().getHttpServletRequest(), null,
                null);
    }
}