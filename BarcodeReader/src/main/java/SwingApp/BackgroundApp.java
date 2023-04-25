package SwingApp;

/******************************************************************************
*   Project Name: BarcodeReader
*   Version:v0.4
*   Description: Project works as a background application with interface in tray
 *               main functionality is screenshoting MAIN screen only and crop
 *               BARCODE or MATRIX and get value from it. App contains also Rebind
 *               functionality which save keybind in config.ini. Program allow to
 *               run several times, and u can run scan function also several times
 *               (after pressing keybind few times it will start method and until
 *               popup is closed it will queue another usage of function)
*   Created by: Kacper Morawski
 ******************************************************************************/

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
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import com.google.zxing.*;
import com.google.zxing.common.HybridBinarizer;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

public class BackgroundApp implements NativeKeyListener {
    private static final int SCREEN_WIDTH = Toolkit.getDefaultToolkit().getScreenSize().width;
    private static final int SCREEN_HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().height;
    public static JFrame f = new JFrame();
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
    public static JPanel panel = null;
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
    public static int iterator;


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
        Proper p1 = new Proper();
        if(p1.init())
        {
           if(!p1.loadProperties())
           {
               System.exit(1);
           }
        }
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


            info.addActionListener(e -> JOptionPane.showMessageDialog(null, "Screenshot keybind: " + NativeKeyEvent.getKeyText(Globals.screenshotBind) + "\nExit program keybind: " + NativeKeyEvent.getKeyText(Globals.exitBind) + "\nRemember after using scan function to wait and close all Popups(if u will run more than one scan at the same time start clicking mouse anywhere and close all popups)\nVersion:v0.4\n\nCreated by: Kacper Morawski"));

            rebindKeys.addActionListener(e -> {
                //Create frame with 2 buttons
                f = new JFrame("Change keybinds");
                f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

                screenshotButton = new JButton("Screenshot Keybind");
                exitProgramButton = new JButton("Exit Keybind");

                screenshotButton.setBounds(25, 50, 200, 50);
                exitProgramButton.setBounds(275, 50, 200, 50);

                //Handiling rebind screenshot button
                screenshotButton.addActionListener(e12 -> {
                    dialog = new JDialog(f, "Pressed key will be assigned to screenshot", false);
                    dialog.setLocationRelativeTo(null);
                    dialog.setSize(200, 100);


                    dialog.addKeyListener(new KeyAdapter() {
                        @Override
                        public void keyPressed(KeyEvent e12) {
                            //Assigne pressed button to screenshot button
                            Globals.screenshotBind = Globals.temp;
                                try {
                                    //Saving new keybind in config.ini
                                    p1.savePropertoIni("SCREENSHOT_BIND",Globals.screenshotBind);
                                } catch (IOException ex) {
                                    throw new RuntimeException(ex);
                                } catch (URISyntaxException ex) {
                                    throw new RuntimeException(ex);
                                }
                            dialog.dispose();
                            f.dispose();
                            f = null;
                            dialog = null;
                            copyButton = null;
                            screenshotButton = null;
                            exitProgramButton = null;
                        }
                    });

                    dialog.setVisible(true);
                });

                exitProgramButton.addActionListener(e1 -> {
                    dialog = new JDialog(f, "Pressed key will be assigned to exit", false);
                    dialog.setLocationRelativeTo(null);
                    dialog.setSize(200, 100);

                    dialog.addKeyListener(new KeyAdapter() {
                        @Override
                        public void keyPressed(KeyEvent e1) {
                            Globals.exitBind = Globals.temp;
                                try {
                                    p1.savePropertoIni("EXIT_BIND",Globals.exitBind);
                                } catch (IOException ex) {
                                    throw new RuntimeException(ex);
                                } catch (URISyntaxException ex) {
                                    throw new RuntimeException(ex);
                                }
                            dialog.dispose();
                            f.dispose();
                            //f = null;
                            dialog = null;
                            copyButton = null;
                            screenshotButton = null;
                            exitProgramButton = null;

                        }
                    });


                    dialog.setVisible(true);

                });

                f.add(screenshotButton);
                f.add(exitProgramButton);
                f.setLayout(null);
                f.setSize(515, 200);
                f.setLocationRelativeTo(null);
                f.setVisible(true);

            });


