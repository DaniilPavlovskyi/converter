package com.molekula.converter.controllers;

import com.molekula.converter.utilities.ImageConverterUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@RestController
public class ConvertController {
    private static final List<String> IMAGE_FORMATS = Arrays.asList(
            "jpeg", "jpg", "png", "gif", "bmp", "tiff", "tif","pbm", "pgm", "ppm", "svg+xml", "webp", "heic", "raw", "ico"
    );
    private static final List<String> DEFAULT_IMAGE_FORMATS = Arrays.asList(
            "jpeg", "jpg", "png", "gif", "bmp", "tiff", "tif"/*
            for future
            ,"pbm", "pgm", "ppm" "svg", "webp", "heic", "raw", "ico"
            */
    );

    @GetMapping("api/convert")
    public ResponseEntity<Object> convert(@RequestParam("file") MultipartFile file,
                                               @RequestParam("type") String type) throws IOException {
        if (isNotImage(file)) {
            return ResponseEntity.badRequest().body("Please upload an image file.");
        }
        if (DEFAULT_IMAGE_FORMATS.contains(type)) {
            byte[] imageData = ImageConverterUtils.convertToDefaultType(file, type);
            return ResponseEntity.ok().contentType(MediaType.parseMediaType("image/" + type)).body(imageData);
        } else if (type.equals("svg")) {
            saveFile(file, file.getOriginalFilename());
            ImageConverterUtils.convertToSVG("img/" + file.getOriginalFilename());

            File svgFile = new File("img/" + file.getOriginalFilename());
            byte[] svgBytes = Files.readAllBytes(svgFile.toPath());

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("image/svg+xml"))
                    .body(svgBytes);
        } else {
            return ResponseEntity.badRequest().body("Please select type to convert.");
        }
    }

    @GetMapping("api/resize")
    public ResponseEntity<Object> resizeImage(@RequestParam("file") MultipartFile file,
                                              @RequestParam("multiplier") double multiplier) {
        if (isNotImage(file)) {
            return ResponseEntity.badRequest().body("Please upload an image file.");
        }
        byte[] resizedImageData = ImageConverterUtils.resize(file, multiplier);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(file.getContentType())).body(resizedImageData);
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

    private void saveFile(MultipartFile multipartFile, String fileName) {
        try {
            byte[] fileBytes = multipartFile.getBytes();
            Path uploadPath = Paths.get("img");
            Path filePath = uploadPath.resolve(fileName);
            Files.write(filePath, fileBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
