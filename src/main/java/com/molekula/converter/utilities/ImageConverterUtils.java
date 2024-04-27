package com.molekula.converter.utilities;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ImageConverterUtils {

    public static byte[] convertPNGtoJPG(byte[] img) {
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(img);
            BufferedImage originalImage = ImageIO.read(bis);
            bis.close();

            BufferedImage newImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_RGB);
            newImage.createGraphics().drawImage(originalImage, 0, 0, null);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ImageIO.write(newImage, "jpg", bos);
            bos.close();

            return bos.toByteArray();
        } catch (IOException e) {
            System.out.println("Error occurred: " + e.getMessage());
            return null;
        }
    }

    public static byte[] resize(byte[] img, double multiplier, String filename) {
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(img);
            BufferedImage originalImage = ImageIO.read(bis);
            bis.close();

            int newWidth = (int)(originalImage.getWidth() * multiplier);
            int newHeight = (int)(originalImage.getHeight() * multiplier);

            BufferedImage newImage = new BufferedImage(newWidth, newHeight, originalImage.getType());
            newImage.createGraphics().drawImage(originalImage, 0, 0, newWidth, newHeight, null);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ImageIO.write(newImage, getImageFormat(filename), bos);
            bos.close();

            return bos.toByteArray();
        } catch (IOException e) {
            System.out.println("Error occurred: " + e.getMessage());
            return null;
        }
    }

    private static String getImageFormat(String filename) {
        String [] split = filename.split("\\.");
        System.out.println(split[split.length - 1]);
        return split[split.length - 1];
    }
}
