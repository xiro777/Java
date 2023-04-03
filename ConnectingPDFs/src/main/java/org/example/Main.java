package org.example;

import com.itextpdf.text.*;
import java.io.IOException;


public class Main {
    public static void main(String[] args) throws IOException, DocumentException {
        ConnectPDFs.processPDF();
        //ConnectPDFs.checkNewFile("Test_last.pdf");
        ConnectPDFs.RunPDFOptimalizer();
    }
}