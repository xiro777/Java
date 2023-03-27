package org.example;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.*;

import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ConnectPDFs {
    public static void processPDF() throws IOException, DocumentException {
        String path = "Komplety";
        String out = "Test_last.pdf";
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

                System.out.println("Iteracja: " + k +" Size: "+reader.getPageSize(j) +  " Rotacja: " +reader.getPageRotation(j));
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
    }

    public static void checkNewFile(String filename) throws IOException {
        PdfReader reader = new PdfReader(filename);
        for ( int i = 1 ; i < reader.getNumberOfPages(); i++)
        {
            System.out.println("Size: " + reader.getPageSizeWithRotation(i));
        }

    }


}