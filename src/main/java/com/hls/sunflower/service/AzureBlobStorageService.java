package com.hls.sunflower.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobHttpHeaders;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AzureBlobStorageService {

    private final BlobContainerClient blobContainerClient;

    /**
     * Upload a single file to Azure Blob Storage
     */
    public String uploadFile(MultipartFile file, String folderPath) {
        try {
            String originalFilename = file.getOriginalFilename();
            String fileName = folderPath + "/" + UUID.randomUUID() + "_" + originalFilename;

            BlobClient blobClient = blobContainerClient.getBlobClient(fileName);

            // Set content type
            BlobHttpHeaders headers = new BlobHttpHeaders().setContentType(file.getContentType());

            blobClient.upload(file.getInputStream(), file.getSize(), true);
            blobClient.setHttpHeaders(headers);

            String blobUrl = blobClient.getBlobUrl();
            log.info("File uploaded successfully to Azure Blob Storage: {}", blobUrl);

            return blobUrl;
        } catch (IOException e) {
            log.error("Failed to upload file to Azure Blob Storage", e);
            throw new RuntimeException("Failed to upload file: " + e.getMessage());
        }
    }

    /**
     * Upload multiple files to Azure Blob Storage
     */
    public List<String> uploadMultipleFiles(List<MultipartFile> files, String folderPath) {
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            String url = uploadFile(file, folderPath);
            urls.add(url);
        }
        return urls;
    }

    /**
     * Upload a product image to Azure Blob Storage
     */
    public String uploadProductImage(MultipartFile file, String productId) {
        return uploadFile(file, "products/" + productId);
    }

    /**
     * Upload multiple product images to Azure Blob Storage
     */
    public List<String> uploadProductImages(List<MultipartFile> files, String productId) {
        return uploadMultipleFiles(files, "products/" + productId);
    }

    /**
     * Delete a file from Azure Blob Storage
     */
    public void deleteFile(String blobUrl) {
        try {
            // Extract blob name from URL
            String blobName = extractBlobNameFromUrl(blobUrl);
            BlobClient blobClient = blobContainerClient.getBlobClient(blobName);

            if (blobClient.exists()) {
                blobClient.delete();
                log.info("File deleted successfully from Azure Blob Storage: {}", blobUrl);
            }
        } catch (Exception e) {
            log.error("Failed to delete file from Azure Blob Storage", e);
            throw new RuntimeException("Failed to delete file: " + e.getMessage());
        }
    }

    /**
     * Extract blob name from the full blob URL
     */
    private String extractBlobNameFromUrl(String blobUrl) {
        // URL format: https://<account>.blob.core.windows.net/<container>/<blob-name>
        String containerName = blobContainerClient.getBlobContainerName();
        int containerIndex = blobUrl.indexOf(containerName);
        if (containerIndex != -1) {
            return blobUrl.substring(containerIndex + containerName.length() + 1);
        }
        throw new IllegalArgumentException("Invalid blob URL format");
    }

    /**
     * Check if a blob exists
     */
    public boolean blobExists(String blobUrl) {
        try {
            String blobName = extractBlobNameFromUrl(blobUrl);
            BlobClient blobClient = blobContainerClient.getBlobClient(blobName);
            return blobClient.exists();
        } catch (Exception e) {
            log.error("Error checking if blob exists", e);
            return false;
        }
    }
}
