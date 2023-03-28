package org.example;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import org.apache.pdfbox.PDFReader;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.pdfbox.util.PDFTextStripperByArea;

import javax.print.Doc;
import java.io.*;
import java.text.SimpleDateFormat;
import java.time.chrono.MinguoChronology;
import java.util.*;
import java.util.List;

public class PDF {
    public static List<Packet> allPackets = new ArrayList<>();

    //METODA TWORZY TYMCZASOWY PLIK Z DODANYMI PUSTYMI KARTKAMI ZA ADRESOWKA(USUWANY PO UTOWRZENIU NOWEGO PLIKU)
    public static String addBlankPage(String filename) throws IOException, DocumentException {
        PdfReader reader = new PdfReader(filename);
        String name = "PDFwithallblankpages.pdf";
        PdfStamper stamper = new PdfStamper(reader,new FileOutputStream(name));
        int page_count = reader.getNumberOfPages();
        int a = page_count + page_count/5;
        for ( int j = 1 ; j <= a ;j++)
        {
            if(j%6==1)
                stamper.insertPage(j+1,reader.getPageSize(1));
        }
        stamper.close();
        reader.close();
        return name;
    }

    //GLOWNA METODA DO DODAWANIA ELEMENTOW DO PDF'a
    public static void processPDF(String filename) throws IOException, DocumentException {
        PdfReader reader = new PdfReader(filename);
        PdfStamper stamper = new PdfStamper(reader,new FileOutputStream("output.pdf"));
        int packetNo;
        int iterator = 0;

        int pageCount = reader.getNumberOfPages();
        for(int i = 1; i <= pageCount;i++)
        {
            packetNo = i/6 + 1;
            String code = Barcode.generateBarcodeText(i, packetNo);
            PdfContentByte cb = stamper.getOverContent(i);
            //ZWIEKSZA ITERATOR STRON  NA KTÓRYCH MA SIE ZNALEZC OMR (WYKORZYSTANE DO TWORZENIA TALICY_OMR)
            if(i%6==1 || i%2==1)
            {
                iterator++;
            }

            //TWORZENIE BIALEGO TLA POD OMR
            Barcode.createRectangle(
                    cb,
                    GLOBALS.x_pixels - Utilities.millimetersToPoints(10f),
                    Utilities.millimetersToPoints(297-150),
                    GLOBALS.BARCODE_HEIGHT,Utilities.millimetersToPoints(150f));

            //TWORZENIE BIALEGO TLA W PRAWYM DOLNYM ROGU
            Barcode.createRectangle(
                    cb,
                    GLOBALS.x_pixels-Utilities.millimetersToPoints(10),
                    0,
                    Utilities.millimetersToPoints(10),
                    Utilities.millimetersToPoints((float)25.4));
            //TWORZENIE BIALEGO TLA POD BARCODE
            Barcode.createRectangle(
                    cb,
                    0,
                    Utilities.millimetersToPoints(297-150),
                    GLOBALS.BARCODE_HEIGHT,
                    Utilities.millimetersToPoints(150f));

            //JESLI ITERATOR JEST NA KARTCE Z ADRESEM DODAJE MATRIXA, BARCODE I OMR
            if(i%6==1)
            {
                Image barcode_img = Barcode.createBarcode(
                        cb,
                        code);
                //DODAWANIE TEKSTU BARCODU ROTACJA: rotate == 0 without rotating, rotate == 1 90degree clockwise rotate, rotate == 2 270 degree rotate
                Barcode.addText(
                        cb,
                        code,
                        GLOBALS.FONT_SIZE_DEFAULT,
                        Utilities.millimetersToPoints(4),
                        GLOBALS.y_pixels - Utilities.millimetersToPoints(55f) - barcode_img.getWidth() - 50 ,
                        1);

                //DODANIE DATY Z CONFIGU.INI DO STRONY ADRESOWEJ
                Barcode.addText(cb,"Data nadania: " + GLOBALS.MATRIX_DATE,6,GLOBALS.DATE_TEXT_CORD_X,GLOBALS.DATE_TEXT_CORD_Y,0);

                //TWORZENIE MATRIX KODU NA PODSTAWIE PAKIETU I TWORZENIE IMG MATRIXA
                String matrix_code = Barcode.generateMatrixCode(packetNo);
                Image matrix_img = Barcode.createMatrix(cb, matrix_code);

                //TWORZENIE BIALEGO TLA POD MATRIXEM
                Barcode.createRectangle(cb,
                        GLOBALS.MATRIX_POS_X-Utilities.millimetersToPoints(1.5f),
                        GLOBALS.MATRIX_POX_Y-Utilities.millimetersToPoints(1.5f),
                        matrix_img.getWidth()+Utilities.millimetersToPoints(3),
                        matrix_img.getHeight()+Utilities.millimetersToPoints(3));

                //TWORZENIE TABLICY ZAWIERAJACEJ ELEMENTY DO RYSOWANIA OMR
                int[] omr_arr = Barcode.crateOMRArray(code, iterator, packetNo);
                //TWORZENIE BIALEGO TLA POD OMR
                Barcode.createRectangle(
                        cb,
                        GLOBALS.x_pixels - Utilities.millimetersToPoints(10f),
                        Utilities.millimetersToPoints(297-150),
                        GLOBALS.BARCODE_HEIGHT,Utilities.millimetersToPoints(150f));
                //TWORZENIE LINI OMR Z WYKORZYSTANIEM TABLICY
                Barcode.createOMRLine(
                        cb,
                        omr_arr,
                        GLOBALS.x_pixels-Utilities.millimetersToPoints(9),
                        GLOBALS.y_pixels - Utilities.millimetersToPoints(40));



                //DODAJE ZDJĘCIE BARCODU I MATRIXA DO PDF'A
                cb.addImage(barcode_img);
                cb.addImage(matrix_img);
            }
            //JESLI ITERATOR JEST NA KAZDEJ INNEJ KARTCE ROBI TO SAMO TYLKO BEZ DODANIA MATRIXA
            else if(i%2==1)
            {
                Image barcode_img = Barcode.createBarcode(
                        cb,
                        code);
                //TWORZENIE BIALEGO TLA POD BARCODE
                Barcode.createRectangle(
                        cb,
                        0,Utilities.millimetersToPoints(297-150),
                        GLOBALS.BARCODE_HEIGHT,Utilities.millimetersToPoints(150f));
                //DODAWANIE TEKSTU POD BARCODE rotate == 0 without rotating, rotate == 1 90degree clockwise rotate, rotate == 2 270 degree rotate
                Barcode.addText(
                        cb,
                        code,
                        GLOBALS.FONT_SIZE_DEFAULT,
                        Utilities.millimetersToPoints(4),
                        GLOBALS.y_pixels - Utilities.millimetersToPoints(55f) - barcode_img.getWidth() - 50,
                        1);


                //create OMR
                int[] omr_arr = Barcode.crateOMRArray(code, iterator, packetNo);
                Barcode.createOMRLine(
                        cb,
                        omr_arr,
                        GLOBALS.x_pixels-Utilities.millimetersToPoints(9),
                        GLOBALS.y_pixels - Utilities.millimetersToPoints(40));


                //TWORZENIE BIALEGO TLA POD BIALY DOLNY ROG
//                Barcode.createRectangle(
//                        cb,
//                        GLOBALS.x_pixels-Utilities.millimetersToPoints(10),
//                        0,Utilities.millimetersToPoints(10),
//                        Utilities.millimetersToPoints((float)25.4));

                //adding images to pdf
                cb.addImage(barcode_img);
            }
            Barcode.drawLine(cb);
        }


        stamper.close();
        reader.close();
        deleteFile(filename);
    }

