package com.molekula.converter.controllers;

import com.molekula.converter.utilities.DefaultImageConverterUtils;
import com.molekula.converter.utilities.SVGConverterUtils;
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
    private static final List<String> IMAGE_FORMATS = Arrays.asList("jpeg", "jpg", "png", "gif", "bmp", "tiff", "tif", "pbm", "pgm", "ppm", "svg+xml", "webp", "heic", "raw", "ico");
    private static final List<String> DEFAULT_IMAGE_FORMATS = Arrays.asList("jpeg", "jpg", "png", "gif", "bmp", "tiff", "tif"/*
            for future
            ,"pbm", "pgm", "ppm" "svg", "webp", "heic", "raw", "ico"
            */);

    @GetMapping("api/convert")
    public ResponseEntity<Object> convert(@RequestParam("file") MultipartFile file, @RequestParam("type") String type) {
        if (isNotDefaultImage(file)) {
            return ResponseEntity.badRequest().body("Please upload an image file.");
        }
        if (!DEFAULT_IMAGE_FORMATS.contains(type)) {
            return ResponseEntity.badRequest().body("Please select on of supported image type(" +
                    "\"jpeg\", \"jpg\", \"png\", \"gif\", \"bmp\", \"tiff\", \"tif\")");
        }
        byte[] imageData = DefaultImageConverterUtils.convert(file, type);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType("image/" + type)).body(imageData);
         /*else if (type.equals("svg")) {
            saveFile(file, file.getOriginalFilename(), "svg/convert/");
            DefaultImageConverterUtils.convertToSVG("img/svg/convert/" + file.getOriginalFilename());

            File svgFile = new File("img/svg/convert/" + file.getOriginalFilename() + ".svg");
            byte[] svgBytes = Files.readAllBytes(svgFile.toPath());

            return ResponseEntity.ok().contentType(MediaType.parseMediaType("image/svg+xml")).body(svgBytes);
        }*/
    }

    @GetMapping("api/convert-from-svg")
    public ResponseEntity<Object> convertFromSVG(@RequestParam("file") MultipartFile file, @RequestParam("type") String type) throws IOException {
        if (file.getContentType() == null || !file.getContentType().equals("image/svg+xml")) {
            return ResponseEntity.badRequest().body("Please upload an SVG image file.");
        }
        return null;
    }

    @GetMapping("api/convert-to-svg")
    public ResponseEntity<Object> convertToSVG(@RequestParam("file") MultipartFile file) throws IOException {
        if (isNotDefaultImage(file)) {
            return ResponseEntity.badRequest().body("Please upload an image file.");
        }
        saveFile(file, file.getOriginalFilename(), "svg/convert/");
        SVGConverterUtils.convertToSVG("img/svg/convert/" + file.getOriginalFilename());

        File svgFile = new File("img/svg/convert/" + file.getOriginalFilename() + ".svg");
        byte[] svgBytes = Files.readAllBytes(svgFile.toPath());

        return ResponseEntity.ok().contentType(MediaType.parseMediaType("image/svg+xml")).body(svgBytes);
    }

    @GetMapping("api/resize")
    public ResponseEntity<Object> resizeImage(@RequestParam("file") MultipartFile file, @RequestParam("multiplier") double multiplier) throws IOException {
        if (isNotDefaultImage(file)) {
            return ResponseEntity.badRequest().body("Please upload an image file.");
        }

        byte[] resizedImageData = DefaultImageConverterUtils.resize(file, multiplier);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(file.getContentType())).body(resizedImageData);
         /*else if (file.getContentType().equals("image/svg+xml")) {
            saveFile(file, file.getOriginalFilename(), "svg/resize/");
            File svgFile = new File("img/svg/resize/" + file.getOriginalFilename());

            DefaultImageConverterUtils.convertFromSVG(svgFile.getPath());
            File pngFile = new File("img/svg/resize/" + file.getOriginalFilename() + ".png");
            byte[] resizedImg = DefaultImageConverterUtils.resize(pngFile, multiplier);
            Files.write(pngFile.toPath(), resizedImg);

            DefaultImageConverterUtils.convertToSVG(pngFile.getPath());

            byte[] svgBytes = Files.readAllBytes(Path.of("img/svg/resize/" + file.getOriginalFilename() + ".png.svg"));
            return ResponseEntity.ok().contentType(MediaType.parseMediaType("image/svg+xml")).body(svgBytes);
        }*/
    }

    @GetMapping("api/compress")
    public ResponseEntity<Object> compressImage(@RequestParam("file") MultipartFile file, @RequestParam("quality") double quality) {
        if (isNotDefaultImage(file)) {
            return ResponseEntity.badRequest().body("Please upload an image file.");
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
    public ResponseEntity<Object> rotateImage(@RequestParam("file") MultipartFile file, @RequestParam("angle") double angle) throws IOException {
        if (isNotDefaultImage(file)) {
            return ResponseEntity.badRequest().body("Please upload an image file.");
        }

        byte[] resizedImageData = DefaultImageConverterUtils.rotate(file, angle);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(file.getContentType())).body(resizedImageData);
    }

    private boolean isNotDefaultImage(MultipartFile file) {
        return file.isEmpty() || file.getContentType() == null || !DEFAULT_IMAGE_FORMATS.contains(file.getContentType().split("/")[1]);
    }

    private void saveFile(MultipartFile multipartFile, String fileName, String type) {
        try {
            byte[] fileBytes = multipartFile.getBytes();
            Path uploadPath = Paths.get("img/" + type);
            Path filePath = uploadPath.resolve(fileName);
            Files.write(filePath, fileBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
