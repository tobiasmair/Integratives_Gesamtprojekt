package com.gesamtprojekt.application.events;

import com.vaadin.flow.shared.Registration;

import java.util.LinkedList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class RoomChangedBroadcaster {
    static Executor executor = Executors.newSingleThreadExecutor();

    static LinkedList<Consumer<RoomChangedEvent>> listeners = new LinkedList<>();

    public static synchronized Registration register(Consumer<RoomChangedEvent> listener) {
        listeners.add(listener);
        return () -> {
            synchronized (RoomChangedBroadcaster.class) {
                listeners.remove(listener);
            }
        };
    }

    public static synchronized void broadcast(RoomChangedEvent event) {
        for (Consumer<RoomChangedEvent> listener : listeners) {
            executor.execute(() -> listener.accept(event));
        }
    }

    public static class RoomChangedEvent {
        private final String message;

        public RoomChangedEvent(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}
