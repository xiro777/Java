package org.example;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

import java.awt.geom.AffineTransform;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ConnectPDFs {
    public static void processPDF() throws IOException, DocumentException {
        String path = "C:/etc/projects/ConnectingPDFs/Komplety";
        String file = "CompletedPDF.pdf";
        String out = "C:/etc/projects/ConnectingPDFs"+File.separator+file;
        File folder = new File(path);
        String[] filesName = folder.list();
        Document doc = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(doc,new FileOutputStream(out));
        doc.open();
        int k = 1;
        PdfContentByte cb = writer.getDirectContent();

        for(int i = 0; i < filesName.length;i++ )
        {
            PdfReader reader = new PdfReader(path+File.separator+filesName[i]);
            for (int j = 1 ; j <= reader.getNumberOfPages();j++)
            {
                doc.newPage();
                PdfTemplate page = writer.getImportedPage(reader,j);
                Rectangle pageSize = reader.getPageSize(j);

                System.out.println("Iteracja: " + k +" Size: " + reader.getPageSize(j) +  " Rotacja: " + reader.getPageRotation(j));
                if(pageSize.getWidth() > 595 || pageSize.getHeight() > 842) {
                    AffineTransform transform = new AffineTransform();
                    if(reader.getPageRotation(j) == 90)
                    {
                        transform.translate(-15,pageSize.getWidth()+50);
                        transform.rotate(-Math.PI/2);
                        transform.scale(1/(pageSize.getHeight()/840),1/(pageSize.getWidth()/593));
                    }
                    if(reader.getPageRotation(j) == 270)
                    {

                        if(pageSize.getWidth() < 843 && pageSize.getHeight() < 596) {
                            transform.translate(reader.getPageSize(j).getHeight(), 0);
                            transform.rotate(Math.PI / 2);
                        }
                        else
                        {
                            transform.scale(1/(pageSize.getHeight()/840),1/(pageSize.getWidth()/593));
                        }
                    }
                    else {
                        transform.scale(1/(pageSize.getWidth()/593),1/(pageSize.getHeight()/840));
                    }
                    cb.addTemplate(page,transform);
                }
                else {

                    cb.addTemplate(page, 0, 0);
                }


                k++;
            }

            }
        doc.close();
        System.out.println("Tworzenie pliku "+file+ " zakończono pomyslnie");
    }

    public static void checkNewFile(String filename) throws IOException {
        PdfReader reader = new PdfReader(filename);
        for ( int i = 1 ; i < reader.getNumberOfPages(); i++)
        {
            System.out.println("Size: " + reader.getPageSizeWithRotation(i));
        }

    }

    public static void RunPDFOptimalizer()
    {
        try
        {
            ProcessBuilder builder = new ProcessBuilder("cmd.exe","/c","cd \"C:/etc/libs\" && java -jar PdfOptimizer.jar C:/etc/projects/ConnectingPDFs C:/etc/projects/ConnectingPDFs/out");
            builder.redirectErrorStream(true);
            Process p = builder.start();
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while(true)
            {
                line = r.readLine();
                if(line ==null)
                {
                    break;
                }
                System.out.println(line);
            }
        }
        catch (Exception e)
        {
            System.out.println("Blad podczas wykonywania komendy do uruchomienia PdfOptymalizer.jar: " + e.getMessage());
        }
        System.out.println("Utowrzono zoptymalizowany plik");
    }


}