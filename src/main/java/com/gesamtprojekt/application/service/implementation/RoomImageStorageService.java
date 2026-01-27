package com.gesamtprojekt.application.service.implementation;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.UUID;

@Service
public class RoomImageStorageService {

    private final Path baseDir = Paths.get("uploads", "rooms");

    public RoomImageStorageService() {
        createDir();
    }


    public StoredImage save(InputStream in, String originalName, String mime) {
        String fileName = UUID.randomUUID() + ext(originalName);
        Path target = baseDir.resolve(fileName);
        copy(in, target);
        return new StoredImage(fileName, normalizeMime(mime, fileName), originalName);
    }

    /**
     * Speichert das Bild mit dem Raumnamen als Dateiname.
     * Überschreibt existierende Bilder mit demselben Raumnamen.
     * Normalisiert problematische Zeichen im Raumnamen (/, \, :, *, ?, ", <, >, |).
     */
    public StoredImage saveWithRoomName(InputStream in, String roomName, String originalName, String mime) {
        // Normalisiere den Raumnamen für Dateisystem
        String safeName = roomName.replaceAll("[/\\\\:*?\"<>|]", "_");

        // Verwende Extension aus Original-Dateiname
        String extension = ext(originalName);
        if (extension.isEmpty()) {
            // Fallback: Extension aus MIME-Type ableiten
            if (mime != null) {
                if (mime.contains("png")) extension = ".png";
                else if (mime.contains("webp")) extension = ".webp";
                else extension = ".jpg";
            } else {
                extension = ".jpg";
            }
        }

        String fileName = safeName + extension;
        Path target = baseDir.resolve(fileName);

        // Lösche existierendes Bild mit gleichem Raumnamen (alle Extensions)
        deleteExistingRoomImages(safeName);

        copy(in, target);
        return new StoredImage(fileName, normalizeMime(mime, fileName), originalName);
    }

    /**
     * Löscht alle existierenden Bilder für einen Raum (alle Extensions).
     */
    private void deleteExistingRoomImages(String safeName) {
        for (String ext : new String[]{".jpeg", ".jpg", ".png", ".webp"}) {
            Path existingFile = baseDir.resolve(safeName + ext);
            try {
                Files.deleteIfExists(existingFile);
            } catch (IOException e) {
                // Ignoriere Fehler beim Löschen
            }
        }
    }

    public OpenedImage openByFileName(String fileName) {
        try {
            Path p = baseDir.resolve(fileName);
            InputStream in = Files.newInputStream(p, StandardOpenOption.READ);
            String mime = guessMime(fileName);
            return new OpenedImage(in, mime);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot open image: " + fileName, e);
        }
    }

    public OpenedImage getRoomImage(String roomName) {
        if (roomName != null && !roomName.isBlank()) {

            String safeName = roomName.replaceAll("[/\\\\:*?\"<>|]", "_");


            for (String ext : new String[]{".jpeg", ".jpg", ".png", ".webp"}) {
                Path roomImagePath = baseDir.resolve(safeName + ext);
                if (Files.exists(roomImagePath)) {
                    try {
                        InputStream in = Files.newInputStream(roomImagePath, StandardOpenOption.READ);
                        String mime = guessMime(safeName + ext);
                        return new OpenedImage(in, mime);
                    } catch (Exception e) {

                    }
                }
            }
        }


        return openByFileName("dummypicture1.jpeg");
    }

    private void createDir() {
        try {
            Files.createDirectories(baseDir);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create upload dir: " + baseDir, e);
        }
    }

    private void copy(InputStream in, Path target) {
        try {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot save image: " + target, e);
        }
    }

    private String ext(String name) {
        if (name == null) return "";
        int i = name.lastIndexOf('.');
        return i >= 0 ? name.substring(i) : "";
    }

    private String guessMime(String fileName) {
        String lower = fileName == null ? "" : fileName.toLowerCase();
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        return "application/octet-stream";
    }

    private String normalizeMime(String mime, String fileName) {
        if (mime != null && !mime.isBlank()) return mime;
        return guessMime(fileName);
    }

    public record OpenedImage(InputStream stream, String mime) {}
    public record StoredImage(String path, String mime, String originalName) {}
}
