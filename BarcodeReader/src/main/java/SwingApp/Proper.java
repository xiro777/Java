package SwingApp;

import java.io.*;
import java.net.URISyntaxException;
import java.util.Properties;

public class Proper extends Properties {
    public static Properties prop = null;
    public static InputStream inputStream;



    public Proper() throws IOException {
        inputStream = new FileInputStream(Globals.temp_path.getParent()+File.separator+"config.ini");
    }

    public boolean init() {
        prop = new Properties();
        try {
            prop.load(inputStream);
            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getProper(String propertyName) {
        String temp;
        temp = prop.getProperty(propertyName);
        System.out.println(propertyName + ": " + temp);
        if (temp == null) {
            System.err.println("Couldn't read parameter " + propertyName + " from .ini file");
        }
        return temp;
    }

    public static boolean loadProperties() {
        try {
            Globals.screenshotBind = Integer.parseInt(getProper("SCREENSHOT_BIND"));
            Globals.exitBind = Integer.parseInt(getProper("EXIT_BIND"));
            Globals.max_number_of_running = Integer.parseInt(getProper("MAX_TIMES_RUNNED_APP"));
            return true;
        } catch (Exception e) {
            System.out.println("Exception during loading files from .ini file");
            System.out.println(e.getMessage());
            return false;
        }
    }

    public static void savePropertoIni(String ini_text, int keyBindValue) throws IOException, URISyntaxException {
        BufferedReader reader = new BufferedReader(new FileReader(Globals.configFile));
        System.out.println(Globals.configFile);
        StringBuilder sb = new StringBuilder();
        String line;
        // Read through the input stream, looking for the line to overwrite
        while ((line = reader.readLine()) != null) {
            if (line.startsWith(ini_text)) {
                // Replace the line with the new line
                sb.append(ini_text + "=" + keyBindValue).append(System.lineSeparator());
            } else {
                // Keep the existing line
                sb.append(line).append(System.lineSeparator());
            }
        }
        BufferedWriter writer = new BufferedWriter(new FileWriter(Globals.configFile));
        writer.write(sb.toString());
        writer.close();

    }


}