package com.molekula.converter.utilities;

import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Iterator;

import static com.molekula.converter.utilities.Variables.DEFAULT_IMAGE_FORMATS;
import static com.molekula.converter.utilities.Variables.ERROR_MESSAGE;

public class DefaultImageConverterUtils {


    public static byte[] convert(MultipartFile file, String type) {
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(file.getBytes());
            BufferedImage originalImage = ImageIO.read(bis);
            bis.close();

            BufferedImage newImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_RGB);
            newImage.createGraphics().drawImage(originalImage, 0, 0, null);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ImageIO.write(newImage, type, bos);
            bos.close();

            return bos.toByteArray();
        } catch (Exception e) {
            System.out.println(ERROR_MESSAGE + e.getMessage());
            return null;
        }
    }

    public static byte[] compress(MultipartFile file, double multiplier) {
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(file.getBytes());
            BufferedImage originalImage = ImageIO.read(bis);
            bis.close();

            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(file.getContentType().split("/")[1]);
            ImageWriter writer = writers.next();

            ImageOutputStream ios = ImageIO.createImageOutputStream(bos);
            writer.setOutput(ios);

            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality((float) multiplier);

            writer.write(null, new IIOImage(originalImage, null, null), param);

            bos.close();
            ios.close();
            writer.dispose();

            return bos.toByteArray();
        } catch (Exception e) {
            System.out.println(ERROR_MESSAGE + e.getMessage());
            return null;
        }
    }

    public static byte[] rotate(MultipartFile file, double angleDegrees) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(file.getBytes());
        BufferedImage originalImage = ImageIO.read(bis);
        bis.close();

        BufferedImage rotatedImage = rotate(originalImage, angleDegrees);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(rotatedImage, file.getContentType().split("/")[1], bos);
        bos.close();

        return bos.toByteArray();
    }

    private static BufferedImage rotate(BufferedImage image, double angleDegrees) {
        double radians = Math.toRadians(angleDegrees);
        double sin = Math.abs(Math.sin(radians));
        double cos = Math.abs(Math.cos(radians));
        int newWidth = (int) Math.round(image.getWidth() * cos + image.getHeight() * sin);
        int newHeight = (int) Math.round(image.getWidth() * sin + image.getHeight() * cos);

        BufferedImage rotatedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = rotatedImage.createGraphics();
        AffineTransform transform = new AffineTransform();
        transform.translate((newWidth - image.getWidth()) / 2.0, (newHeight - image.getHeight()) / 2.0);
        int x = image.getWidth() / 2;
        int y = image.getHeight() / 2;
        transform.rotate(Math.toRadians(angleDegrees), x, y);
        g2d.setTransform(transform);
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();

        return rotatedImage;
    }


    public static byte[] resize(InputStream inputStream, double multiplier) throws IOException {
        BufferedImage originalImage = ImageIO.read(inputStream);
        inputStream.close();

        int newWidth = (int) (originalImage.getWidth() * multiplier);
        int newHeight = (int) (originalImage.getHeight() * multiplier);

        BufferedImage newImage = new BufferedImage(newWidth, newHeight, originalImage.getType());
        newImage.createGraphics().drawImage(originalImage, 0, 0, newWidth, newHeight, null);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(newImage, "png", bos);
        bos.close();

        return bos.toByteArray();
    }

    public static byte[] resize(MultipartFile file, double multiplier) {
        try {
            return resize(file.getInputStream(), multiplier);
        } catch (IOException e) {
            System.out.println(ERROR_MESSAGE + e.getMessage());
            return null;
        }
    }

    public static boolean isNotDefaultImage(String type) {
        return DEFAULT_IMAGE_FORMATS.stream().noneMatch(type::contains);
    }

    public static boolean isFileEmptyOrNotDefaultType(MultipartFile file) {
        return file.isEmpty() ||
                file.getContentType() == null ||
                file.getContentType().split("/") == null ||
                isNotDefaultImage(file.getContentType());
    }
}
