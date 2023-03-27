package org.example;

import com.itextpdf.text.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class Main {
    public static void main(String[] args) throws IOException, DocumentException {
        ConnectPDFs.processPDF();
        //ConnectPDFs.checkNewFile("Test_last.pdf");
        ConnectPDFs.RunPDFOptimalizer();
    }
}