package BarcodeReader;

/************************************************************************************************************
 *   Project Name: BarcodeReader
 *   Version:v1.0
 *   Description[PL]: Projekt to aplikacja działająca w tle, wyświetlająca się w Tray
 *      (prawy dolny gór małe ikonki). Główną funkcją programu jest możliwość robienia screenshotu ekranu
 *      i wycinanie z niego obszaru do zczytania. Funckja rozczytuje z obrazu wartości Matrixów/Barcodów.
 *      Program posiada możliwość przypisania skrótów klawiszowych do uruchamiania funkcji screenshot i
 *      wyłączenia aplikacji(domyślnie F6-robienie screenshota, F7-wyjście z programu). Dodatkowo w menu Tray
 *      są przyciski do sprawdzania skrótów klawiszowych, robienia screena i wyłączenia programu.
 *      Skróty klawiszowe zapisywane są do pliku config.ini.
 *
 *   [EN]Design of a background application displayed in a Tray
 *      (bottom right top small icons). the ability to create screenshots of the screen
 *      and cutting from this area to be read. The function reads Matrix/Barcode values from the image.
 *      The program has the ability to assign keyboard shortcuts to run the screenshot function and
 *      location of the application (default F6-taking a screenshot, F7-exiting the program).
 *      Additionally in the Tray menu are carefully to check the hotkeys,
 *      turn on the screen and run the program. Short keys are written to the config.ini file.
 *   Created by: Kacper Morawski
 ************************************************************************************************************/

