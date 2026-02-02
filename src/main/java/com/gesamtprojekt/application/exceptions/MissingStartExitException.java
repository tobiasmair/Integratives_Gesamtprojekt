package com.gesamtprojekt.application.exceptions;

import com.gesamtprojekt.application.model.Exit;
import java.util.List;

public class MissingStartExitException extends RuntimeException {
    private final List<Exit> availableExits;

    public MissingStartExitException(List<Exit> availableExits) {
        super("Booking attempted less than 60 minutes before start; user must select a start exit.");
        this.availableExits = availableExits;
    }

    public List<Exit> getAvailableExits() {
        return availableExits;
    }
}