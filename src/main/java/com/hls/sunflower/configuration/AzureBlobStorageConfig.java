package com.hls.sunflower.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class AzureBlobStorageConfig {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AzureBlobStorageConfig.class);

    @Value("${azure.storage.connection-string}")
    private String connectionString;

    @Value("${azure.storage.container-name}")
    private String containerName;

    @Bean
    @ConditionalOnProperty(name = "azure.storage.connection-string")
    public BlobServiceClient blobServiceClient() {
        try {
            // Check if connection string is valid (not a placeholder)
            if (connectionString.startsWith("DefaultEndpointsProtocol")
                    && connectionString.contains("AccountName=your_account")) {
                log.warn(
                        "Azure storage connection string is using default placeholder values. Azure storage will not be available.");
                return null;
            }
            return new BlobServiceClientBuilder()
                    .connectionString(connectionString)
                    .buildClient();
        } catch (Exception e) {
            log.error("Failed to create Azure Blob Service Client: {}", e.getMessage());
            return null;
        }
    }

    @Bean
    @ConditionalOnProperty(name = "azure.storage.connection-string")
    public BlobContainerClient blobContainerClient(BlobServiceClient blobServiceClient) {
        if (blobServiceClient == null) {
            log.warn("BlobServiceClient is not available. BlobContainerClient will not be created.");
            return null;
        }
        try {
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
            // Create container if it doesn't exist
            if (!containerClient.exists()) {
                containerClient.create();
            }
            return containerClient;
        } catch (Exception e) {
            log.error("Failed to create Azure Blob Container Client: {}", e.getMessage());
            return null;
        }
    }
}
