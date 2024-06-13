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



package jgam;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.*;
import java.io.IOException;
import java.io.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import jgam.util.*;
import java.awt.*;
import jgam.game.*;

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
 *
 * @todo changed as the new game came in ... not checked
 */
public class HintFrame extends JFrame {
    private BorderLayout borderLayout1 = new BorderLayout();
    private String command;
    private Process process;
    private JScrollPane jScrollPane1 = new JScrollPane();
    private JButton jButton1 = new JButton();
    private JTextArea jTextArea1 = new JTextArea();
    private StringBuffer content = new StringBuffer();

    private HintFrame(String command) {
        super("Hint!");
        this.command = command;
        try {
            jbInit();
            startProcess();
            pack();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void startProcess() throws IOException {
        process = Runtime.getRuntime().exec(command);
        final InputStream is = process.getInputStream();

        new Thread("gnubglistener") {
            public void run() {
                try {
                    byte[] buffer = new byte[1024];
                    int read = is.read(buffer);
                    while (read != -1) {
                        addContent(buffer, read);
                        read = is.read(buffer);
                    }
                } catch (IOException ex) {
                }
            }
        }.start();

        sendCommand("set player 0 human");
        sendCommand("set player 0 name white");
        sendCommand("set player 1 human");
        sendCommand("set player 1 blue");
    }

    synchronized private void addContent(byte[] c, int length) {
        for (int i = 0; i < length; i++) {
            content.append((char) c[i]);
        }
        repaint();
    }

    public void paint(Graphics g) {
        jTextArea1.setText(content.toString());
        super.paint(g);
    }

    private void addContent(String s) {
        addContent(s.getBytes(), s.length());
    }

    private void sendCommand(String command) {
        PrintStream ps = new PrintStream(process.getOutputStream());
        addContent("> " + command + "\n");
        ps.println(command);
        ps.flush();
    }

    private void jbInit() throws Exception {
        getContentPane().setLayout(borderLayout1);
        jButton1.setText("Hint please");
        jButton1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jButton1_actionPerformed(e);
            }
        });
        jTextArea1.setColumns(80);
        jTextArea1.setRows(50);
        this.getContentPane().add(jButton1, java.awt.BorderLayout.SOUTH);
        jTextArea1.setFont(new java.awt.Font("Monospaced", Font.PLAIN, 12));
        this.getContentPane().add(jScrollPane1, java.awt.BorderLayout.CENTER);
        jScrollPane1.getViewport().add(jTextArea1);

    }

    public static void showHintFrame() {

        String command = "c:\\spiele\\gnubg\\gnubg-no-gui";
        command = (String) JOptionPane.showInputDialog(new JFrame(),
                "GnuBG commandline: ", command);

        new HintFrame(command).setVisible(true);
    }

    public void jButton1_actionPerformed(ActionEvent e) {

        // get the game!
        Game game = jgam.JGammon.jgammon().getGame();

        sendCommand("first move");
        sendCommand("new game");
        sendCommand("set turn 0");
        sendCommand("set board " + positionID(game));
        sendCommand("set turn " + (game.getCurrentPlayer().getNumber()-1));

        int dice[] = game.getDice();
        if (dice != null) {
            sendCommand("set dice " + dice[0] + " " + dice[1]);
        }
        sendCommand("hint");
    }

    /**
     * see: http://www.gnu.org/software/gnubg/manual/html_node/A-technical-description-of-the-position-ID.html
     * @param game Game
     * @return String
     */
    private static String positionID(Game game) {
        BoardSetup setup = game.getGameBoard();

        BitArray bits = new BitArray(80);
        int counter = 0;
        for (int i = 1; i <= 25; i++) {
            for (int j = 0; j < setup.getPoint(1, i); j++) {
                bits.setBit(counter++);
            }
            bits.clearBit(counter++);
        }

        for (int i = 1; i <= 25; i++) {
            for (int j = 0; j < setup.getPoint(0, i); j++) {
                bits.setBit(counter++);
            }
            bits.clearBit(counter++);
        }

        byte byteRes[] = bits.toByteArray(BitArray.LSB_FIRST);
        String stringRes = Base64.encode(byteRes);
        return stringRes.substring(0, 14); // strip trailing ==
    }
}
