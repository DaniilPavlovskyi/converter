package com.molekula.converter.utilities;

import com.aspose.words.*;

import static com.molekula.converter.utilities.Variables.ERROR_MESSAGE;

public class SVGConverterUtils {

    public static void convertToSVG(String file) {
        try {
            Document doc = new Document();
            DocumentBuilder builder = new DocumentBuilder(doc);

            Shape shape = builder.insertImage(file);
            shape.getShapeRenderer().save(file + ".svg", new ImageSaveOptions(SaveFormat.SVG));
        } catch (Exception e) {
            System.out.println(ERROR_MESSAGE + e.getMessage());
        }
    }
    public static void convertFromSVG(String file) {
        try {
            Document doc = new Document();
            DocumentBuilder builder = new DocumentBuilder(doc);

            Shape shape = builder.insertImage(file);
            shape.getShapeRenderer().save(file + ".png", new ImageSaveOptions(SaveFormat.PNG));
        } catch (Exception e) {
            System.out.println(ERROR_MESSAGE + e.getMessage());
        }
    }
}
