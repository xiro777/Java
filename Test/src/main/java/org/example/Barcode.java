package org.example;

import com.itextpdf.text.*;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.Barcode128;
import com.itextpdf.text.pdf.BarcodeDatamatrix;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class Barcode {

    public static void createRectangle(PdfContentByte cb, float x, float y, float w, float h) {
        cb.rectangle(x, y, w, h);
        cb.setColorStroke(BaseColor.WHITE);
        cb.setColorFill(BaseColor.WHITE);
        cb.fillStroke();

    }

    public static Image createBarcode(PdfContentByte cb, String input_code, int packetNo) throws IOException {
        Barcode128 code = new Barcode128();
        code.setBarHeight(GLOBALS.BARCODE_HEIGHT - 6);
        code.setCode(input_code);
        code.setFont(null);
        code.setBaseline(0);

        //Creating barcode image to save it to file
        saveBarcodeToFile(code, input_code, packetNo);
        //Image created to put it in pdf file
        Image image = code.createImageWithBarcode(cb, BaseColor.BLACK, null);
        image.setAbsolutePosition(GLOBALS.BARCODE_POS_X + 3, GLOBALS.BARCODE_POX_Y - code.getBarcodeSize().getWidth());

        //rotate to begin of code be at the top
        image.setRotationDegrees(90);
        GLOBALS.BARCODE_WIDTH = image.getWidth();
        return image;
    }

    public static void saveBarcodeToFile(Barcode128 code, String input_code, int packetNo) throws IOException {
        //Creating folder for barcodes
        int padding = 5;
        File folder = new File(GLOBALS.BARCODE_FOLDER);
        if (!folder.exists()) {
            folder.mkdir();
        }
        java.awt.Image img = code.createAwtImage(Color.BLACK, Color.WHITE);
        BufferedImage buffimg = new BufferedImage(img.getWidth(null) + padding*2 , img.getHeight(null) + padding*2, BufferedImage.TYPE_INT_RGB);

        Graphics2D g = buffimg.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0,0, buffimg.getWidth(),buffimg.getHeight());
        g.drawImage(img,padding,padding,null);
        g.dispose();

        File file = new File(folder, "Packet_" + packetNo + "_barcode_" + input_code + ".jpg");
        ImageIO.write(buffimg, "jpg", file);
    }


    public static Image createMatrix(PdfContentByte cb, String matrix_code) throws IOException, BadElementException {
        BarcodeDatamatrix datamatrix = new BarcodeDatamatrix();
        datamatrix.setHeight((int) Utilities.millimetersToPoints(30f));
        datamatrix.generate(matrix_code);
        Image image = datamatrix.createImage();

        int padding = 5;

        java.awt.Image img = datamatrix.createAwtImage(Color.BLACK,Color.WHITE).getScaledInstance((int) (image.getWidth()*5), (int) (image.getHeight()*5), java.awt.Image.SCALE_SMOOTH);
        BufferedImage buffimg = new BufferedImage(img.getWidth(null) + padding*2,img.getHeight(null) + padding*2,BufferedImage.TYPE_INT_RGB);

        Graphics2D g = buffimg.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0,0,buffimg.getWidth(),buffimg.getHeight());
        g.drawImage(img,padding,padding,null);
        g.dispose();

        File folder = new File("C:\\etc\\projects\\Test\\Matrix");
        if (!folder.exists()) {
            folder.mkdir();
        }

        File file = new File(folder, "img_"+ matrix_code.replace('/','.') + ".jpg");
        ImageIO.write(buffimg, "jpg", file);

        image.setAbsolutePosition(GLOBALS.MATRIX_POS_X, GLOBALS.MATRIX_POX_Y);
        return image;
    }

    public static String generateBarcodeText(int current_page, int packetNo) {
        String matrix = "";
        String sekcja1;
        String sekcja2;
        String sekcja3;
        if (current_page % 6 == 3 || current_page % 6 == 5) {
            sekcja1 = String.format("%04d", 2);
            sekcja2 = String.format("%04d", current_page / 2 + 1);
            sekcja3 = String.format("%08d", packetNo);
            matrix = sekcja1 + sekcja2 + sekcja3;
        } else if (current_page % 6 == 1) {
            sekcja1 = String.format("%04d", 1);
            sekcja2 = String.format("%04d", current_page / 2 + 1);
            sekcja3 = String.format("%08d", packetNo);
            matrix = sekcja1 + sekcja2 + sekcja3;
        } else {
            matrix = "";
        }
        return matrix;
    }

    public static String generateMatrixCode(int packetNo) {
        String matrix = "";
        String packetNo_formatted = String.format("%06d", packetNo);
        matrix = GLOBALS.MATRIX_WYRÓŻNIK_FIRMY + "_" + GLOBALS.MATRIX_DATE + "_" + GLOBALS.MATRIX_OPERATOR + "P_" + GLOBALS.MATRIX_UNIQUE_CLIENT_ID + packetNo_formatted + "_X" + packetNo_formatted;
        return matrix;
    }

    public static void addText(PdfContentByte cb, String text, int font_size, float x, float y, float rotate) throws DocumentException, IOException {
        cb.setColorFill(BaseColor.BLACK);
        cb.beginText();
        BaseFont font = BaseFont.createFont("Helvetica", "winansi", false);
        cb.setFontAndSize(font, font_size);
        if (rotate == 0)
            cb.setTextMatrix(1, 0, 0, 1, x, y);
        else if (rotate == 1)
            cb.setTextMatrix(0, -1, 1, 0, x, y);
        else if (rotate == 2)
            cb.setTextMatrix(0, 1, -1, 0, x, y);

        cb.showText(text);
        cb.endText();
    }

    public static int[] crateOMRArray(String code1, int pageNo, int packetNo) {
        String s1 = code1.substring(0, 4);
        int[] omr_arr = new int[19];
        //ITERATOR PARZYSTOSCI
        int j = 0;
        //SN START
        omr_arr[0] = 1;
        j++;
        //LK WYSTEPUJE ZAWSZE
        omr_arr[2] = 1;
        j++;
        //DZ WEZ NASTEPNĄ
        if (s1.equals("0002")) {
            omr_arr[3] = 1;
            j++;
        }
        //DGR KONIEC PAKUJ
        if (s1.equals("0001")) {
            omr_arr[4] = 1;
            j++;
        }
        //SEKWENCJA KARTEK ROSNĄCA. SPRAWDZA CZY PRZEKAZYWANY ITERATOR STRON NA KTORYCH JEST OMR JEST NA 3 BITACH
        if (pageNo % 7 == 0) {
            omr_arr[5] = 1;
            omr_arr[6] = 1;
            omr_arr[7] = 1;
            j = j + 3;
        } else if (pageNo % 7 == 1) {
            omr_arr[5] = 1;
            j++;
        } else if (pageNo % 7 == 2) {
            omr_arr[6] = 1;
            j++;
        } else if (pageNo % 7 == 3) {
            omr_arr[5] = 1;
            omr_arr[6] = 1;
            j = j + 2;
        } else if (pageNo % 7 == 4) {
            omr_arr[7] = 1;
            j++;
        } else if (pageNo % 7 == 5) {
            omr_arr[5] = 1;
            omr_arr[7] = 1;
            j = j + 2;
        } else if (pageNo % 7 == 6) {
            omr_arr[6] = 1;
            omr_arr[7] = 1;
            j = j + 2;
        }

        //SEKWENCJA PAKIETÓW ROSNĄCA. SPRAWDZA JAKIE JEST MODULO 7 NA NUMERZE PAKIETU, CZYLI ZAPIS NA 3 BITACH
        if (packetNo % 7 == 0) {
            omr_arr[14] = 1;
            omr_arr[15] = 1;
            omr_arr[16] = 1;
            j = j + 3;
        } else if (packetNo % 7 == 1) {
            omr_arr[14] = 1;
            j++;
        } else if (packetNo % 7 == 2) {
            omr_arr[15] = 1;
            j++;
        } else if (packetNo % 7 == 3) {
            omr_arr[14] = 1;
            omr_arr[15] = 1;
            j = j + 2;
        } else if (packetNo % 7 == 4) {
            omr_arr[16] = 1;
            j++;
        } else if (packetNo % 7 == 5) {
            omr_arr[14] = 1;
            omr_arr[16] = 1;
            j = j + 2;
        } else if (packetNo % 7 == 6) {
            omr_arr[15] = 1;
            omr_arr[16] = 1;
            j = j + 2;
        }
        //PR PARZYSTOŚĆ ZLICZONE J SPRAWDZA CZY JEST RESZTA PODZIELNA PRZEZ 2 JEST ROWNA 1 JESLI TAK DODAJE WARTOSC PR JESLI NIE TO NIE
        if (j % 2 == 1) {
            omr_arr[17] = 1;
            j++;
        }

        return omr_arr;
    }


    public static void drawLine(PdfContentByte cb) {
        float line_width = Utilities.millimetersToPoints(9);
        float line_tickness = Utilities.millimetersToPoints(0.5f);

        cb.setLineWidth(line_tickness);
        cb.setColorStroke(BaseColor.BLACK);
        cb.moveTo(PageSize.A4.getWidth() - line_width, Utilities.millimetersToPoints(5));
        cb.lineTo(PageSize.A4.getWidth(), Utilities.millimetersToPoints(5));
        cb.stroke();
    }

    public static void createOMRLine(PdfContentByte cb, int[] omr_arr, float x, float y) {
        float line_width = Utilities.millimetersToPoints(8f);
        float line_tickness = Utilities.millimetersToPoints(0.5f);
        float dist_between_line = Utilities.millimetersToPoints(4.2f);
        float beggining_spacing = 0f;

        for (int i = 0; i < omr_arr.length; i++) {
            if (omr_arr[i] == 1) {
                cb.setLineWidth(line_tickness);
                cb.setColorStroke(BaseColor.BLACK);
                cb.moveTo(x, y - beggining_spacing);
                cb.lineTo(x + line_width, y - beggining_spacing);
                cb.stroke();

            }
            beggining_spacing = beggining_spacing + dist_between_line;

        }
    }


}