    public static void deleteFile(String filename)
    {
        File file = new File(filename);
        try
        {
            if(file.exists())
            {
                file.delete();
            }
            else {
                System.out.println("plik nie istnieje");
            }
        }catch (Exception e)
        {
            System.err.println("An error occurred while attempting to delete the file");
        }
    }
    public static PdfImportedPage getPageFromPdf(String filename, int pageNo) throws IOException, DocumentException {
        PdfReader inDoc = new PdfReader(filename);
        Document doc = new Document(PageSize.A4,0,0,0,0);
        PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream("pageNo"+pageNo+ ".pdf"));
        doc.open();
        PdfImportedPage page = writer.getImportedPage(inDoc,pageNo);
        doc.add(Image.getInstance(page));
        doc.close();
        writer.close();
        inDoc.close();
        return page;
    }
    public static PDPage getSinglePageFromPdf(String filename, int pageNo) throws IOException {
        PDDocument doc = PDDocument.load(filename);
        List<PDPage> allPages = doc.getDocumentCatalog().getAllPages();
        PDPage page = allPages.get(pageNo);
        return page;
    }

    public static void saveSinglePageAsPdf(PDPage page,String new_filename) throws IOException, COSVisitorException {
        PDDocument doc1 = new PDDocument();
        doc1.addPage(page);
        doc1.save(new_filename);
    }
    public static String getTextFromPage(PDPage page) throws IOException, COSVisitorException {
        String regionName = "kod_pocztowy";
        String text = "";
        java.awt.Rectangle rect = new java.awt.Rectangle(0,750,300,300);
        try
        {
            PDFTextStripperByArea stripper = new PDFTextStripperByArea();
            stripper.setSortByPosition(true);
            stripper.addRegion(regionName,rect);
            stripper.extractRegions(page);
            text = stripper.getTextForRegion(regionName);
        }
        catch (Exception e)
        {
            System.out.println("Błąd podczas odczytywania tekstu: " + e);
        }
        return text;
    }

    public static String getWholeTextPage(String filename, int PageNo) throws IOException, DocumentException {
        PdfReader reader = new PdfReader(filename);
        PdfReaderContentParser parser = new PdfReaderContentParser(reader);
        String a = PdfTextExtractor.getTextFromPage(reader,PageNo);

        return a;
    }


    public static List<String> getAddressData(String filename) throws IOException, DocumentException {
        PdfReader reader = new PdfReader(filename);
        int countPages = reader.getNumberOfPages();
        List<String> data = new ArrayList<>();
        for(int i = 1 ; i <= countPages; i = i + 5)
        {
            String temp_text = getWholeTextPage(filename,i);
            temp_text += "\n" + i + "\n";
            data.add(temp_text);
        }
        return data;

    }
    //METODA POBIERA DANE DO LISTY Z ADRESOWEK ZWRACA NIEPOPRAWNE DANE ADRESOWEK
    public static List<String> getPacketData(String filename,List<String> addressData) throws IOException {
        PdfReader reader = new PdfReader(filename);
        List<String> wyjatki = new ArrayList<>();
        int pageCount = reader.getNumberOfPages();
        allPackets.add(null);

        for (int i = 1 ; i < pageCount/5; i++)
        {
            String temp = addressData.get(i);

            String[] spilted = temp.split("\n");
            if(spilted.length>4)
            {
                allPackets.add(i,null);
                wyjatki.add( i + "\n" +temp);
                continue;
            }
            String post=spilted[2].substring(0,spilted[2].indexOf(" "));
            String city=spilted[2].substring(spilted[2].indexOf(" "));
            Packet p = new Packet(i,Integer.parseInt(spilted[3]),spilted[0],spilted[1],post,city,filename,false);
            allPackets.add(i,p);
        }

        return wyjatki;
    }

    //METODA PRZETWARZA I NAPRAWIA WYJATKI ADRESOWEK Z METODY getPacketData (kilka adresowek posiada dodatkowe dane jak 2 razy miejscowość przez co program nie działał)
    public static List<Packet> obslugawyjatkow(List<String> wyjatki,String filename)
    {
        //System.out.println(wyjatki);
        int commaIndex;
        List<Integer> indexes = new ArrayList<>();
        List<String> temp = new ArrayList<>();
        int k = 0;
        int l = 0;
        for(String str1:wyjatki)
        {
            int index = -1;
            while((index = str1.indexOf('\n',index+1))!=-1){
                indexes.add(index);
            }
        }
        for(int i = 0 ; i < wyjatki.size();i++)
        {

            String str = wyjatki.get(i);
            commaIndex = str.indexOf(",");
            temp.add(str.substring(0, indexes.get(k)));
            temp.add(str.substring(indexes.get(k)+1 , indexes.get(k+1)));
            if(str.charAt(commaIndex+2)=='\n')
                temp.add(str.substring(indexes.get(k+2)+1,indexes.get(k+3)));
                //str.substring(commaIndex + 3,indexes.get(k+2)) +
            else
            {
                temp.add(str.substring(commaIndex + 2, indexes.get(k+2)) + str.substring(indexes.get(k+2)+1, indexes.get(k+3)));
            }

            temp.add(str.substring(indexes.get(k+3), indexes.get(k+4)));
            temp.add(str.substring(indexes.get(k+4)+1, indexes.get(k+5)));
            String code_city = str.substring(indexes.get(k+3)+1, indexes.get(k+4));
            k = k + 6;
            int a = code_city.indexOf(' ');
            Packet p = new Packet(Integer.parseInt(temp.get(l)),Integer.parseInt(temp.get(l+4)),temp.get(l+1),temp.get(l+2),code_city.substring(0,code_city.indexOf(' ')),code_city.substring(a+1),filename,false);
            allPackets.set(Integer.parseInt(temp.get(l)),p);
            l = l + 5;
        }
        return allPackets;
    }




}
