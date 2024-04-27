package com.molekula.converter.utilities;

import org.springframework.beans.factory.annotation.Value;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
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

            byte[] jpgImageData = bos.toByteArray();

            System.out.println("Conversion completed successfully.");

            return jpgImageData;
        } catch (IOException e) {
            System.out.println("Error occurred: " + e.getMessage());
            return null;
        }
    }
}
