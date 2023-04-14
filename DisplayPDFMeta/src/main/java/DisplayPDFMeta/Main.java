package DisplayPDFMeta;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Main {
    //ARGS[0] default folder with files
    public static void main(String[] args) throws Exception {
        String path = "";
        File input_folder = null;
        ArrayList<String> pdfNames = new ArrayList<>();
        ArrayList<File> Files = new ArrayList<>();

        if (args[0].endsWith(".pdf") || args.length > 1) {
            int args_length = args.length;
            for (int i = 0; i < args_length; i++) {
                pdfNames.add(args[i]);
                Files.add(new File(args[i]));
            }
        } else if (!args[0].endsWith(".pdf") || args.length <= 1) {
            path = args[0];
            input_folder = new File(path);
            if (!input_folder.exists()) {
                throw new Exception("Incorrect path or folder doesn't exists!");
            }
            File[] temp = input_folder.listFiles((dir, name) -> name.endsWith(".pdf"));
            for (int i = 0; i < temp.length; i++) {
                Files.add(temp[i]);
            }
        }
        int filesCount = 0;
        int[] pages = new int[Files.size()];
        for (File file : Files) {
            filesCount++;
            System.out.println("File opened: " + file.getName());

            try {
                PDDocument doc = PDDocument.load(file);
                int page_numbers = doc.getNumberOfPages();
                System.out.println("PagesNo: "+page_numbers);
                Map<Rectangle2D, Integer> map = new HashMap<Rectangle2D, Integer>();
                for (int i = 0; i < page_numbers; i++) {
                    PDPage page = doc.getPage(i);
                    Rectangle2D rect = new Rectangle2D.Float(0f,0f, page.getMediaBox().getWidth(), page.getMediaBox().getHeight());
                    if (map.containsKey(rect)) {
                        map.put(rect, map.get(rect) + 1);
                    } else {
                        map.put(rect, 1);
                    }
                }
                int temp = 0;
                for (Map.Entry<Rectangle2D, Integer> entry : map.entrySet()) {

                    System.out.println("   ["+entry.getKey().getX() +","+entry.getKey().getY()+"," +entry.getKey().getWidth() +","+entry.getKey().getHeight() + "] occurs: " + entry.getValue());

                }
                System.out.println();
                doc.close();

            } catch (Exception e) {
                System.out.println("Error during opening file: " + file.getName());
                System.err.println(e.getMessage());
            }
        }
        System.out.println("All filesNo: " + filesCount);


    }
}