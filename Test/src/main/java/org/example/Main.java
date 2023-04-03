package org.example;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import static java.lang.System.exit;


public class Main {
    public static void main(String[] args) {
        try {
            String filename = "Deklaracja PIT 11 Inwemer z oo.PDF";
            File file = new File("output.pdf");
            Proper p1 = new Proper("config.ini");
            System.out.println(p1.init());
            if (p1.init()) {
                if (!p1.loadProperties()) {
                    System.exit(1);
                }
            } else {
                System.exit(1);
            }
            String temp_name = PDF.addBlankPage(filename);
            PDF.processPDF(temp_name);
            //PDF.extractBarcodeFromPdf(file);
//            try {
//                List<BufferedImage> matrixImage = PDF.getMatrixImage(file);
////                for(int i = 0 ; i < matrixImage.size();i++)
////                {
////                    ImageIO.write(matrixImage.get(i),"jpg",new FileOutputStream("img"+i+".jpg"));
////                }
//
//            } catch (Exception e) {
//                System.err.println(e.getMessage());
//            }


        } catch (Exception e) {
            System.out.println("Blad: " + e);
            exit(1);
        }


    }
}