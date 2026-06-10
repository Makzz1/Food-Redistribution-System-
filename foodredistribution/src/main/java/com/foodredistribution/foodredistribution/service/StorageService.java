package com.foodredistribution.foodredistribution.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * Abstraction for image storage.
 * Currently backed by LocalStorageService.
 * Can be swapped for S3, GCS, Cloudinary etc. without touching callers.
 */
public interface StorageService {

    /**
     * Stores a file and returns the publicly accessible URL.
     *
     * @param file   the multipart file to store
     * @param subDir subdirectory bucket (e.g. "profiles", "food-posts")
     * @return publicly accessible URL string
     */
    String store(MultipartFile file, String subDir);
}
