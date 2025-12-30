package com.gesamtprojekt.application.service;

import com.gesamtprojekt.application.model.Client;

public interface ClientServiceInterface {

    Client createClient(String username,
                          String password,
                          String role);

}
