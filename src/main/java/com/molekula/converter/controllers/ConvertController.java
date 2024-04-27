package com.molekula.converter.controllers;

import com.molekula.converter.utilities.ImageConverterUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@RestController
public class ConvertController {

    private static final List<String> IMAGE_FORMATS = Arrays.asList(
            "jpeg", "png", "gif", "bmp", "tiff", "psd", "svg", "webp", "heic", "raw", "ico", "pbm", "pgm", "ppm"
    );

    @GetMapping("api/convert-png-to-jpg")
    public ResponseEntity<Object> convertPNGtoJPG(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty() || IMAGE_FORMATS.contains(ImageConverterUtils.getFileFormat(file.getOriginalFilename()))) {
            return ResponseEntity.badRequest().body("Please upload an image file.");
        }
        try {
            byte[] jpgImageData = ImageConverterUtils.convertPNGtoJPG(file.getBytes());
            return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(jpgImageData);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to convert the image file.");
        }
    }

    @GetMapping("api/resize")
    public ResponseEntity<Object> resize(@RequestParam("file") MultipartFile file,
                                         @RequestParam("multiplier") double multiplier) {
        if (file.isEmpty() || ImageConverterUtils.getFileFormat(file.getOriginalFilename()) == null) {
            return ResponseEntity.badRequest().body("Please upload an image file.");
        }
        try {
            byte[] jpgImageData = ImageConverterUtils.resize(file.getBytes(), multiplier, file.getOriginalFilename());
            return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(jpgImageData);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to convert the image file.");
        }
    }
}
