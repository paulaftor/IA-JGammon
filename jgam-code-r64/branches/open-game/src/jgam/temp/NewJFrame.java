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
 */package jgam.temp;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
 * This code was edited or generated using CloudGarden's Jigloo SWT/Swing GUI
 * Builder, which is free for non-commercial use. If Jigloo is being used
 * commercially (ie, by a corporation, company or business for any purpose
 * whatever) then you should purchase a license for each developer using Jigloo.
 * Please visit www.cloudgarden.com for details. Use of Jigloo implies
 * acceptance of these licensing terms. A COMMERCIAL LICENSE HAS NOT BEEN
 * PURCHASED FOR THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED LEGALLY FOR
 * ANY CORPORATE OR COMMERCIAL PURPOSE.
 */
public class NewJFrame extends javax.swing.JFrame {
	private JTable statTable;
    private JPopupMenu boardPopupMenu;
    private JMenuItem jMenuItem1;
    private JMenuItem jMenuItem2;

	/**
	 * Auto-generated main method to display this JFrame
	 */
	public static void main(String[] args) {
		NewJFrame inst = new NewJFrame();
		inst.setVisible(true);
	}

	public NewJFrame() {
		super();
		initGUI();
	}

	private void initGUI() {
		try {
			setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			{
				DefaultTableModel statTableModel = new DefaultTableModel(8, 3);
				statTableModel.setValueAt("Player 1", 0, 1);
				statTableModel.setValueAt("Player 2", 0, 2);
				statTableModel.setValueAt("coloronboard", 1, 0);
				statTableModel.setValueAt("checkers",2,0);
				statTableModel.setValueAt("piptogo",3,0);
				statTableModel.setValueAt("pipsofar",4,0);
				statTableModel.setValueAt("doubles",5,0);
				statTableModel.setValueAt("hits",6,0);
				statTableModel.setValueAt("ai_eval",7,0);
				statTable = new JTable();
				getContentPane().add(statTable, BorderLayout.CENTER);
				statTable.setModel(statTableModel);
                {
                    boardPopupMenu = new JPopupMenu();
                    setComponentPopupMenu(statTable, boardPopupMenu);
                    {
                        jMenuItem1 = new JMenuItem();
                        boardPopupMenu.add(jMenuItem1);
                        jMenuItem1.setText("turnboard");                       
                        jMenuItem1.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent evt) {
                                //
                            }
                        });
                    }
                    {
                        jMenuItem2 = new JMenuItem();
                        boardPopupMenu.add(jMenuItem2);
                        jMenuItem2.setText("flipboard");
                        jMenuItem2.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent evt) {
                                System.out
                                    .println("jMenuItem2.actionPerformed, event="
                                        + evt);
                                //TODO add your code for jMenuItem2.actionPerformed
                            }
                        });
                    }
                }
			}
			pack();
			setSize(400, 300);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
    
    /**
    * Auto-generated method for setting the popup menu for a component
    */
    private void setComponentPopupMenu(
        final java.awt.Component parent,
        final javax.swing.JPopupMenu menu) {
        parent.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent e) {
                if (e.isPopupTrigger())
                    menu.show(parent, e.getX(), e.getY());
            }
            public void mouseReleased(java.awt.event.MouseEvent e) {
                if (e.isPopupTrigger())
                    menu.show(parent, e.getX(), e.getY());
            }
        });
    }
    
    private void jMenuItem1ActionPerformed(ActionEvent evt) {
        System.out.println("jMenuItem1.actionPerformed, event=" + evt);
        //TODO add your code for jMenuItem1.actionPerformed
    }

}
