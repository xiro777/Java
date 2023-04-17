package org.example;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.apache.pdfbox.text.TextPosition;
import org.apache.pdfbox.util.Matrix;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;

public class PDF {
    public static List<Packet> allPackets = new ArrayList<>();
    public static List<PDPage> pagesList = new ArrayList<>();


    //METODA TWORZY TYMCZASOWY PLIK Z DODANYMI PUSTYMI KARTKAMI ZA ADRESOWKA(USUWANY PO UTOWRZENIU NOWEGO PLIKU)
    public static String addBlankPage(String filename) throws IOException, DocumentException {
        PdfReader reader = new PdfReader(filename);
        String name = "PDFwithallblankpages.pdf";
        PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(name));
        int page_count = reader.getNumberOfPages();
        int a = page_count + page_count / 5;
        for (int j = 1; j <= a; j++) {
            if (j % 6 == 1)
                stamper.insertPage(j + 1, reader.getPageSize(1));
        }
        stamper.close();
        reader.close();
        return name;
    }


    //GLOWNA METODA DO DODAWANIA ELEMENTOW DO PDF'a
    public static void processPDF(String filename) throws IOException, DocumentException {
        PdfReader reader = new PdfReader(filename);
        PdfStamper stamper = new PdfStamper(reader, new FileOutputStream("output.pdf"));
        int packetNo;
        int iterator = 0;

        int pageCount = reader.getNumberOfPages();
        for (int i = 1; i <= pageCount; i++) {
            packetNo = i / 6 + 1;
            String code = Barcode.generateBarcodeText(i, packetNo);
            PdfContentByte cb = stamper.getOverContent(i);
            //ZWIEKSZA ITERATOR STRON  NA KTÓRYCH MA SIE ZNALEZC OMR (WYKORZYSTANE DO TWORZENIA TALICY_OMR)
            if (i % 6 == 1 || i % 2 == 1) {
                iterator++;
            }

            //TWORZENIE BIALEGO TLA POD OMR
            Barcode.createRectangle(
                    cb,
                    GLOBALS.x_pixels - Utilities.millimetersToPoints(10f),
                    Utilities.millimetersToPoints(297 - 150),
                    GLOBALS.BARCODE_HEIGHT, Utilities.millimetersToPoints(150f));

            //TWORZENIE BIALEGO TLA W PRAWYM DOLNYM ROGU
            Barcode.createRectangle(
                    cb,
                    GLOBALS.x_pixels - Utilities.millimetersToPoints(10),
                    0,
                    Utilities.millimetersToPoints(10),
                    Utilities.millimetersToPoints((float) 25.4));
            //TWORZENIE BIALEGO TLA POD BARCODE
            Barcode.createRectangle(
                    cb,
                    0,
                    Utilities.millimetersToPoints(297 - 150),
                    GLOBALS.BARCODE_HEIGHT,
                    Utilities.millimetersToPoints(150f));

            //JESLI ITERATOR JEST NA KARTCE Z ADRESEM DODAJE MATRIXA, BARCODE I OMR
            if (i % 6 == 1) {
                Image barcode_img = Barcode.createBarcode(
                        cb,
                        code,
                        packetNo);
                //DODAWANIE TEKSTU BARCODU ROTACJA: rotate == 0 without rotating, rotate == 1 90degree clockwise rotate, rotate == 2 270 degree rotate
                Barcode.addText(
                        cb,
                        code,
                        GLOBALS.FONT_SIZE_DEFAULT,
                        Utilities.millimetersToPoints(4),
                        GLOBALS.y_pixels - Utilities.millimetersToPoints(55f) - barcode_img.getWidth() - 50,
                        1);

                //DODANIE DATY Z CONFIGU.INI DO STRONY ADRESOWEJ
                Barcode.addText(cb, "Data nadania: " + GLOBALS.MATRIX_DATE, 6, GLOBALS.DATE_TEXT_CORD_X, GLOBALS.DATE_TEXT_CORD_Y, 0);

                //TWORZENIE MATRIX KODU NA PODSTAWIE PAKIETU I TWORZENIE IMG MATRIXA
                String matrix_code = Barcode.generateMatrixCode(packetNo);
                Image matrix_img = Barcode.createMatrix(cb, matrix_code);

                //TWORZENIE BIALEGO TLA POD MATRIXEM
                Barcode.createRectangle(cb,
                        GLOBALS.MATRIX_POS_X - Utilities.millimetersToPoints(1.5f),
                        GLOBALS.MATRIX_POX_Y - Utilities.millimetersToPoints(1.5f),
                        matrix_img.getWidth() + Utilities.millimetersToPoints(3),
                        matrix_img.getHeight() + Utilities.millimetersToPoints(3));

                //TWORZENIE TABLICY ZAWIERAJACEJ ELEMENTY DO RYSOWANIA OMR
                int[] omr_arr = Barcode.crateOMRArray(code, iterator, packetNo);
                //TWORZENIE BIALEGO TLA POD OMR
                Barcode.createRectangle(
                        cb,
                        GLOBALS.x_pixels - Utilities.millimetersToPoints(10f),
                        Utilities.millimetersToPoints(297 - 150),
                        GLOBALS.BARCODE_HEIGHT, Utilities.millimetersToPoints(150f));
                //TWORZENIE LINI OMR Z WYKORZYSTANIEM TABLICY
                Barcode.createOMRLine(
                        cb,
                        omr_arr,
                        GLOBALS.x_pixels - Utilities.millimetersToPoints(9),
                        GLOBALS.y_pixels - Utilities.millimetersToPoints(40));


                //DODAJE ZDJĘCIE BARCODU I MATRIXA DO PDF'A
                cb.addImage(barcode_img);
                cb.addImage(matrix_img);
            }
            //JESLI ITERATOR JEST NA KAZDEJ INNEJ KARTCE ROBI TO SAMO TYLKO BEZ DODANIA MATRIXA
            else if (i % 2 == 1) {
                Image barcode_img = Barcode.createBarcode(
                        cb,
                        code,
                        packetNo);

                //TWORZENIE BIALEGO TLA POD BARCODE
                Barcode.createRectangle(
                        cb,
                        0, Utilities.millimetersToPoints(297 - 150),
                        GLOBALS.BARCODE_HEIGHT, Utilities.millimetersToPoints(150f));
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
                        GLOBALS.x_pixels - Utilities.millimetersToPoints(9),
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

    public static void deleteFile(String filename) {
        File file = new File(filename);
        try {
            if (file.exists()) {
                file.delete();
            } else {
                System.out.println("plik nie istnieje");
            }
        } catch (Exception e) {
            System.err.println("An error occurred while attempting to delete the file");
        }
    }


    public static PdfImportedPage getPageFromPdf(String filename, int pageNo) throws IOException, DocumentException {
        PdfReader inDoc = new PdfReader(filename);
        Document doc = new Document(PageSize.A4, 0, 0, 0, 0);
        PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream("pageNo" + pageNo + ".pdf"));
        doc.open();
        PdfImportedPage page = writer.getImportedPage(inDoc, pageNo);
        doc.add(Image.getInstance(page));
        doc.close();
        writer.close();
        inDoc.close();
        return page;
    }

    public static void extractBarcodeFromPdf(File filename) throws IOException {
        File folder = new File("C:/etc/projects/Test/img");
        if (!folder.exists()) {
            folder.mkdir();
        }
        PDDocument doc = PDDocument.load(filename);
        PDFRenderer renderer = new PDFRenderer(doc);
        PDPageTree pages = doc.getDocumentCatalog().getPages();
        for (int i = 0; i < pages.getCount(); i = i + 2) {
            BufferedImage image = renderer.renderImage(i, 1.8f);
            BufferedImage barcode_img = image.getSubimage(3, 842 / 2 - 150, (int) GLOBALS.BARCODE_HEIGHT + 15, (int) GLOBALS.BARCODE_WIDTH * 2);
            File file = new File(folder, "img" + i + ".jpg");
            ImageIO.write(barcode_img, "JPEG", file);
            pagesList.add(pages.get(i));
        }
    }

    public static String getTextFromPage(PDPage page) throws IOException {
        String regionName = "kod_pocztowy";
        String text = "";
        java.awt.Rectangle rect = new java.awt.Rectangle(0, 750, 300, 300);
        try {
            PDFTextStripperByArea stripper = new PDFTextStripperByArea();
            stripper.setSortByPosition(true);
            stripper.addRegion(regionName, rect);
            stripper.extractRegions(page);
            text = stripper.getTextForRegion(regionName);
        } catch (Exception e) {
            System.out.println("Błąd podczas odczytywania tekstu: " + e);
        }
        return text;
    }

    public static String getWholeTextPage(String filename, int PageNo) throws IOException, DocumentException {
        PdfReader reader = new PdfReader(filename);
        PdfReaderContentParser parser = new PdfReaderContentParser(reader);
        String a = PdfTextExtractor.getTextFromPage(reader, PageNo);

        return a;
    }

    public static void createPage() throws IOException {
        PDDocument doc = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        doc.addPage(page);
        PDPageContentStream contentStream = new PDPageContentStream(doc, page);

        contentStream.saveGraphicsState();

        contentStream.setNonStrokingColor(new PDColor(new float[]{0, 0, 0.6f}, PDDeviceRGB.INSTANCE));
        contentStream.transform(Matrix.getRotateInstance(Math.toRadians(90), PDRectangle.A4.getUpperRightX() / 2, PDRectangle.A4.getUpperRightY() / 2));
        contentStream.addRect(0, 0, 200, 100);
        contentStream.fill();

        contentStream.restoreGraphicsState();

        contentStream.beginText();
        String text = "TYTUL";
        PDFont font = PDType1Font.HELVETICA_BOLD;
        int fontSize = 14;
        contentStream.setFont(font, fontSize);
        contentStream.setNonStrokingColor(new PDColor(new float[]{0, 0, 0}, PDDeviceRGB.INSTANCE));
        contentStream.newLineAtOffset((page.getMediaBox().getWidth() - getTextWidth(text, fontSize, font)) / 2, page.getMediaBox().getHeight() - getTextHeight(fontSize, font));
        contentStream.showText(text);

        contentStream.endText();
        contentStream.fill();


        contentStream.close();
        doc.save("test.pdf");
        doc.close();
    }

    public static float getTextWidth(String text, float fontSize, PDFont font) throws IOException {
        return (font.getStringWidth(text) / 1000 * fontSize);
    }

    public static float getTextHeight(float fontSize, PDFont font) {
        return (font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * fontSize);
    }


    public static List<String> getAddressData(String filename) throws IOException, DocumentException {
        PdfReader reader = new PdfReader(filename);
        int countPages = reader.getNumberOfPages();
        List<String> data = new ArrayList<>();
        for (int i = 1; i <= countPages; i = i + 5) {
            String temp_text = getWholeTextPage(filename, i);
            temp_text += "\n" + i + "\n";
            data.add(temp_text);
        }
        return data;

    }

    //METODA POBIERA DANE DO LISTY Z ADRESOWEK ZWRACA NIEPOPRAWNE DANE ADRESOWEK
    public static List<String> getPacketData(String filename, List<String> addressData) throws IOException {
        PdfReader reader = new PdfReader(filename);
        List<String> wyjatki = new ArrayList<>();
        int pageCount = reader.getNumberOfPages();
        allPackets.add(null);

        for (int i = 1; i < pageCount / 5; i++) {
            String temp = addressData.get(i);

            String[] spilted = temp.split("\n");
            if (spilted.length > 4) {
                allPackets.add(i, null);
                wyjatki.add(i + "\n" + temp);
                continue;
            }
            String post = spilted[2].substring(0, spilted[2].indexOf(" "));
            String city = spilted[2].substring(spilted[2].indexOf(" "));
            Packet p = new Packet(i, Integer.parseInt(spilted[3]), spilted[0], spilted[1], post, city, filename, false);
            allPackets.add(i, p);
        }

        return wyjatki;
    }

    //METODA PRZETWARZA I NAPRAWIA WYJATKI ADRESOWEK Z METODY getPacketData (kilka adresowek posiada dodatkowe dane jak 2 razy miejscowość przez co program nie działał)
    public static List<Packet> obslugawyjatkow(List<String> wyjatki, String filename) {
        //System.out.println(wyjatki);
        int commaIndex;
        List<Integer> indexes = new ArrayList<>();
        List<String> temp = new ArrayList<>();
        int k = 0;
        int l = 0;
        for (String str1 : wyjatki) {
            int index = -1;
            while ((index = str1.indexOf('\n', index + 1)) != -1) {
                indexes.add(index);
            }
        }
        for (int i = 0; i < wyjatki.size(); i++) {

            String str = wyjatki.get(i);
            commaIndex = str.indexOf(",");
            temp.add(str.substring(0, indexes.get(k)));
            temp.add(str.substring(indexes.get(k) + 1, indexes.get(k + 1)));
            if (str.charAt(commaIndex + 2) == '\n')
                temp.add(str.substring(indexes.get(k + 2) + 1, indexes.get(k + 3)));
                //str.substring(commaIndex + 3,indexes.get(k+2)) +
            else {
                temp.add(str.substring(commaIndex + 2, indexes.get(k + 2)) + str.substring(indexes.get(k + 2) + 1, indexes.get(k + 3)));
            }

            temp.add(str.substring(indexes.get(k + 3), indexes.get(k + 4)));
            temp.add(str.substring(indexes.get(k + 4) + 1, indexes.get(k + 5)));
            String code_city = str.substring(indexes.get(k + 3) + 1, indexes.get(k + 4));
            k = k + 6;
            int a = code_city.indexOf(' ');
            Packet p = new Packet(Integer.parseInt(temp.get(l)), Integer.parseInt(temp.get(l + 4)), temp.get(l + 1), temp.get(l + 2), code_city.substring(0, code_city.indexOf(' ')), code_city.substring(a + 1), filename, false);
            allPackets.set(Integer.parseInt(temp.get(l)), p);
            l = l + 5;
        }
        return allPackets;
    }

    public static void getSomePages() throws IOException {
        String folder_path = "C:/etc/projects/Test/in";
        File folder = new File(folder_path);
        String[] filesNames = folder.list();
        System.out.println(filesNames.length);

        for (int j = 0; j < filesNames.length; j++) {
            System.out.println("Starting processing file: " + folder_path + "/" + filesNames[j]);
            File file = new File(folder_path + "/" + filesNames[j]);
            PDDocument doc = PDDocument.load(file);
            PDDocument new_doc = new PDDocument();
            PDPageTree pages = doc.getPages();
            for (int i = 0; i < doc.getNumberOfPages(); i++) {
                PDPage page = pages.get(i);
                new_doc.addPage(page);
            }
            new_doc.save("C:/etc/projects/Test/in/" + filesNames[j]);
            new_doc.close();
            doc.close();
        }
        System.out.println("End of processing files from folder:" + folder_path);
        PDF.RunPDFOptimalizer();

    }


    public static List<BufferedImage> getMatrixImage(File pdf) {
        List<BufferedImage> list1 = new ArrayList<>();
        try {
            PDDocument doc = PDDocument.load(pdf);
            PDPageTree list = doc.getPages();
            int i = 0;
            for (PDPage page : list) {
                if (list.indexOf(page) == i) {
                    PDResources pdResources = page.getResources();
                    System.out.println(pdResources);
                    for (COSName c : pdResources.getXObjectNames()) {
                        PDXObject o = pdResources.getXObject(c);
                        if (o instanceof PDImageXObject) {
                            PDImageXObject image = (PDImageXObject) o;
                            BufferedImage im = image.getImage();
                            list1.add(im);
                        }
                    }
                    i = i + 6;
                }
            }
            doc.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return list1;
    }

    public static String readBarcodeAndQRCodeFromImage(String filename)
    {
        File file = new File(filename);
        try {
            BufferedImage image = ImageIO.read(file);

            int[] pixels = image.getRGB(0,0,image.getWidth(),image.getHeight(),null,0,image.getWidth());
            RGBLuminanceSource source = new RGBLuminanceSource(image.getWidth(),image.getHeight(),pixels);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

            MultiFormatReader reader = new MultiFormatReader();
            Result result = reader.decodeWithState(bitmap);

            return result.getText();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    public static void RunPDFOptimalizer() {
        try {
            ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "cd \"C:/etc/libs\" && java -jar PdfOptimizer.jar C:/etc/projects/Test/in C:/etc/projects/Test/out");
            builder.redirectErrorStream(true);
            Process p = builder.start();
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while (true) {
                line = r.readLine();
                if (line == null) {
                    break;
                }
                System.out.println(line);
            }
        } catch (Exception e) {
            System.out.println("Blad podczas wykonywania komendy do uruchomienia PdfOptymalizer.jar: " + e.getMessage());
        }
        System.out.println("Utowrzono zoptymalizowany plik");
    }


}
