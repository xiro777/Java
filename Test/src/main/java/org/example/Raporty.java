package org.example;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Raporty {
    public static void createAddressLog(String filename) throws IOException, DocumentException {
        List<String> data = PDF.getAddressData(filename);
        System.out.println(data);
        try
        {
            FileWriter writer = new FileWriter("raport_dane.txt");
            for(int i = 0; i < data.size();i++)
            {
                    writer.write(data.get(i));
            }
            writer.close();
        }
        catch (Exception e)
        {
            System.out.println("Wystąpił błąd podczas tworzenia pliku z raportem danych wysyłkowych");
        }
    }

    public static void createPacketsLog(String filename) throws DocumentException, IOException {
        List<String> a = PDF.getAddressData(filename);
        List<String> wyjatki = PDF.getPacketData(filename,a);
        List<Packet> packets = PDF.obslugawyjatkow(wyjatki,filename);
        try
        {
            FileWriter writer = new FileWriter("raport_pakiety.txt");
            //System.out.println("->"+packets.get(206).city + "<-");
            for(int i = 1; i < packets.size();i++)
            {
                writer.write("Packet:{");
                writer.write("id:"+packets.get(i).id + ",");
                writer.write("startPageNo:"+packets.get(i).startPageNo+ ",");
                writer.write("adresat:"+packets.get(i).adresat+ ",");
                writer.write("address:"+packets.get(i).address+ ",");
                writer.write("postcode:"+packets.get(i).postCode+ ",");
                writer.write("city:"+packets.get(i).city+ ",");
                writer.write("filename:"+packets.get(i).filename+ ",");
                writer.write("isUlotka:"+String.valueOf(packets.get(i).isUlotka) + "}\n");

            }
            writer.close();
        }
        catch (Exception e)
        {
            System.out.println("Wystąpił bład podczas tworzenia pliku z raportem danych wysyłkowych \n" + e);
        }
    }
}
