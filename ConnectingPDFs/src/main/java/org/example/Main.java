package org.example;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfReader;

import java.io.IOException;
import java.util.List;


public class Main {
    public static void main(String[] args) throws IOException, DocumentException {
        ConnectPDFs.processPDF();
        //ConnectPDFs.checkNewFile("Test_last.pdf");
        ConnectPDFs.RunPDFOptimalizer();
        //ConnectPDFs.checkNewFile("64953957-83136406-LW05.pdf");
    }
}