/*
 * JGammon: A backgammon client written in Java
 * Copyright (C) 2005/06 Mattias Ulbrich
 *
 * JGammon includes: - playing over network
 *                   - plugin mechanism for graphical board implementations
 *                   - artificial intelligence player
 *                   - plugin mechanism for AI players
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */



package jgam.temp;

import java.awt.*;

import javax.swing.*;
import java.awt.event.*;

/**
 * <p>Title: JGam - Java Backgammon</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class Frame1 extends JFrame {
    private BorderLayout borderLayout1 = new BorderLayout();
    private JButton jButton1 = new JButton();
    private JLabel label = new JLabel();

    public Frame1() {
        try {
            jbInit();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void jbInit() throws Exception {
        getContentPane().setLayout(borderLayout1);
        this.getContentPane().add(jButton1, java.awt.BorderLayout.SOUTH);
        this.getContentPane().add(label, java.awt.BorderLayout.NORTH);
        jButton1.setBorderPainted(false);
        jButton1.setIcon(new LinesIcon(jButton1));
        LinesIcon li = new LinesIcon(label);
        label.setIcon(li);
        label.addMouseListener(li);
    }

    public static void main(String[] args) {
        Frame1 frame1 = new Frame1();
        frame1.setVisible(true);
    }
}


class LinesIcon implements Icon, MouseListener {
    JComponent comp;
    boolean mouse;
    LinesIcon(JComponent c) {
        this.comp = c;
    }

    public void paintIcon(Component jc, Graphics g, int x, int y) {
        int width = jc.getWidth() - 2*x;
        System.out.println("g.getColor() = " + g.getColor());
        g.setColor(jc.getBackground().darker().darker());
        g.fill3DRect(x+4,y+3,width-8,4,!mouse);
    }

    public int getIconWidth() {
        return comp.getWidth();
    }

    public int getIconHeight() {
        return 10;
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
        mouse = false;
        comp.repaint();
    }

    public void mousePressed(MouseEvent e) {
        mouse = true;
        comp.repaint();
    }

    public void mouseReleased(MouseEvent e) {
        mouse = false;
        comp.repaint();
    }
}
