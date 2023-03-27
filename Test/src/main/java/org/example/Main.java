package org.example;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import jdk.internal.util.xml.impl.Input;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;


import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;


import static java.lang.System.exit;
import static java.lang.System.setOut;


public class Main {
    public static void main(String[] args)  {
        try {
            String filename = "Deklaracja PIT 11 Inwemer z oo.PDF";
            Proper p1 = new Proper("config.ini");
            System.out.println(p1.init());
            if (p1.init()) {
                if (!p1.loadProperties()) {
                    System.exit(1);
                }
            } else {
                System.exit(1);
            }

            //PdfImportedPage page = PDF.getPageFromPdf(filename, 2);
            String temp_name = PDF.addBlankPage(filename);
            //System.out.println(temp_name);
            PDF.processPDF(temp_name);
            //PDF.deleteFile(temp_name);
            //List<String> a = PDF.getAddressData(filename);
            //Raporty.createAddressLog(filename);

            //List<String> wyjatki = PDF.getPacketData(filename,a);
            //List<Packet> packets = PDF.obslugawyjatkow(wyjatki,filename);
            //Raporty.createPacketsLog(filename);

            //PDPage page = PDF.getPageFromPDF(filename,0);
            //String a = PDF.getWholeTextPage(filename,0);
            //PDF.getTextFromArea(filename,0);

            //PDPage page = PDF.getSinglePageFromPdf(filename,3);
            //String text = PDF.getTextFromPage(page);


        }
        catch (Exception e)
        {
            System.out.println("Blad: "+ e);
            exit(1);
        }


    }
}