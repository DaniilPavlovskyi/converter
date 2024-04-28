package com.molekula.converter.controllers;

import com.molekula.converter.utilities.ImageConverterUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@RestController
public class ConvertController {

    private static final List<String> IMAGE_FORMATS = Arrays.asList(
            "jpeg", "jpg", "png", "gif"/*
            for future
            , "bmp", "tiff", "svg", "webp", "heic", "raw", "pbm", "pgm", "ppm", "ico"
            */
    );

    @GetMapping("api/convert")
    public ResponseEntity<Object> convert(@RequestParam("file") MultipartFile file,
                                               @RequestParam("type") String type) {
        if (isNotImage(file)) {
            return ResponseEntity.badRequest().body("Please upload an image file.");
        }

        if (!IMAGE_FORMATS.contains(type)) {
            return ResponseEntity.badRequest().body("Please select type to convert.");
        }

        byte[] imageData = ImageConverterUtils.convertToDefaultType(file, type);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType("image/" + type)).body(imageData);
    }

    @GetMapping("api/resize")
    public ResponseEntity<Object> resizeImage(@RequestParam("file") MultipartFile file,
                                              @RequestParam("multiplier") double multiplier) {
        if (isNotImage(file)) {
            return ResponseEntity.badRequest().body("Please upload an image file.");
        }
        byte[] resizedImageData = ImageConverterUtils.resize(file, multiplier);
        MediaType contentType = MediaType.parseMediaType(file.getContentType());
        return ResponseEntity.ok().contentType(contentType).body(resizedImageData);
    }
    @GetMapping("api/compress")
    public ResponseEntity<Object> compressImage(@RequestParam("file") MultipartFile file,
                                                @RequestParam("quality") double quality) {
        if (isNotImage(file)) {
            return ResponseEntity.badRequest().body("Please upload an image file.");
        }

        if(quality < 0 || quality > 1) {
            return ResponseEntity.badRequest().body("Quality should be in range [0, 1].");
        }

        try {
            byte[] originalImageData = file.getBytes();
            byte[] compressedImageData = ImageConverterUtils.compress(file, quality);

            double compressionRatio = 0;
            if (compressedImageData != null) {
                compressionRatio = (double) compressedImageData.length / originalImageData.length;
            }

            if (compressionRatio >= 1) {
                return ResponseEntity.ok().contentType(MediaType.parseMediaType(file.getContentType())).body(originalImageData);
            } else {
                return ResponseEntity.ok().contentType(MediaType.parseMediaType(file.getContentType())).body(compressedImageData);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to compress the image file.");
        }
    }

    private boolean isNotImage(MultipartFile file) {
        return file.isEmpty() ||
                file.getContentType() == null ||
                !IMAGE_FORMATS.contains(file.getContentType().split("/")[1]);
    }

    private String getImageType(MultipartFile file) {
        return file.getContentType().split("/")[1];
    }
}
