package com.molekula.converter.controllers;

import com.molekula.converter.utilities.DefaultImageConverterUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static com.molekula.converter.utilities.DefaultImageConverterUtils.isFileEmptyOrNotDefaultType;
import static com.molekula.converter.utilities.Variables.DEFAULT_IMAGE_FORMATS;
import static com.molekula.converter.utilities.Variables.UPLOAD_IMAGE;

@RestController
public class DefaultConvertController {

    @GetMapping("api/convert")
    public ResponseEntity<Object> convert(@RequestParam("file") MultipartFile file, @RequestParam("type") String type) {
        if (isFileEmptyOrNotDefaultType(file)) {
            return ResponseEntity.badRequest().body(UPLOAD_IMAGE);
        }
        if (!DEFAULT_IMAGE_FORMATS.contains(type)) {
            return ResponseEntity.badRequest().body("Please select on of supported image type(" +
                    "\"jpeg\", \"jpg\", \"png\", \"gif\", \"bmp\", \"tiff\", \"tif\")");
        }
        type = type.equals("jpg") ? "jpeg" : type.equals("tif") ? "tiff" : type;
        byte[] imageData = DefaultImageConverterUtils.convert(file, type);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType("image/" + type)).body(imageData);
    }
    @GetMapping("api/resize")
    public ResponseEntity<Object> resize(@RequestParam("file") MultipartFile file, @RequestParam("multiplier") double multiplier) {
        if (isFileEmptyOrNotDefaultType(file)) {
            return ResponseEntity.badRequest().body(UPLOAD_IMAGE);
        }
        if (file.getContentType() == null) {
            return null;
        }

        byte[] resizedImageData = DefaultImageConverterUtils.resize(file, multiplier);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(file.getContentType())).body(resizedImageData);
    }

    @GetMapping("api/compress")
    public ResponseEntity<Object> compress(@RequestParam("file") MultipartFile file, @RequestParam("quality") double quality) {
        if (isFileEmptyOrNotDefaultType(file)) {
            return ResponseEntity.badRequest().body(UPLOAD_IMAGE);
        }

        if (quality < 0 || quality > 1) {
            return ResponseEntity.badRequest().body("Quality should be in range [0, 1].");
        }

        try {
            byte[] originalImageData = file.getBytes();
            byte[] compressedImageData = DefaultImageConverterUtils.compress(file, quality);

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

    @GetMapping("api/rotate")
    public ResponseEntity<Object> rotate(@RequestParam("file") MultipartFile file, @RequestParam("angle") double angle) throws IOException {
        if (isFileEmptyOrNotDefaultType(file)) {
            return ResponseEntity.badRequest().body(UPLOAD_IMAGE);
        }

        byte[] resizedImageData = DefaultImageConverterUtils.rotate(file, angle);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(file.getContentType())).body(resizedImageData);
    }


}