/*
 * Copyright 2009 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.*;

import com.google.zxing.*;
import com.google.zxing.common.HybridBinarizer;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.w3c.dom.css.Rect;

public class BackgroundApp implements NativeKeyListener {
    private static final int SCREEN_WIDTH = Toolkit.getDefaultToolkit().getScreenSize().width;
    private static final int SCREEN_HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().height;
    public static BufferedImage screenshot = null;
    public static Robot robot = null;
    public static int[] pixels;
    public static BufferedImage croppedImage = null;
    public static BufferedImage scaledImg = null;
    public static BufferedImage rotatedImage = null;
    public static JDialog dialog = null;
    public static double[] scale = {0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1, 2, 3, 4, 5, 6};
    public static Graphics2D g = null;
    public static AffineTransform transform = null;
    public static JTextArea textArea = null;
    public static StringSelection selection = null;
    public static Clipboard clipboard = null;
    public static MultiFormatReader reader = null;
    public static RGBLuminanceSource source = null;
    public static BinaryBitmap bitmap = null;
    public static Result result = null;
    public static CropFrame cropFrame = null;
    public static JButton copyButton = null;
    public static JButton screenshotButton = null;
    public static JButton exitProgramButton = null;
    public static int iterator = 0;
    public static String programName;

    public BackgroundApp() {
        try {
            Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
            logger.setLevel(Level.OFF);
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(this);
        } catch (NativeHookException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        //Loading properties from config.ini
        //To debug code comment initing config.ini(also in rebind function) and Globals values
        Proper p1 = new Proper();
        if (p1.init()) {
            if (!p1.loadProperties()) {
                System.exit(1);
            }
        }
        File folder = new File(Globals.ini_path_without_ini.getParent());
        if(folder.exists() && folder.isDirectory())
        {
            File[] files = folder.listFiles();
            for(File file: files)
            {
                if(file.getName().startsWith("BarcodeReader")==true)
                {
                    programName = file.getName();
                }
            }
        }
        //until there
        //Run application with tray
        if (SystemTray.isSupported()) {
            new BackgroundApp();
            SystemTray tray = SystemTray.getSystemTray();
            Image icon = Toolkit.getDefaultToolkit().getImage(BackgroundApp.class.getResource("/icon.jpg"));

            TrayIcon trayIcon = new TrayIcon(icon, "BarcodeReader");
            trayIcon.setImageAutoSize(true);

            try {
                tray.add(trayIcon);
            } catch (AWTException e) {
                throw new RuntimeException(e);
            }
            //Adding list to tray menu
            PopupMenu popup = new PopupMenu();
            MenuItem rebindKeys = new MenuItem("Rebind");
            MenuItem startMethod = new MenuItem("Scan");
            MenuItem info = new MenuItem("Information");
            MenuItem exitItem = new MenuItem("Exit");
            popup.add(startMethod);
            popup.add(info);
            popup.add(rebindKeys);
            popup.add(exitItem);


            //Handling tray menu buttons
            startMethod.addActionListener(e -> {
                NativeKeyEvent event = new NativeKeyEvent(NativeKeyEvent.NATIVE_KEY_PRESSED, 0, Globals.screenshotBind, Globals.screenshotBind, NativeKeyEvent.CHAR_UNDEFINED);
                GlobalScreen.postNativeEvent(event);
            });


            info.addActionListener(e -> JOptionPane.showMessageDialog(null, "Screenshot keybind: " + NativeKeyEvent.getKeyText(Globals.screenshotBind) + "\nExit program keybind: " + NativeKeyEvent.getKeyText(Globals.exitBind) + "\nAplikacja dziala tylko na glownym monitorze(przed uruchomieniem skanowania prosze kliknac na Glowny monitor i dopiero rozpoczac funkcje)\nVersion:v1.0\n\nCreated by: Kacper Morawski"));

            rebindKeys.addActionListener(e -> {
                //Create frame with 2 buttons
                Globals.f = new JFrame("Change keybinds");
                Globals.f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

                screenshotButton = new JButton("Screenshot Keybind");
                exitProgramButton = new JButton("Exit Keybind");

                screenshotButton.setBounds(25, 50, 200, 50);
                exitProgramButton.setBounds(275, 50, 200, 50);

                //Handiling rebind screenshot button
                screenshotButton.addActionListener(e12 -> {
                    dialog = new JDialog(Globals.f, "Pressed key will be assigned to screenshot", false);
                    dialog.setLocationRelativeTo(null);
                    dialog.setSize(200, 100);

                    dialog.addKeyListener(new KeyAdapter() {
                        @Override
                        public void keyPressed(KeyEvent e12) {
                            //Assigne pressed button to screenshot button
                            Globals.screenshotBind = Globals.temp;
                            //to debug comment from here
                            try {
                                //Saving new keybind in config.ini
                                p1.savePropertoIni("SCREENSHOT_BIND", Globals.screenshotBind);
                            } catch (IOException ex) {
                                throw new RuntimeException(ex);
                            } catch (URISyntaxException ex) {
                                throw new RuntimeException(ex);
                            }
                            //until there
                            dialog.dispose();
                            Globals.f.dispose();
                            Globals.f = null;
                            dialog = null;
                            copyButton = null;
                            screenshotButton = null;
                            exitProgramButton = null;
                        }
                    });

                    dialog.setVisible(true);
                });

                exitProgramButton.addActionListener(e1 -> {
                    dialog = new JDialog(Globals.f, "Pressed key will be assigned to exit", false);
                    dialog.setLocationRelativeTo(null);
                    dialog.setSize(200, 100);

                    dialog.addKeyListener(new KeyAdapter() {
                        @Override
                        public void keyPressed(KeyEvent e1) {
                            Globals.exitBind = Globals.temp;
                            //to debug comment from here
                            try {
                                p1.savePropertoIni("EXIT_BIND", Globals.exitBind);
                            } catch (IOException ex) {
                                throw new RuntimeException(ex);
                            } catch (URISyntaxException ex) {
                                throw new RuntimeException(ex);
                            }
                            //to there
                            dialog.dispose();
                            Globals.f.dispose();
                            //f = null;
                            dialog = null;
                            copyButton = null;
                            screenshotButton = null;
                            exitProgramButton = null;

                        }
                    });


                    dialog.setVisible(true);

                });

                Globals.f.add(screenshotButton);
                Globals.f.add(exitProgramButton);
                Globals.f.setLayout(null);
                Globals.f.setSize(515, 200);
                Globals.f.setLocationRelativeTo(null);
                Globals.f.setVisible(true);

            });


            exitItem.addActionListener(e -> {
                // Close the application
                System.exit(0);
            });
            trayIcon.setPopupMenu(popup);
        }
    }   //end main

    //Main Method to capture screenshot
    private void captureScreenshot() {
        try {
            //Rerun application after too many use(because program was not restoring allocated RAM after every use)
            if (iterator > Globals.max_number_of_running) {
                Runtime.getRuntime().exec("java -jar "+programName);
                System.exit(0);
            }
            iterator++;
            Globals.cropisRunned = true;

            robot = new Robot();
            screenshot = robot.createScreenCapture(new Rectangle(SCREEN_WIDTH, SCREEN_HEIGHT));
            if (Globals.f == null)
                Globals.f = new JFrame();
            cropImage(screenshot);
            screenshot = null;
            robot = null;
            Globals.cropisRunned = false;
            System.gc();
        } catch (AWTException | IOException ex) {
            Globals.cropisRunned = false;
            ex.printStackTrace();
        }
    }

    //Method which display JFrame and crop images
    private boolean cropImage(BufferedImage screenshot) {
        String output;
        croppedImage = null;
        cropFrame = null;
        cropFrame = new CropFrame(screenshot);
        try {
            //this while allow to see screenshot as long as u not crop image from screenshot
            while (cropFrame.visible) {
                Thread.sleep(100);
            }
            //after cropping image isCropped is true
            if (cropFrame.isCropped()) {
                //Display popup when u don't crop any rectangle
                if (cropFrame.w == 0 || cropFrame.h == 0) {
                    JOptionPane.showMessageDialog(null, "Image wasn't cropped properly from screenshot!");
                    return false;
                }
                //crop subimage from screenshot which will be scanned
                croppedImage = screenshot.getSubimage(cropFrame.x, cropFrame.y, cropFrame.w, cropFrame.h);
                //creating popup with selectable text area and copy button which contains Barcode/Matrix text
                output = readBarcodeAndQRCodeFromScreenShot(croppedImage);
                croppedImage = null;
                cropFrame = null;
                //rerun application when barcode/matrix value is not found(also was occupy RAM depends on size of cropped image)
                if (output == null) {
                    Runtime.getRuntime().exec("java -jar "+programName);
                    System.exit(0);
                    return false;
                }
                //Create popup with selectable text copy button etc
                Globals.panel = new JPanel();
                textArea = new JTextArea();
                textArea.setEditable(false);
                textArea.setText(output);
                copyButton = new JButton("Copy");
                copyButton.addActionListener(e -> {
                    selection = new StringSelection(output);
                    clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clipboard.setContents(selection, selection);
                });
                Globals.panel.add(textArea);
                Globals.panel.add(copyButton);
                JOptionPane.showOptionDialog(null, Globals.panel, "Message", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);
                Globals.cropisRunned = false;
                Globals.panel = null;
                selection = null;
                clipboard = null;
                textArea = null;
                copyButton = null;
                return true;
            }
        } catch (InterruptedException ex) {
            Globals.cropisRunned = false;
            ex.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        croppedImage = null;
        cropFrame = null;
        Globals.panel = null;
        selection = null;
        clipboard = null;
        textArea = null;
        copyButton = null;
        return false;
    }


    //Method to recognize barcode and matrix from cropped image
    private String readBarcodeAndQRCodeFromScreenShot(BufferedImage image) {

        //added scale to change resolution of image(help with finding values hidden underneath)

        for (double v : scale) {
            //scale image
            scaledImg = scaleImage(image, v);
            try {
                //main method for decoding text from image
                pixels = scaledImg.getRGB(0, 0, scaledImg.getWidth(), scaledImg.getHeight(), null, 0, scaledImg.getWidth());
                if (pixels == null)
                    break;
                source = new RGBLuminanceSource(scaledImg.getWidth(), scaledImg.getHeight(), pixels);
                bitmap = new BinaryBitmap(new HybridBinarizer(source));
                reader = new MultiFormatReader();
                result = reader.decodeWithState(bitmap);

                return result.getText();
            } catch (NotFoundException e) {
                System.out.println(e.getMessage());
            }
            //rotating image by 90degree because Zxing has problem with reading barcode which was rotated
            try {
                rotatedImage = rotateImage(scaledImg);
                pixels = rotatedImage.getRGB(0, 0, rotatedImage.getWidth(), rotatedImage.getHeight(), null, 0, rotatedImage.getWidth());
                if (pixels == null)
                    break;
                source = new RGBLuminanceSource(rotatedImage.getWidth(), rotatedImage.getHeight(), pixels);
                bitmap = new BinaryBitmap(new HybridBinarizer(source));
                reader = new MultiFormatReader();
                result = reader.decodeWithState(bitmap);
                return result.getText();
            } catch (IOException | NotFoundException e) {
                System.out.println(e.getMessage());
            }
            scaledImg.flush();
            rotatedImage.flush();
            pixels = null;
            source = null;
            bitmap = null;
            reader.reset();
            result = null;
            System.gc();
        }

        Globals.cropisRunned = false;
        JOptionPane.showMessageDialog(null, "Matrix/Barcode not found (try to scale matrix)");
        return null;
    }

    //Method scale image for better recognizing barcode/matrix(this method help to decode image by changing size)
    public BufferedImage scaleImage(BufferedImage image, double scale) {
        try {
            scaledImg = new BufferedImage((int) (image.getWidth() * scale), (int) (image.getHeight() * scale), BufferedImage.TYPE_INT_RGB);
            g = scaledImg.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(image, 0, 0, (int) (image.getWidth() * scale), (int) (image.getHeight() * scale), null);
            g.dispose();
            return scaledImg;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        JOptionPane.showMessageDialog(null, "Cropping image function is in progress (press mouse anywhere and start Scan function again)");
        scaledImg = null;
        g = null;
        return null;
    }

    //Method rotate image because barcode is only readable horizontally(not vertically)
    public static BufferedImage rotateImage(BufferedImage image) throws IOException {
        try {
            rotatedImage = new BufferedImage(image.getHeight(), image.getWidth(), BufferedImage.TYPE_INT_RGB);
            g = rotatedImage.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            transform = new AffineTransform();
            transform.translate((float) (image.getHeight()) / 2, (float) (image.getWidth()) / 2);
            transform.rotate(Math.toRadians(90));
            transform.translate(-(float) (image.getWidth()) / 2, -(float) (image.getHeight()) / 2);
            g.drawImage(image, transform, null);
            g.dispose();
            return rotatedImage;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        JOptionPane.showMessageDialog(null, "Cropping image function is in progress (press mouse anywhere and start Scan function again)");
        rotatedImage = null;
        g = null;
        transform = null;
        return null;
    }


    //Methods from JNativeHook library which collect pressed keys from whole system but only main screen
    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        if (e.getKeyCode() == Globals.screenshotBind) {
            if (!Globals.cropisRunned) {
                captureScreenshot();
            } else {
                //JOptionPane.showMessageDialog(null, "Cropping image function is in progress (after closing popup start method again)");
                Globals.cropisRunned = false;
            }


        }
        if (e.getKeyCode() == Globals.exitBind) {
            JOptionPane optionPane = new JOptionPane("Program will close in 3 sec", JOptionPane.INFORMATION_MESSAGE);
            JDialog dialog = optionPane.createDialog("Message Dialog");
            Timer timer = new Timer(3000, e1 -> {
                dialog.setVisible(false);
                dialog.dispose();
            });
            timer.setRepeats(false);
            timer.start();
            dialog.setVisible(true);
            System.exit(0);

        }
        //I couldn't find solution for collecting rebind keybinds so in whole program temp is change on key code u press and it is used only when u press Rebind->AnyButton
        if (e.getKeyCode() != Globals.screenshotBind | e.getKeyCode() != Globals.exitBind) {
            Globals.temp = e.getKeyCode();
        }

    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {
        // Do nothing
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent e) {
        // Do nothing
    }
}

