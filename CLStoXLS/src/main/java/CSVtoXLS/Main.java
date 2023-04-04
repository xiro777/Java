package CSVtoXLS;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.*;

public class Main {
    public static void main(String[] args) throws IOException {
        String path = "";
        String path1 = "";
        String spitter = ";";
        String encoding = "WINDOWS-1250";
        if(args.length<2 || args.length>4)
        {
            System.out.println(args.length);
            System.err.println("Zla ilosc parametrow!");
            System.exit(1);
        }
        else if(args.length==2)
        {
            path = args[0];
            path1 = args[1];
        }
        else if(args.length==3)
        {
            path = args[0];
            path1 = args[1];
            spitter = args[2];
        }
        else if(args.length==4)
        {
            path = args[0];
            path1 = args[1];
            spitter = args[2];
            encoding = args[3];

        }
        System.out.println("Sciezka wejsciowa: " +path);
        System.out.println("Sciezka wyjsciowa: " +path1);
        System.out.println("Typ kodowania: " +encoding);
        System.out.println("Splitter: '" +spitter +"'");
        File folder = new File(path);
        File folder1 = new File(path1);
        if(!folder.exists())
        {
            folder.mkdir();
        }
        if(!folder1.exists())
        {
            folder1.mkdir();
        }
        File[] csvFiles = folder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".csv");
            }
        });

        int ilosc_plikow = 0;
        int ilosc_lini_w_pliku = 0;
        int ilosc_lini = 0;
        for(File csvFile : csvFiles)
        {
            ilosc_plikow++;

            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(csvFile),encoding));

            String xlsFileName = csvFile.getName().replaceAll(".csv"
                    ,".xls");
            File xlsFile = new File(folder1,xlsFileName);
            HSSFWorkbook workbook = new HSSFWorkbook();
            HSSFSheet sheet = workbook.createSheet("Sheet 1");

            String line;
            int rowNumber = 0;

            while((line = reader.readLine())!= null) {
                ilosc_lini_w_pliku++;

                String[] columns = line.split(spitter);
                HSSFRow row = sheet.createRow(rowNumber++);
                for (int i = 0; i < columns.length; i++)
                {
                    HSSFCell cell = row.createCell(i);
                    cell.setCellValue(columns[i]);

                }
            }
            ilosc_lini_w_pliku = ilosc_lini_w_pliku - 1;
            ilosc_lini += ilosc_lini_w_pliku;


            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(xlsFile),"WINDOWS-1250");
            FileOutputStream outputStream = new FileOutputStream(xlsFile);
            workbook.write(outputStream);
            workbook.close();
            writer.close();
            reader.close();
        }
        System.out.println("Ilosc plików: " + ilosc_plikow);
        System.out.println("Ogolna ilosc zczytanych lini: " + ilosc_lini);

    }
}