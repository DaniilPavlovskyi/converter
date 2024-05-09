package com.molekula.converter.controllers;

import com.molekula.converter.utilities.ByteArrayMultipartFile;
import com.molekula.converter.utilities.DefaultImageConverterUtils;
import com.molekula.converter.utilities.SVGConverterUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.molekula.converter.utilities.DefaultImageConverterUtils.*;
import static com.molekula.converter.utilities.Variables.*;

@RestController
public class SVGConvertController {

    @PostMapping("api/svg/convert-from")
    public ResponseEntity<Object> convertFrom(@RequestParam("file") MultipartFile file, @RequestParam("type") String type) throws IOException {
        if (isNotSVG(file.getContentType())) {
            return ResponseEntity.badRequest().body(UPLOAD_SVG_IMAGE);
        }

        if (!DEFAULT_IMAGE_FORMATS.contains(type)) {
            return ResponseEntity.badRequest().body("Please select on of supported image type(" +
                    "\"jpeg\", \"jpg\", \"png\", \"gif\", \"bmp\", \"tiff\", \"tif\")");
        }

        Path path = Path.of(TARGET_CONVERT_PATH + "/" + file.getOriginalFilename());
        if (isPathWrong(path, TARGET_CONVERT_PATH.toString())) {
            return ResponseEntity.badRequest().body(BAD_PATH);
        }
        saveFile(file, file.getOriginalFilename());

        SVGConverterUtils.convertFromSVG(path.toString());
        File pngFile = new File(TARGET_CONVERT_PATH + "/" + file.getOriginalFilename() + ".png");
        if (isPathWrong(pngFile.toPath(), TARGET_CONVERT_PATH.toString())) {
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

    @PostMapping("api/svg/convert-to")
    public ResponseEntity<Object> convertTo(@RequestParam("file") MultipartFile file) throws IOException {
        if (isFileEmptyOrNotDefaultType(file)) {
            return ResponseEntity.badRequest().body(UPLOAD_IMAGE);
        }
        saveFile(file, file.getOriginalFilename());
        if (isPathWrong(Path.of(TARGET_CONVERT_PATH + "/" + file.getOriginalFilename()), TARGET_CONVERT_PATH.toString())) {
            return ResponseEntity.badRequest().body(BAD_PATH);
        }

        SVGConverterUtils.convertToSVG(TARGET_CONVERT_PATH + "/" + file.getOriginalFilename());

        File svgFile = new File(TARGET_CONVERT_PATH + "/" + file.getOriginalFilename() + ".svg");

        if (isPathWrong(svgFile.toPath(), TARGET_CONVERT_PATH.toString())) {
            return ResponseEntity.badRequest().body(BAD_PATH);
        }
        byte[] svgBytes = Files.readAllBytes(svgFile.toPath());

        return ResponseEntity.ok().contentType(MediaType.parseMediaType("image/svg+xml")).body(svgBytes);
    }

    private boolean isNotSVG(String type) {
        return !"image/svg+xml".equals(type);
    }

    private boolean isPathWrong(Path path, String targetPath) {
        return !path.normalize().toString().startsWith(targetPath);
    }


    private void saveFile(MultipartFile multipartFile, String fileName) {
        try {
            byte[] fileBytes = multipartFile.getBytes();
            Path uploadPath = Paths.get("img/svg/convert/");
            Path filePath = uploadPath.resolve(fileName);
            Files.write(filePath, fileBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
