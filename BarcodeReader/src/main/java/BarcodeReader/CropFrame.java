package BarcodeReader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import static BarcodeReader.Globals.f;

public class CropFrame {
    int x = 0;
    int y = 0;
    int w = 0;
    int h = 0;
    private int width = 0;
    private int height = 0;
    private boolean cropped = false;
    boolean visible = true;
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
        Globals.panel = new JPanel() {
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
                Globals.f.setCursor(Cursor.getDefaultCursor());
                //disapear of frame
                Globals.f.dispose();
                Globals.f= null;
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

        Globals.f.setContentPane(Globals.panel);
        Globals.f.pack();
        Globals.f.setVisible(true);
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

