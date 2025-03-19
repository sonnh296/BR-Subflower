package com.hls.sunflower.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.hls.sunflower.service.CloudinaryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/cloudinary/upload")
@RequiredArgsConstructor
public class CloudinaryImageUploadController {

    private final CloudinaryService cloudinaryService;

    @PostMapping("/image")
    public ResponseEntity<Map> uploadImage(@RequestParam("image") MultipartFile file) {
        Map data = this.cloudinaryService.uploadImage(file);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @PostMapping("/file")
    public ResponseEntity<Map> uploadFile(@RequestParam("file") MultipartFile file) {
        Map data = this.cloudinaryService.uploadFile(file);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }
}
