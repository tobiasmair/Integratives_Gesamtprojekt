package com.gesamtprojekt.application.ui.components.calendar;

import java.util.List;

public class CalendarDummyRooms {

    public record Room(
            String name,
            String building,
            int capacity,
            String floor,
            List<String> tags,
            String imageUrl
    ) {}

    public static List<Room> rooms() {
        return List.of(
                new Room(
                        "Meeting Room A",
                        "MCI 1",
                        95,
                        "3",
                        List.of("Wifi", "Whiteboard", "Smart TV", "Video Conference", "+ 2 more"),
                        "https://picsum.photos/600/350"
                ),
                new Room(
                        "Meeting Room A",
                        "MCI 1",
                        95,
                        "3",
                        List.of("Wifi", "Whiteboard", "Smart TV", "Video Conference", "+ 2 more"),
                        "https://picsum.photos/601/350"
                ),
                new Room(
                        "Meeting Room A",
                        "MCI 1",
                        95,
                        "3",
                        List.of("Wifi", "Whiteboard", "Smart TV", "Video Conference", "+ 2 more"),
                        "https://picsum.photos/602/350"
                ),
        new Room(
                "Meeting Room A",
                "MCI 1",
                95,
                "3",
                List.of("Wifi", "Whiteboard", "Smart TV", "Video Conference", "+ 2 more"),
                "https://picsum.photos/602/350"
        ),
        new Room(
                "Meeting Room A",
                "MCI 1",
                95,
                "3",
                List.of("Wifi", "Whiteboard", "Smart TV", "Video Conference", "+ 2 more"),
                "https://picsum.photos/602/350"
        ),
                new Room(
                        "Meeting Room A",
                        "MCI 1",
                        95,
                        "3",
                        List.of("Wifi", "Whiteboard", "Smart TV", "Video Conference", "+ 2 more"),
                        "https://picsum.photos/602/350"
                )
        );
    }
}