            exitItem.addActionListener(e -> {
                // Close the application
                System.exit(0);
            });
            trayIcon.setPopupMenu(popup);
        }
    }


    public static class CropFrame {
        private int x = 0;
        private int y = 0;
        private int w = 0;
        private int h = 0;
        private int width = 0;
        private int height = 0;
        private boolean cropped = false;
        private boolean visible = true;
        private Point start;
        private Point end;




        //Main constructor which create frame with screenshot to crop from it
        public CropFrame(BufferedImage screenshot) {
            f.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
            f.setAlwaysOnTop(true);
            f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            f.setSize(screenshot.getWidth(), screenshot.getHeight());
            f.setExtendedState(JFrame.MAXIMIZED_BOTH);
            f.setUndecorated(true);
            f.setTitle("Crop Frame");

            //create new panel which contains screenshot as background and allow to draw rectangle on this panel
            panel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    g.drawImage(screenshot, 0, 0, this);
                    g.setColor(Color.BLUE);
                    if (start != null && end != null) {
                        x = Math.min(start.x, end.x);
                        y = Math.min(start.y, end.y);
                        w = Math.abs(start.x - end.x);
                        h = Math.abs(start.y - end.y);
                        g.drawImage(screenshot, 0, 0, this);
                        g.drawRect(x, y, w, h);
                    }
                    g.dispose();
                }
            };

            //handling mouse buttons (gathering mouse positions)
            f.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    start = e.getPoint();
                    end = start;
                    x = e.getX();
                    y = e.getY();
                }

                public void mouseReleased(MouseEvent e) {
                    end = e.getPoint();
                    width = e.getX() - x;
                    height = e.getY() - y;
                    //change cursor to normal one
                    f.setCursor(Cursor.getDefaultCursor());
                    //disapear of frame
                    f.dispose();
                    f= null;
                    visible = false;
                    cropped = true;
                }
            });
            f.addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    super.mouseDragged(e);
                    end = e.getPoint();
                    f.revalidate();
                    f.repaint();
                }
            });

            f.setContentPane(panel);
            f.pack();
            f.setVisible(true);
        }


        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public boolean isCropped() {
            return cropped;
        }
    }

    //Methods from JNativeHook library which collect pressed keys from whole system
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
        //I couldnt find solution for collecting rebind keybinds so in whole program temp is change on key code u press and it is used only when u press Rebind->AnyButton
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


    private void captureScreenshot() {
        try {
            if(iterator>Globals.max_number_of_running)
            {
                Runtime.getRuntime().exec("java -jar BarcodeReader.jar");
                System.exit(0);
            }
            iterator++;
            Globals.cropisRunned = true;
            robot = new Robot();
            screenshot = robot.createScreenCapture(new Rectangle(SCREEN_WIDTH, SCREEN_HEIGHT));
            if(f == null)
                f = new JFrame();
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

    //Method to recognize barcode and matrix from cropped image
    private String readBarcodeAndQRCodeFromScreenShot(BufferedImage image) {

        //added scale to change resolution of image(help with finding values hidden underneath)

        for (double v : scale) {
            //scale image
            scaledImg = scaleImage(image, v);
            try {
                pixels = scaledImg.getRGB(0, 0, scaledImg.getWidth(), scaledImg.getHeight(), null, 0, scaledImg.getWidth());
                if(pixels == null)
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
                if(pixels == null)
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

    public BufferedImage scaleImage(BufferedImage image, double scale) {
        try
        {
            scaledImg = new BufferedImage((int) (image.getWidth() * scale), (int) (image.getHeight() * scale), BufferedImage.TYPE_INT_RGB);
            g = scaledImg.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(image, 0, 0, (int) (image.getWidth() * scale), (int) (image.getHeight() * scale), null);
            g.dispose();
            return scaledImg;
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        JOptionPane.showMessageDialog(null, "Cropping image function is in progress (press mouse anywhere and start Scan function again)");
        scaledImg = null;
        g = null;
        return null;
    }

    public static BufferedImage rotateImage(BufferedImage image) throws IOException {
        try{
            rotatedImage = new BufferedImage(image.getHeight(), image.getWidth(), BufferedImage.TYPE_INT_RGB);
            g = rotatedImage.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            transform = new AffineTransform();
            transform.translate((float)(image.getHeight()) / 2, (float)(image.getWidth()) / 2);
            transform.rotate(Math.toRadians(90));
            transform.translate(-(float)(image.getWidth()) / 2, -(float)(image.getHeight()) / 2);
            g.drawImage(image, transform, null);
            g.dispose();
            return rotatedImage;
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        JOptionPane.showMessageDialog(null, "Cropping image function is in progress (press mouse anywhere and start Scan function again)");
        rotatedImage = null;
        g = null;
        transform = null;
        return null;
    }


    private boolean cropImage(BufferedImage screenshot) {
        String output;
        croppedImage = null;
        cropFrame = null;
        cropFrame = new CropFrame(screenshot);
        try {
            while (cropFrame.visible) {
                Thread.sleep(100);
            }
            if (cropFrame.isCropped()) {

                croppedImage = screenshot.getSubimage(cropFrame.x,cropFrame.y,cropFrame.w,cropFrame.h);
                //creating popup with selectable text area and copy button which contains Barcode/Matrix text
                output = readBarcodeAndQRCodeFromScreenShot(croppedImage);
                croppedImage = null;
                cropFrame = null;
                if(output==null)
                {
                    System.out.println("PUSTY TEKST");
                    Runtime.getRuntime().exec("java -jar BarcodeReader.jar");
                    System.exit(0);
                    return false;
                }
                panel = new JPanel();
                textArea = new JTextArea();
                textArea.setEditable(false);
                textArea.setText(output);
                copyButton = new JButton("Copy");
                copyButton.addActionListener(e -> {
                    selection = new StringSelection(output);
                    clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clipboard.setContents(selection, selection);
                });
                panel.add(textArea);
                panel.add(copyButton);
                JOptionPane.showOptionDialog(null, panel, "Message", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);
                Globals.cropisRunned = false;
                panel = null;
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
        panel = null;
        selection = null;
        clipboard = null;
        textArea = null;
        copyButton = null;
        return false;
    }
}

