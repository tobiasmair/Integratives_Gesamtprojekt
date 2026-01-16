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

    /**
     * Speichert das Bild am Server im Ordner uploads/rooms
     * und gibt als "path" nur den Dateinamen zurück (z.B. 8f3a... .jpg).
     */
    public StoredImage save(InputStream in, String originalName, String mime) {
        String fileName = UUID.randomUUID() + ext(originalName);
        Path target = baseDir.resolve(fileName);
        copy(in, target);
        return new StoredImage(fileName, normalizeMime(mime, fileName), originalName);
    }

    /**
     * Öffnet ein gespeichertes Bild anhand des Dateinamens (nicht voller Pfad!).
     */
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
