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
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

/**
 * <p>Title: JGam - Java Backgammon</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author Mattias Ulbrich
 * @version 1.0
 */
public class Frame3 extends JPanel  {
    public Frame3() {
        try {
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }



    private void jbInit() throws Exception {
        jLabel1.setText("jLabel1");
        jLabel2.setText("jLabel2");
        this.setLayout(gridLayout1);
        gridLayout1.setRows(2);
        this.setBackground(UIManager.getColor("ToolTip.background"));
        this.setBorder(null);
        this.add(jLabel1, null);
        this.add(jLabel2, null);
    }

    private JLabel jLabel1 = new JLabel();
    private JLabel jLabel2 = new JLabel();
    private GridLayout gridLayout1 = new GridLayout();
    private Border border1 = BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED, Color.white, Color.white, new Color(109, 109, 110), new Color(156, 156, 158)),
                             BorderFactory.createEmptyBorder(15, 15, 15, 15));
    private Border border2 = BorderFactory.createLineBorder(Color.white, 2);

    public static void main(String[] args) {

        Window w = new Window(new Frame());
        w.add(new Frame3());
        w.pack();
        w.setVisible(true);

    }

}
