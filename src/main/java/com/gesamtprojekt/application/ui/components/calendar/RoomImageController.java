package com.gesamtprojekt.application.ui.components.calendar;

import com.gesamtprojekt.application.service.implementation.RoomImageStorageService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/room-images")
public class RoomImageController {

    private final RoomImageStorageService storage;

    public RoomImageController(RoomImageStorageService storage) {
        this.storage = storage;
    }

    @GetMapping("/{fileName}")
    public ResponseEntity<InputStreamResource> get(@PathVariable String fileName) {
        var img = storage.openByFileName(fileName);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(img.mime()))
                .body(new InputStreamResource(img.stream()));
    }

    @GetMapping("/by-room")
    public ResponseEntity<InputStreamResource> getByRoomName(@RequestParam String roomName) {
        var img = storage.getRoomImage(roomName);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(img.mime()))
                .body(new InputStreamResource(img.stream()));
    }
}
