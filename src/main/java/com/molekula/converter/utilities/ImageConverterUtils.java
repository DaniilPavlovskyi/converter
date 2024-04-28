package com.molekula.converter.utilities;

import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Iterator;

public class ImageConverterUtils {
    public static byte[] convertToDefaultType(MultipartFile file, String type) {
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
            System.out.println("Error occurred: " + e.getMessage());
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
            System.out.println("Error occurred: " + e.getMessage());
            return null;
        }
    }

    public static byte[] resize(MultipartFile file, double multiplier) {
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(file.getBytes());
            BufferedImage originalImage = ImageIO.read(bis);
            bis.close();

            int newWidth = (int) (originalImage.getWidth() * multiplier);
            int newHeight = (int) (originalImage.getHeight() * multiplier);

            BufferedImage newImage = new BufferedImage(newWidth, newHeight, originalImage.getType());
            newImage.createGraphics().drawImage(originalImage, 0, 0, newWidth, newHeight, null);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ImageIO.write(newImage, file.getContentType().split("/")[1], bos);
            bos.close();

            return bos.toByteArray();
        } catch (Exception e) {
            System.out.println("Error occurred: " + e.getMessage());
            return null;
        }
    }
}
