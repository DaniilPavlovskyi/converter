package com.molekula.converter.utilities;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class Variables {

    public static final String UPLOAD_IMAGE = "Please upload an image file.";
    public static final String UPLOAD_SVG_IMAGE = "Please upload an SVG image file.";
    public static final String BAD_PATH = "Image path is outside of the target directory.";
    public static final String TARGET_CONVERT_DIRECTORY = "img/svg/convert/";
    public static final Path TARGET_CONVERT_PATH = new File(TARGET_CONVERT_DIRECTORY).toPath().normalize();
    public static final String ERROR_MESSAGE = "Error occurred: ";

    public static final List<String> DEFAULT_IMAGE_FORMATS = Arrays.asList("jpeg", "jpg", "png", "gif", "bmp", "tiff", "tif"/*
            for future
            ,"pbm", "pgm", "ppm" "svg", "webp", "heic", "raw", "ico"
            */);

}
