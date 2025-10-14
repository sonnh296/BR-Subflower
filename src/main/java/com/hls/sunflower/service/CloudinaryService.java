package com.hls.sunflower.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public Map uploadImage(MultipartFile file) {
        try {
            Map data = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());

            String url = (String) data.get("url");

            return Map.of("url", url);
        } catch (IOException io) {
            throw new RuntimeException("Image upload fail");
        }
    }

    public Map uploadFile(MultipartFile file) {
        try {
            Map data = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap("resource_type", "raw"));

            String url = (String) data.get("url");

            return Map.of("url", url);
        } catch (IOException io) {
            throw new RuntimeException("Image upload fail");
        }
    }

    public List<String> uploadMultipleImages(List<MultipartFile> files) {
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            try {
                Map data = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap("folder", "products"));
                String url = (String) data.get("url");
                urls.add(url);
            } catch (IOException io) {
                throw new RuntimeException("Image upload failed for file: " + file.getOriginalFilename());
            }
        }
        return urls;
    }

    public String uploadProductImage(MultipartFile file, String productId) {
        try {
            Map data = cloudinary
                    .uploader()
                    .upload(
                            file.getBytes(),
                            ObjectUtils.asMap(
                                    "folder",
                                    "products/" + productId,
                                    "transformation",
                                    ObjectUtils.asMap("quality", "auto", "fetch_format", "auto")));
            return (String) data.get("url");
        } catch (IOException io) {
            throw new RuntimeException("Product image upload failed");
        }
    }
}
