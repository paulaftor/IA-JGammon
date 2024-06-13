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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.WindowConstants;
import javax.swing.JFrame;
import javax.swing.JLabel;

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
public class NewJPanel extends javax.swing.JPanel {
	private JLabel labelPlayer1;

	private JLabel colorBoard1;
	private JLabel statDoubles2;
	private JLabel statDoubles1;
	private JLabel doublesLabel;
	private JLabel statPip2;
	private JLabel statPip1;
	private JLabel piplabel;

	private JLabel colorBoard2;

	private JLabel colorOnBoard;

	private JLabel labelPlayer2;

	/**
	 * Auto-generated main method to display this JPanel inside a new JFrame.
	 */
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.getContentPane().add(new NewJPanel());
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}

	public NewJPanel() {
		super();
		initGUI();
	}

	private void initGUI() {
		try {
			GridBagLayout thisLayout = new GridBagLayout();
			thisLayout.rowWeights = new double[] { 0.1, 0.1, 0.1, 0.1 };
			thisLayout.rowHeights = new int[] { 7, 7, 7, 7 };
			thisLayout.columnWeights = new double[] { 0.1, 0.1, 0.1, 0.1 };
			thisLayout.columnWidths = new int[] { 7, 7, 7, 7 };
			this.setLayout(thisLayout);
			setPreferredSize(new Dimension(400, 300));
			{
				labelPlayer1 = new JLabel();
				this.add(labelPlayer1, new GridBagConstraints(1, 0, 1, 1, 0.0,
						0.0, GridBagConstraints.CENTER,
						GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
				labelPlayer1.setText("Player 1");
			}
			{
				labelPlayer2 = new JLabel();
				this.add(labelPlayer2, new GridBagConstraints(2, 0, 1, 1, 0.0,
						0.0, GridBagConstraints.CENTER,
						GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
				labelPlayer2.setText("Player 2");
			}
			{
				colorOnBoard = new JLabel();
				this.add(colorOnBoard, new GridBagConstraints(0, 1, 1, 1, 0.0,
						0.0, GridBagConstraints.CENTER,
						GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
				colorOnBoard.setText("coloronboard");
			}
			{
				colorBoard1 = new JLabel();
				this.add(colorBoard1, new GridBagConstraints(1, 1, 1, 1, 0.0,
						0.0, GridBagConstraints.CENTER,
						GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
				colorBoard1.setText("<color1>");
			}
			{
				colorBoard2 = new JLabel();
				this.add(colorBoard2, new GridBagConstraints(2, 1, 1, 1, 0.0,
						0.0, GridBagConstraints.CENTER,
						GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
				colorBoard2.setText("<color2>");
			}
			{
				piplabel = new JLabel();
				this.add(piplabel, new GridBagConstraints(
					0,
					2,
					1,
					1,
					0.0,
					0.0,
					GridBagConstraints.CENTER,
					GridBagConstraints.NONE,
					new Insets(0, 0, 0, 0),
					0,
					0));
				piplabel.setText("pip");
			}
			{
				statPip1 = new JLabel();
				this.add(statPip1, new GridBagConstraints(
					1,
					2,
					1,
					1,
					0.0,
					0.0,
					GridBagConstraints.CENTER,
					GridBagConstraints.NONE,
					new Insets(0, 0, 0, 0),
					0,
					0));
				statPip1.setText("<pip1>");
			}
			{
				statPip2 = new JLabel();
				this.add(statPip2, new GridBagConstraints(
					2,
					2,
					1,
					1,
					0.0,
					0.0,
					GridBagConstraints.CENTER,
					GridBagConstraints.NONE,
					new Insets(0, 0, 0, 0),
					0,
					0));
				statPip2.setText("<pip2>");
			}
			{
				doublesLabel = new JLabel();
				this.add(doublesLabel, new GridBagConstraints(
					0,
					3,
					1,
					1,
					0.0,
					0.0,
					GridBagConstraints.CENTER,
					GridBagConstraints.NONE,
					new Insets(0, 0, 0, 0),
					0,
					0));
				doublesLabel.setText("doubles");
			}
			{
				statDoubles1 = new JLabel();
				this.add(statDoubles1, new GridBagConstraints(
					1,
					3,
					1,
					1,
					0.0,
					0.0,
					GridBagConstraints.CENTER,
					GridBagConstraints.NONE,
					new Insets(0, 0, 0, 0),
					0,
					0));
				statDoubles1.setText("<doubles1>");
			}
			{
				statDoubles2 = new JLabel();
				this.add(statDoubles2, new GridBagConstraints(
					2,
					3,
					1,
					1,
					0.0,
					0.0,
					GridBagConstraints.CENTER,
					GridBagConstraints.NONE,
					new Insets(0, 0, 0, 0),
					0,
					0));
				statDoubles2.setText("<doubles2>");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
