package org.example;

import com.itextpdf.text.*;
import java.io.IOException;


public class Main {
    public static void main(String[] args) throws IOException, DocumentException {
        ConnectPDFs.processPDF();
        //ConnectPDFs.checkNewFile("Test_last.pdf");
        try
        {
            Process process = Runtime.getRuntime().exec(new String[]{"cmd.exe","C:/etc/libs","PdfOptimizer.jar","java -jar PdfOptimalizer.jar C:/etc/projects/ConnectingPDFs C:/etc/projects/ConnectingPDFs/out"});
        }
        catch (Exception e)
        {
            System.out.println("BLAD: " + e.getMessage());
        }
    }
}