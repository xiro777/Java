package BarcodeReader;

import org.jnativehook.keyboard.NativeKeyEvent;

import javax.swing.*;
import java.io.File;

public class Globals {
    public static int screenshotBind = NativeKeyEvent.VC_F6;
//    public static int screenshotBind;
    public static int temp;
//    public static int max_number_of_running;
    public static int max_number_of_running = 10;
    public static int exitBind = NativeKeyEvent.VC_F7;
//    public static int exitBind;
    public static boolean cropisRunned = false;
    public static JPanel panel = null;
    public static JFrame f = new JFrame();

    public static String path = BackgroundApp.class.getProtectionDomain().getCodeSource().getLocation().getPath();
    public static File ini_path_without_ini = new File(path);
    public static File configFile = new File(ini_path_without_ini.getParent()+File.separator+"config.ini");
}
