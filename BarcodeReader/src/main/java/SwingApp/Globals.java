package SwingApp;

import org.jnativehook.keyboard.NativeKeyEvent;

import java.io.File;

public class Globals {
//    public static int screenshotBind = NativeKeyEvent.VC_F6;
    public static int screenshotBind;
    public static int temp;
    public static int max_number_of_running;
//    public static int exitBind = NativeKeyEvent.VC_F7;
    public static int exitBind;
    public static boolean cropisRunned = false;

    public static String path = BackgroundApp.class.getProtectionDomain().getCodeSource().getLocation().getPath();
    public static File temp_path = new File(path);
    public static File configFile = new File(temp_path.getParent()+File.separator+"config.ini");
}
