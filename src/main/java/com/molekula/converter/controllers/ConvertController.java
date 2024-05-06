package com.molekula.converter.controllers;

import com.molekula.converter.utilities.ByteArrayMultipartFile;
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
    private static final List<String> DEFAULT_IMAGE_FORMATS = Arrays.asList("jpeg", "jpg", "png", "gif", "bmp", "tiff", "tif"/*
            for future
            ,"pbm", "pgm", "ppm" "svg", "webp", "heic", "raw", "ico"
            */);
    private static final String UPLOAD_IMAGE = "Please upload an image file.";
    private static final String BAD_PATH = "Image path is outside of the target directory.";

    private static final String targetConvertDirectory = "img/svg/convert/convert-";
    private static final Path targetConvertPath = new File(targetConvertDirectory).toPath().normalize();

    @GetMapping("api/convert")
    public ResponseEntity<Object> convert(@RequestParam("file") MultipartFile file, @RequestParam("type") String type) {
        if (file.isEmpty() || file.getContentType() == null || isNotDefaultImage(file.getContentType())) {
            return ResponseEntity.badRequest().body(UPLOAD_IMAGE);
        }
        if (!DEFAULT_IMAGE_FORMATS.contains(type)) {
            return ResponseEntity.badRequest().body("Please select on of supported image type(" +
                    "\"jpeg\", \"jpg\", \"png\", \"gif\", \"bmp\", \"tiff\", \"tif\")");
        }
        byte[] imageData = DefaultImageConverterUtils.convert(file, type);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType("image/" + type)).body(imageData);
    }

    @GetMapping("api/convert-from-svg")
    public ResponseEntity<Object> convertFromSVG(@RequestParam("file") MultipartFile file, @RequestParam("type") String type) throws IOException {
        if (file.isEmpty() || file.getContentType() == null || isNotSVG(file.getContentType())) {
            return ResponseEntity.badRequest().body("Please upload an SVG image file.");
        }
        byte[] fileBytes = file.getBytes();
        Path path = Path.of(targetConvertPath +file.getOriginalFilename());
        if (isPathWrong(path)) {
            System.out.println(path);
            return ResponseEntity.badRequest().body(BAD_PATH);
        }
        Files.write(path, fileBytes);
        SVGConverterUtils.convertFromSVG(path.toString());

        File pngFile = new File(targetConvertPath + file.getOriginalFilename() + ".png");
        if (isPathWrong(pngFile.toPath())) {
            System.out.println(pngFile.toPath());
            return ResponseEntity.badRequest().body(BAD_PATH);
        }
        type = type.equals("jpg") ? "jpeg" : type.equals("tif") ? "tiff" : type;
        byte[] pngBytes = Files.readAllBytes(pngFile.toPath());
        if (type.equals("png")) {
            return ResponseEntity.ok().contentType(MediaType.parseMediaType("image/png")).body(pngBytes);
        } else {
            byte[] imageData = DefaultImageConverterUtils.convert(new ByteArrayMultipartFile(pngBytes,
                    file.getName(), file.getOriginalFilename(), file.getContentType()), type);
            return ResponseEntity.ok().contentType(MediaType.parseMediaType("image/" + type)).body(imageData);
        }
    }

    @GetMapping("api/convert-to-svg")
    public ResponseEntity<Object> convertToSVG(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty() || file.getContentType() == null || isNotSVG(file.getContentType())) {
            return ResponseEntity.badRequest().body(UPLOAD_IMAGE);
        }
        saveFile(file, file.getOriginalFilename(), "svg/convert/");
        if (isPathWrong(Path.of(targetConvertPath + file.getOriginalFilename()))) {
            return ResponseEntity.badRequest().body("Entry is outside of the target directory");
        }

        SVGConverterUtils.convertToSVG(targetConvertPath + file.getOriginalFilename());

        File svgFile = new File(targetConvertPath + file.getOriginalFilename() + ".svg");

        if (isPathWrong(svgFile.toPath())) {
            return ResponseEntity.badRequest().body("Entry is outside of the target directory");
        }
        byte[] svgBytes = Files.readAllBytes(svgFile.toPath());

        return ResponseEntity.ok().contentType(MediaType.parseMediaType("image/svg+xml")).body(svgBytes);
    }

    @GetMapping("api/resize")
    public ResponseEntity<Object> resizeImage(@RequestParam("file") MultipartFile file, @RequestParam("multiplier") double multiplier) {
        if (file.isEmpty() || file.getContentType() == null || isNotDefaultImage(file.getContentType())) {
            return ResponseEntity.badRequest().body(UPLOAD_IMAGE);
        }
        if (file.getContentType() == null) {
            return null;
        }

        byte[] resizedImageData = DefaultImageConverterUtils.resize(file, multiplier);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(file.getContentType())).body(resizedImageData);
    }

    @GetMapping("api/compress")
    public ResponseEntity<Object> compressImage(@RequestParam("file") MultipartFile file, @RequestParam("quality") double quality) {
        if (file.isEmpty() || file.getContentType() == null || isNotDefaultImage(file.getContentType())) {
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
    public ResponseEntity<Object> rotateImage(@RequestParam("file") MultipartFile file, @RequestParam("angle") double angle) throws IOException {
        if (file.isEmpty() || file.getContentType() == null || isNotDefaultImage(file.getContentType())) {
            return ResponseEntity.badRequest().body(UPLOAD_IMAGE);
        }

        byte[] resizedImageData = DefaultImageConverterUtils.rotate(file, angle);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(file.getContentType())).body(resizedImageData);
    }

    private boolean isNotDefaultImage(String type) {
        return !DEFAULT_IMAGE_FORMATS.contains(type);
    }

    private boolean isNotSVG(String type) {
        return !"image/svg+xml".equals(type);
    }

    private boolean isPathWrong(Path path) {
        return !path.normalize().toString().startsWith(targetConvertPath.toString());
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
