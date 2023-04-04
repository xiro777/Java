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
            System.out.println("Incorrect amount of parameters!");
            System.out.println("Argument 1: Input Path Argument 2: Output Path Argument 3: Splitter Argument 4: Codding Type");
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
        System.out.println("Input path: " +path);
        System.out.println("Output path: " +path1);
        System.out.println("Splitter: '" +spitter +"'");
        System.out.println("Codding type: " +encoding);
        System.out.println();

        File folder = new File(path);
        File folder1 = new File(path1);
        if(!folder.exists())
        {
            throw new FileNotFoundException();
        }
        if(!folder1.exists())
        {
            folder1.mkdir();
        }
        File[] csvFiles = folder.listFiles((dir, name) -> name.endsWith(".csv"));

        int filesCount = 0;
        int lineCountInFile = 0;
        int lineCount = 0;
        for(File csvFile : csvFiles)
        {
            filesCount++;
            System.out.println("File opened: " + csvFile.getName());
            try{
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(csvFile), encoding));

                String xlsFileName = csvFile.getName().replaceAll(".csv"
                        ,".xls");
                File xlsFile = new File(folder1,xlsFileName);
                HSSFWorkbook workbook = new HSSFWorkbook();
                HSSFSheet sheet = workbook.createSheet("Sheet 1");

                String line;
                int rowNumber = 0;

                while((line = reader.readLine())!= null) {
                    lineCountInFile++;

                    String[] columns = line.split(spitter);
                    HSSFRow row = sheet.createRow(rowNumber++);
                    for (int i = 0; i < columns.length; i++)
                    {
                        HSSFCell cell = row.createCell(i);
                        cell.setCellValue(columns[i]);

                    }
                }
                lineCount += lineCountInFile;

                OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(xlsFile),encoding);
                FileOutputStream outputStream = new FileOutputStream(xlsFile);
                workbook.write(outputStream);
                writer.close();
                workbook.close();
                reader.close();

            }
            catch (Exception e)
            {
                System.out.println("Error during creating and saving .xls file: "+e.getMessage());
            }

        }
        System.out.println();
        System.out.println("File count: " + filesCount);
        System.out.println("Read lines: " + lineCount);

    }
}