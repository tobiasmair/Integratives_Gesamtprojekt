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
        return new StoredImage(target.toString(), mime, originalName);
    }

    public InputStream open(String path) {
        try {
            return Files.newInputStream(Paths.get(path));
        } catch (IOException e) {
            throw new IllegalStateException("Cannot open image: " + path, e);
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

    public record StoredImage(String path, String mime, String originalName) {}
}
