package SwingApp;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.swing.*;
import com.google.zxing.*;
import com.google.zxing.common.HybridBinarizer;
import javafx.scene.input.KeyCode;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.NativeInputEvent;
import org.jnativehook.keyboard.NativeKeyAdapter;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

public class BackgroundApp implements NativeKeyListener {
    private static final int SCREEN_WIDTH = Toolkit.getDefaultToolkit().getScreenSize().width;
    private static final int SCREEN_HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().height;

    private static int screenshotBind = NativeKeyEvent.VC_F6;
    private static int temp;
    private static int exitBind = NativeKeyEvent.VC_F7;
    private static BufferedImage screenshot = null;


    public BackgroundApp() {
        try {
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(this);
        } catch (NativeHookException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if(SystemTray.isSupported()) {
            BackgroundApp app = new BackgroundApp();
            SystemTray tray = SystemTray.getSystemTray();
            Image icon = Toolkit.getDefaultToolkit().getImage(BackgroundApp.class.getResource("/icon.jpg"));
            TrayIcon trayIcon = new TrayIcon(icon, "BarcodeReader");
            trayIcon.setImageAutoSize(true);

            try
            {
                tray.add(trayIcon);
            } catch (AWTException e) {
                throw new RuntimeException(e);
            }
            PopupMenu popup = new PopupMenu();
            MenuItem rebindKeys = new MenuItem("Rebind");
            MenuItem startMethod = new MenuItem("Scan");
            MenuItem info = new MenuItem("Information");
            MenuItem exitItem = new MenuItem("Exit");
            popup.add(startMethod);
            popup.add(info);
            popup.add(rebindKeys);
            popup.add(exitItem);

            startMethod.addActionListener(e ->
            {
                NativeKeyEvent event = new NativeKeyEvent(NativeKeyEvent.NATIVE_KEY_PRESSED, 0, screenshotBind, screenshotBind, NativeKeyEvent.CHAR_UNDEFINED);
                GlobalScreen.postNativeEvent(event);
            });


            info.addActionListener(e -> {
                JOptionPane.showMessageDialog(
                        null,
                        "Screenshot keybind: "+ NativeKeyEvent.getKeyText(screenshotBind) + "\nExit program keybind: "+NativeKeyEvent.getKeyText(exitBind) +"\nVersion:v0.1\nCreated by: Kacper Morawski"
                );

            });

            rebindKeys.addActionListener(e -> {
                JFrame frame = new JFrame("Change keybinds");
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);


                JButton screenshotButton = new JButton("Screenshot Keybind");
                JButton exitProgramButton = new JButton("Exit Keybind");

                screenshotButton.setBounds(25,50,200,50);
                exitProgramButton.setBounds(275,50,200,50);

                screenshotButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        JDialog dialog = new JDialog(frame, "Pressed key will be assigned to screenshot", false);
                        dialog.setLocationRelativeTo(null);
                        dialog.setSize(200, 100);


                        dialog.addKeyListener(new KeyAdapter() {
                            @Override
                            public void keyPressed(KeyEvent e) {
                                screenshotBind = temp;
                                dialog.dispose();
                                frame.dispose();
                            }
                        });

                        dialog.setVisible(true);
                    }
                });

                exitProgramButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        JDialog dialog = new JDialog(frame, "Pressed key will be assigned to exit", false);
                        dialog.setLocationRelativeTo(null);
                        dialog.setSize(200, 100);

                        dialog.addKeyListener(new KeyAdapter() {
                            @Override
                            public void keyPressed(KeyEvent e) {

                                exitBind = temp;
                                dialog.dispose();
                                frame.dispose();
                            }
                        });


                        dialog.setVisible(true);
                    }
                });

                //frame.add(screenshotButton);
                frame.add(exitProgramButton);
                frame.setLayout(null);
                frame.setSize(515, 200);
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            });


            exitItem.addActionListener(e -> {
                // Close the application
                System.exit(0);
            });
            trayIcon.setPopupMenu(popup);
        }
    }



    public class CropFrame
    {
        private final BufferedImage screenshot;
        private int x = 0;
        private int y = 0;
        private int width = 0;
        private int height = 0;
        private boolean cropped = false;
        private boolean visible = true;
        private Point start;
        private Point end;
        private JPanel p;


        public CropFrame(BufferedImage screenshot) {
            this.screenshot = screenshot;
            JFrame f = new JFrame();
            f.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
            f.setAlwaysOnTop(true);
            f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            f.setSize(screenshot.getWidth(), screenshot.getHeight());
            f.setExtendedState(JFrame.MAXIMIZED_BOTH);
            f.setUndecorated(true);
            f.setTitle("Crop Frame");

            p = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    g.drawImage(screenshot,0,0,this);
                    g.setColor(Color.BLUE);
                    if (start != null && end != null) {
                        int x = Math.min(start.x, end.x);;
                        int y = Math.min(start.y, end.y);
                        int w = Math.abs(start.x - end.x);
                        int h = Math.abs(start.y - end.y);
                        g.drawImage(screenshot,0,0,this);
                        g.drawRect(x, y, w, h);
                    }
                }
            };



            f.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {

                    start = e.getPoint();
                    end = start;
                    x = e.getX();
                    y = e.getY();
                    //f.revalidate();
                    //f.repaint();
                }

                public void mouseReleased(MouseEvent e) {
                    end = e.getPoint();
                    width = e.getX() - x;
                    height = e.getY() - y;
                    f.setCursor(Cursor.getDefaultCursor());
                    f.dispose();
                    visible = false;
                    cropped = true;
                    f.revalidate();
                    f.repaint();

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

            f.setContentPane(p);
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


    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        if (e.getKeyCode() == screenshotBind) {
            captureScreenshot();

        }
        if(e.getKeyCode() == exitBind)
        {
            JOptionPane optionPane = new JOptionPane("Program will close in 3 sec",JOptionPane.INFORMATION_MESSAGE);
            JDialog dialog = optionPane.createDialog("Message Dialog");
            Timer timer = new Timer(3000, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    dialog.setVisible(false);
                    dialog.dispose();
                }
            });
            timer.setRepeats(false);
            timer.start();

            dialog.setVisible(true);
            System.exit(0);
        }
        if(e.getKeyCode() != screenshotBind | e.getKeyCode() != exitBind)
        {
            temp = e.getKeyCode();
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
            Robot robot = new Robot();
            screenshot = robot.createScreenCapture(new Rectangle(SCREEN_WIDTH, SCREEN_HEIGHT));
            //ImageIO.write(screenshot, "png", new File("screenshot.png"));
            cropImage(screenshot);
            //
        } catch (AWTException  ex) {
            ex.printStackTrace();
        }
    }

    private String readBarcodeAndQRCodeFromScreenShot(BufferedImage image)
    {
        double[] scale = {0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9,1,2,3,4,5,6,7,8,9,10};
        for(int i = 0 ; i < scale.length ; i++)
        {
            BufferedImage scaledImg = scaleImage(image,scale[i]);
            try {
                int[] pixels = scaledImg.getRGB(0, 0, scaledImg.getWidth(), scaledImg.getHeight(), null, 0, scaledImg.getWidth());
                RGBLuminanceSource source = new RGBLuminanceSource(scaledImg.getWidth(), scaledImg.getHeight(), pixels);
                BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
                MultiFormatReader reader = new MultiFormatReader();
                Result result = reader.decodeWithState(bitmap);

                return result.getText();
            }
            catch (NotFoundException e) {
                System.out.println("Doesn't found result of matrix for scale: ");
            }
            try {
                BufferedImage rotatedImage = rotateImage(scaledImg,90);
                int[] pixels = rotatedImage.getRGB(0, 0, rotatedImage.getWidth(),rotatedImage.getHeight() , null, 0, rotatedImage.getWidth());
                RGBLuminanceSource source = new RGBLuminanceSource(rotatedImage.getWidth(),rotatedImage.getHeight(), pixels);
                BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
                MultiFormatReader reader = new MultiFormatReader();
                Result result = reader.decodeWithState(bitmap);

                return result.getText();
            }
            catch (NotFoundException e) {
                System.out.println("Doesn't found result of matrix after rotate: ");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        JOptionPane.showMessageDialog(null,"Matrix/Barcode not found (try to scale matrix)");
        throw new NullPointerException();
    }

    public static BufferedImage scaleImage(BufferedImage image,double scale)
    {
        BufferedImage scaledImage = new BufferedImage((int) (image.getWidth()*scale), (int) (image.getHeight()*scale),BufferedImage.TYPE_INT_RGB);
        Graphics2D g = scaledImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(image,0,0, (int) (image.getWidth()*scale), (int) (image.getHeight()*scale),null);
        g.dispose();
        return scaledImage;
    }

    public static BufferedImage rotateImage(BufferedImage image,int rotate) throws IOException {
        int height = image.getHeight();
        int width = image.getWidth();

        BufferedImage rotatedImage = new BufferedImage(height,width,BufferedImage.TYPE_INT_RGB);
        Graphics2D g = rotatedImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        AffineTransform transform = new AffineTransform();
        transform.translate(height/2,width/2);
        transform.rotate(Math.toRadians(90));
        transform.translate(-width/2,-height/2);
        g.drawImage(image,transform,null);
        return rotatedImage;
    }


    private void cropImage(BufferedImage screenshot) {
        try {
            CropFrame cropFrame = new CropFrame(screenshot);
            while (cropFrame.visible == true) {
                Thread.sleep(100);
            }
            if (cropFrame.isCropped()) {
                BufferedImage croppedImage = null;
                if(cropFrame.getWidth()>0 & cropFrame.getHeight()>0)
                    croppedImage = screenshot.getSubimage(cropFrame.getX() , cropFrame.getY(), cropFrame.getWidth(), cropFrame.getHeight());
                else if(cropFrame.getWidth()>0 & cropFrame.getHeight()<0)
                    croppedImage = screenshot.getSubimage(cropFrame.getX(), cropFrame.getY()-Math.abs(cropFrame.getHeight()), cropFrame.getWidth(),Math.abs(cropFrame.getHeight()));
                else if(cropFrame.getWidth()<0 & cropFrame.getHeight()>0)
                    croppedImage = screenshot.getSubimage(cropFrame.getX()-Math.abs(cropFrame.getWidth()) , cropFrame.getY(), Math.abs(cropFrame.getWidth()), cropFrame.getHeight());
                else if(cropFrame.getWidth()<0 & cropFrame.getHeight()<0)
                    croppedImage = screenshot.getSubimage(cropFrame.getX()-Math.abs(cropFrame.getWidth()), cropFrame.getY()-Math.abs(cropFrame.getHeight()),Math.abs(cropFrame.getWidth()) , Math.abs(cropFrame.getHeight()));


                String output = readBarcodeAndQRCodeFromScreenShot(croppedImage);
                JPanel panel = new JPanel();
                JTextArea textArea = new JTextArea();
                textArea.setEditable(false);
                textArea.setText(output);
                JButton copyButton = new JButton("Copy");
                copyButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        StringSelection selection = new StringSelection(output);
                        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                        clipboard.setContents(selection,selection);
                    }
                });
                panel.add(textArea);
                panel.add(copyButton);
                int option = JOptionPane.showOptionDialog(null,panel,"Message",JOptionPane.DEFAULT_OPTION,JOptionPane.PLAIN_MESSAGE,null,null,null);

            }
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
}

