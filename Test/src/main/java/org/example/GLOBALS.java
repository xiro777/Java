package org.example;

import com.itextpdf.text.Utilities;
import sun.java2d.pipe.SpanShapeRenderer;

import java.text.SimpleDateFormat;
import java.util.Date;

public class GLOBALS {
    public static SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");

    public static float x_pixels = 595f;
    public static float y_pixels = 842f;

    public static int ADDRESS_BOX_X;// = 300;
    public static int ADDRESS_BOX_Y;//= 130;
    public static int ADDRESS_BOX_WIDTH;//= 300;
    public static int ADDRESS_BOX_HEIGHT;// = 200;


    public static int FONT_SIZE_DEFAULT;// = 8;

    public static String MATRIX_DATE;//= formatter.format(temp_date);


    public static String MATRIX_WYRÓŻNIK_FIRMY;// = "INW";
    public static String MATRIX_OPERATOR;//= "004";
    public static String MATRIX_UNIQUE_CLIENT_ID;//= "PIT1074";
    public static float MATRIX_POS_X = Utilities.millimetersToPoints(166.75f);
    public static float MATRIX_POX_Y = y_pixels - Utilities.millimetersToPoints(55f);
    public static float DATE_TEXT_CORD_X = Utilities.millimetersToPoints(116.75f);
    public static float DATE_TEXT_CORD_Y = y_pixels - Utilities.millimetersToPoints(54f);


    public static float BARCODE_POS_X = 0;
    public static float BARCODE_POX_Y = y_pixels - Utilities.millimetersToPoints(55f);
    public static float BARCODE_HEIGHT = Utilities.millimetersToPoints(10f);
    public static float BARCODE_WIDTH = 0;

    public static String BARCODE_FOLDER = "C:/etc/projects/Test/Barcodes";


}
