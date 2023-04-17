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
            String filename1 = "PD14489.2023-04-06.G14.C1.paper123.pdf";
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
            //PDF.createPage();
            //PDF.openPDFpage(filename1);
            //PDF.getPageFromPdf(filename1,790);
            //PDF.getSomePages();
            //PDF.RunPDFOptimalizer();
//            String text = PDF.readBarcodeAndQRCodeFromImage("C:/etc/projects/Test/Matrix/img_INW_25.03.2023_004P_PIT1074000001_X000001.jpg");
//            System.out.println(text);
//            String temp_name = PDF.addBlankPage(filename);
//            PDF.processPDF(temp_name);
//            PDF.extractBarcodeFromPdf(file);
//            try {
//                List<BufferedImage> matrixImage = PDF.getMatrixImage(file);
//                File folder = new File("C:\\etc\\projects\\Test\\img");
//                if(!folder.exists())
//                {
//                    folder.mkdir();
//                }
//                for(int i = 0 ; i < matrixImage.size();i++)
//                {
//                    ImageIO.write(matrixImage.get(i),"jpg",new FileOutputStream(folder+File.separator+ "img"+i+".jpg"));
//                }
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