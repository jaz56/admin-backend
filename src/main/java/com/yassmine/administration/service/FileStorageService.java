package com.yassmine.administration.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileStorageService {

    @Value("${file.upload-dir:/app/uploads}")
    private String uploadDir;

    // Garder le nom "store" — c'est ce que UserService appelle
    public String store(MultipartFile file, String userId, String type) {
        try {
            Path uploadPath = Paths.get(uploadDir, "users", userId, type);
            Files.createDirectories(uploadPath);

            String originalName = file.getOriginalFilename();
            String extension = "";
            if (originalName != null && originalName.contains(".")) {
                extension = originalName.substring(originalName.lastIndexOf("."));
            }

            String filename = type + "_" + System.currentTimeMillis() + extension;
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return "/uploads/users/" + userId + "/" + type + "/" + filename;

        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de l'upload : " + e.getMessage(), e);
        }
    }
    public String storeForBooking(MultipartFile file, String bookingId, String type) {
        try {
            Path uploadPath = Paths.get(uploadDir, "bookings", bookingId, type);
            Files.createDirectories(uploadPath);

            String originalName = file.getOriginalFilename();
            String extension = "";
            if (originalName != null && originalName.contains(".")) {
                extension = originalName.substring(originalName.lastIndexOf("."));
            }

            String filename = type + "_" + System.currentTimeMillis() + extension;
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return "/uploads/bookings/" + bookingId + "/" + type + "/" + filename;

        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de l'upload : " + e.getMessage(), e);
        }
    }
}