package com.foodredistribution.foodredistribution.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Stores uploaded images in the local filesystem under uploads/{subDir}/.
 * The URL returned is a relative path that can be served via Spring's
 * static resource handler at /uploads/**.
 *
 * Replace this bean with a cloud-backed implementation (S3, GCS, Cloudinary)
 * without any changes to the callers.
 */
@Service
public class LocalStorageService implements StorageService {

    private static final String BASE_UPLOAD_DIR = "uploads";

    @Override
    public String store(MultipartFile file, String subDir) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File must not be empty");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";

        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
        }

        String uniqueFilename = UUID.randomUUID().toString() + extension;

        Path uploadDir = Paths.get(BASE_UPLOAD_DIR, subDir);

        try {
            Files.createDirectories(uploadDir);
            Path targetPath = uploadDir.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file: " + e.getMessage(), e);
        }

        // Return a URL path that can be served by Spring MVC static resources
        return "/" + BASE_UPLOAD_DIR + "/" + subDir + "/" + uniqueFilename;
    }
}
