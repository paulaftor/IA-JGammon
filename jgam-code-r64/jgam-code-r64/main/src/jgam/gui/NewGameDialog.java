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

package jgam.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.FileFilter;

import jgam.*;
import jgam.ai.*;
import jgam.game.*;
import jgam.net.*;
import jgam.util.*;

// source 1.5

/**
 * This dialog is shown before starting a new game.
 * 
 * It allows the user to chose the type of players and to configure them.
 *
 * Some pictures are taken from: iconarchive.com
 * @author Mattias Ulbrich
 */

@SuppressWarnings("serial")
public class NewGameDialog extends JDialog {
    private JPanel panel1 = new JPanel();
    private ResourceBundle msg = JGammon.getResources(
                                 "jgam.msg.NewGameDialog");
    private JRadioButton RBlocal = new JRadioButton();
    private GridBagLayout gridBagLayout1 = new GridBagLayout();
    private JRadioButton RBnetwork = new JRadioButton();
    private JRadioButton RBclient = new JRadioButton();
    private JRadioButton RBcomputer = new JRadioButton();
    private JRadioButton RBai = new JRadioButton();
    private Component component1 = Box.createHorizontalStrut(8);
    private JLabel jLabel1 = new JLabel();
    private JTextField name1 = new JTextField();
    private JLabel jLabel2 = new JLabel();
    private JTextField name2 = new JTextField();
    private JLabel jLabel4 = new JLabel();
    private JLabel jLabel5 = new JLabel();
    private JTextField server = new JTextField();
    private JTextField port = new JTextField();
    private JRadioButton RBserver = new JRadioButton();
    private JButton cancel = new JButton();
    private JButton OK = new JButton();
    private JLabel jLabel6 = new JLabel();
    private JLabel jLabel7 = new JLabel();
    private JLabel jLabel8 = new JLabel();
    private JLabel jLabel9 = new JLabel();
    private ButtonGroup topGroup = new ButtonGroup();
    private ButtonGroup remoteGroup = new ButtonGroup();
    private JLabel jLabel3 = new JLabel();
    private JTextField locName = new JTextField();

    private ImageIcon local = new ImageIcon(NewGameDialog.class.getResource(
                              "/jgam/img/local.gif"));
    private ImageIcon networkIcon = new ImageIcon(NewGameDialog.class.
                                    getResource(
                                    "/jgam/img/network.gif"));
    private ImageIcon aiIcon = new ImageIcon(NewGameDialog.class.
                               getResource(
                               "/jgam/img/computer.gif"));
    private ImageIcon openIcon = new ImageIcon(NewGameDialog.class.getResource(
                                 "/jgam/img/open.gif"));

    private boolean okPressed = false;
    private JGammon jgam;
    private JToggleButton loadButton = new JToggleButton();
    private JLabel fileNameLabel = new JLabel();
    private JCheckBox storedID = new JCheckBox();
    private JCheckBox invertSnapshot = new JCheckBox();
    private File boardFile = null;

    private Game game;
    private JGammonConnection gameConnection;

    public NewGameDialog(JGammon jgam) {
        super(jgam.getFrame(), true);
        this.jgam = jgam;
        setTitle(msg.getString("newgame"));
        try {
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            jbInit();
            getRootPane().setDefaultButton(OK);
            pack();
            setLocationRelativeTo(jgam.getFrame());
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public NewGameDialog() {
        try {
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            jbInit();
            pack();

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void jbInit() throws Exception {
        ChangeListener changeListener = new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                enableDisable();
            }
        };

        panel1.setLayout(gridBagLayout1);
        RBlocal.setSelected(true);
        RBlocal.setText(msg.getString("local"));
        RBlocal.addChangeListener(changeListener);
        RBnetwork.addChangeListener(changeListener);
        RBclient.addChangeListener(changeListener);
        RBserver.addChangeListener(changeListener);
        RBcomputer.addChangeListener(changeListener);
        RBnetwork.setText(msg.getString("network"));
        RBclient.setSelected(true);
        RBclient.setText(msg.getString("connect"));
        RBcomputer.setText(msg.getString("gnubg"));
        jLabel1.setText(msg.getString("locname1"));
        name1.setPreferredSize(new Dimension(110, 20));
        name1.setText("Paula");
        jLabel2.setText(msg.getString("locname2"));
        name2.setPreferredSize(new Dimension(110, 20));
        name2.setText("Luca");
        jLabel4.setText(msg.getString("server"));
        jLabel5.setText(msg.getString("port"));
        port.setPreferredSize(new Dimension(110, 20));
        port.setText("1777");
        RBserver.setText(msg.getString("startServer"));
        cancel.setText(msg.getString("cancel"));
        cancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        OK.setText(msg.getString("OK"));
        OK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                okPressed = true;
                setVisible(false);
            }
        });
        server.setPreferredSize(new Dimension(110, 20));
        jLabel6.setIcon(local);
        jLabel7.setIcon(networkIcon);
        jLabel8.setIcon(aiIcon);
        jLabel3.setText(msg.getString("localName"));
        locName.setPreferredSize(new Dimension(110, 20));
        locName.setText(InetAddress.getLocalHost().getCanonicalHostName());
        locAIName.setText(InetAddress.getLocalHost().getCanonicalHostName());
        jLabel9.setIcon(openIcon);
        loadButton.setEnabled(false);
        loadButton.setText(msg.getString("loadBoard"));
        loadButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loadButtonChanged();
            }
        });
        fileNameLabel.setMaximumSize(new Dimension(150, 15));
        fileNameLabel.setPreferredSize(new Dimension(150, 15));
        RBlocal.setEnabled(true);
        RBclient.setEnabled(true);
        storedID.setEnabled(false);
        storedID.setText(msg.getString("useidentity"));
        invertSnapshot.setEnabled(false);
        invertSnapshot.setText(msg.getString("loadinvert"));
        jLabel10.setText(msg.getString("localName"));
        topGroup.add(RBlocal);
        topGroup.add(RBnetwork);
        topGroup.add(RBcomputer);
        remoteGroup.add(RBclient);
        remoteGroup.add(RBserver);
        this.getContentPane().add(panel1, java.awt.BorderLayout.CENTER);
        panel1.add(RBlocal, new GridBagConstraints(1, 0, 4, 1, 0.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(20, 0, 0, 0), 2, 0));
        panel1.add(component1, new GridBagConstraints(1, 6, 1, 1, 0.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 20, 0));
        panel1.add(jLabel6, new GridBagConstraints(0, 0, 1, 3, 0.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(20, 20, 20, 20), 0, 0));
        panel1.add(jLabel1, new GridBagConstraints(2, 1, 2, 1, 0.0, 0.0
                , GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 7), 0, 0));
        panel1.add(jLabel2, new GridBagConstraints(2, 2, 2, 1, 0.0, 0.0
                , GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 7), 0, 0));
        panel1.add(jLabel4, new GridBagConstraints(2, 8, 2, 1, 0.0, 0.0
                , GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 7), 0, 0));
        panel1.add(jLabel5, new GridBagConstraints(2, 9, 2, 1, 0.0, 0.0
                , GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 7), 0, 0));
        panel1.add(jLabel9, new GridBagConstraints(0, 12, 1, 1, 0.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 20, 0, 0), 0, 0));
        panel1.add(jLabel3, new GridBagConstraints(2, 10, 2, 1, 0.0, 0.0
                , GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 7), 0, 0));
        panel1.add(RBserver, new GridBagConstraints(2, 6, 3, 1, 0.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        panel1.add(jLabel7, new GridBagConstraints(0, 4, 1, 4, 0.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(20, 0, 0, 0), 0, 0));
        panel1.add(RBclient, new GridBagConstraints(2, 7, 3, 1, 0.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        panel1.add(cancel, new GridBagConstraints(1, 14, 3, 1, 0.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 19, 0), 0, 0));
        panel1.add(OK, new GridBagConstraints(4, 14, 1, 1, 0.0, 0.0
                , GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 20, 20), 0, 0));
        panel1.add(loadButton, new GridBagConstraints(1, 12, 2, 1, 0.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 0, 0, 0), 0, 0));
        panel1.add(fileNameLabel, new GridBagConstraints(3, 12, 1, 1, 0.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 10, 0, 0), 0, 0));
        panel1.add(invertSnapshot, new GridBagConstraints(2, 13, 3, 1, 0.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 10, 0), 0, 0));
        panel1.add(storedID, new GridBagConstraints(3, 11, 2, 1, 0.0, 0.0
                , GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 20), 0, 0));
        panel1.add(server, new GridBagConstraints(4, 8, 1, 1, 0.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 20), 0, 0));
        panel1.add(port, new GridBagConstraints(4, 9, 1, 1, 0.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 20), 0, 0));
        panel1.add(locName, new GridBagConstraints(4, 10, 1, 1, 0.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 20), 0, 0));
        panel1.add(name1, new GridBagConstraints(4, 1, 1, 1, 0.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 20), 0, 0));
        panel1.add(name2, new GridBagConstraints(4, 2, 1, 1, 0.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 20), 0, 0));
        panel1.add(RBcomputer, new GridBagConstraints(1, 3, 4, 1, 0.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(20, 0, 0, 0), 0, 0));
        panel1.add(jLabel8, new GridBagConstraints(0, 3, 1, 2, 0.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(20, 0, 0, 0), 0, 0));
        panel1.add(RBnetwork, new GridBagConstraints(1, 5, 4, 1, 0.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(20, 0, 0, 0), 0, 0));
        panel1.add(locAIName, new GridBagConstraints(4, 4, 1, 1, 0.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 20), 0, 0));
        panel1.add(jLabel10, new GridBagConstraints(3, 4, 1, 1, 0.0, 0.0
                , GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 7), 0, 0));
    }


    public boolean showAndEval() {

        gameConnection = null;
        game = null;

        while (true) {
            okPressed = false;
            setVisible(true);

            // waiting for answer. ...
            if (!okPressed) {
                return false;
            }

            try {
                if (RBlocal.isSelected()) {
                    if (name1.getText().length() == 0) {
                        JOptionPane.showMessageDialog(this,
                                msg.getString("errorName1"),
                                msg.getString("error"),
                                JOptionPane.ERROR_MESSAGE);
                    } else
                    if (name2.getText().length() == 0) {
                        JOptionPane.showMessageDialog(this,
                                msg.getString("errorName2"),
                                msg.getString("error"),
                                JOptionPane.ERROR_MESSAGE);
                    } else {
                        Player player1 = new UIPlayer(name1.getText(), jgam);
                        Player player2 = new UIPlayer(name2.getText(), jgam);
                        game = new Game(new LocalDiceRoller(), player1, player2, jgam);
                        if (boardFile != null) {
                            BoardSetup snapshot = new FileBoardSetup(boardFile);
                            if (invertSnapshot.isSelected()) {
                                snapshot = new InvertedBoardSetup(snapshot);
                            }
                            game.setBoardSetup(snapshot);
                        }
                        return true;
                    }
                } else if (RBcomputer.isSelected()) {
                    if (locName.getText().length() == 0) {
                        JOptionPane.showMessageDialog(this,
                                msg.getString("errorLocName"),
                                msg.getString("error"),
                                JOptionPane.ERROR_MESSAGE);
                    } else {
                        AI selectedAI = selectAI();
                        if (selectedAI != null) {
                            Player player1 = new UIPlayer(locName.getText(), jgam);
                            // Player player1 = new GnubgPlayer("localhost", 1779);
                            Player player2 = new AIPlayer(selectedAI);
                            game = new Game(new LocalDiceRoller(), player1, player2, jgam);
                            if (boardFile != null) {
                                BoardSetup snapshot = new FileBoardSetup(boardFile);
                                if (invertSnapshot.isSelected()) {
                                    snapshot = new InvertedBoardSetup(snapshot);
                                }
                                game.setBoardSetup(snapshot);
                            }
                            return true;
                        }
                    }
                // insert here AI x AI
                //} else if (RBai.isSelected()) {
                //   AI selectedAI1 = selectAI();
                //   AI selectedAI2 = selectAI();
                //   if (selectedAI1 != null && selectedAI2 != null) {
                //       Player player1 = new AIPlayer(selectedAI1);
                //       Player player2 = new AIPlayer(selectedAI2);
                //       game = new Game(new LocalDiceRoller(), player1, player2, jgam);
                //       if (boardFile != null) {
                //           BoardSetup snapshot = new FileBoardSetup(boardFile);
                //           if (invertSnapshot.isSelected()) {
                //               snapshot = new InvertedBoardSetup(snapshot);
                //           }
                //           game.setBoardSetup(snapshot);
                //       return true;
                } else { // network game
                    if (locName.getText().length() == 0) {
                        JOptionPane.showMessageDialog(this,
                                msg.getString("errorLocName"),
                                msg.getString("error"),
                                JOptionPane.ERROR_MESSAGE);
                    } else if (RBclient.isSelected()) {
                        gameConnection = new JGammonConnection(server.getText(),
                                         Integer.parseInt(port.getText()),
                                         locName.getText());
                        Player locPlayer = new UIPlayer(locName.getText(), jgam);
                        Player remPlayer = new JGammonNetPlayer(gameConnection);
                        game = new Game(gameConnection, remPlayer, locPlayer, jgam);
                        game.setBoardSetup(gameConnection.getBoardSetup());
                        return true;
                    } else { // server is checked
                        BoardSetup snapshot = null;
                        if (boardFile != null) {
                            snapshot = new FileBoardSetup(boardFile);
                            if (invertSnapshot.isSelected()) {
                                snapshot = new InvertedBoardSetup(snapshot);
                            }
                        }

                        AsynchronousWaitingWindow window = mkWaitingWindow();
                        window.asyncShow();
                        try {
                            gameConnection = new JGammonConnection(Integer.parseInt(port.getText()),
                                             locName.getText(),
                                             snapshot, window);
                        } finally {
                            window.dispose();
                        }
                        Player locPlayer = new UIPlayer(locName.getText(), jgam);
                        Player remPlayer = new JGammonNetPlayer(gameConnection);

                        game = new Game(gameConnection, locPlayer, remPlayer, jgam);
                        game.setBoardSetup(snapshot == null ? BoardSnapshot.INITIAL_SETUP : snapshot);
                        return true;
                    }
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        msg.getString("errorPort"),
                        msg.getString("error"),
                        JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, ex,
                        msg.getString("error"),
                        JOptionPane.ERROR_MESSAGE);

                if (gameConnection != null) {
                    gameConnection.close(ex.toString());
                }
            }

        }
    }

    /**
     * Choose the AI to be used with a dialog box.
     *
     * @return the chosen AI, <code>null</code> if none selected.
     */
    private AI selectAI() {
        ArrayList<AI> ais = new ArrayList<AI>();
        ArrayList<String> descs = new ArrayList<String>();
        ServiceLoader<AI> services = ServiceLoader.load(AI.class, JGammon.getExtClassLoader());
        for (AI ai : services) {
            ais.add(ai);
            descs.add(ai.getDescription());
        }

        Object desc = JOptionPane.showInputDialog(this,
                      "Please choose the AI implementation to play against:",
                      "Choose computer player",
                      JOptionPane.QUESTION_MESSAGE, aiIcon, descs.toArray(), descs.get(0));

        if (desc == null) {
            return null;
        }

        return ais.get(descs.indexOf(desc));
    }


    /**
     * get the game that has recently been constructed
     * @return Game
     */
    public Game getGame() {
        return game;
    }

    /**
     * return the GameConnection for the recently constructed game.
     */
    public GameConnection getGameConnection() {
        return gameConnection;
    }

    public void enableDisable() {
        boolean local = RBlocal.isSelected();
        boolean net = RBnetwork.isSelected();
        boolean ai = RBcomputer.isSelected();
        boolean client = net && RBclient.isSelected();

        name1.setEnabled(local);
        jLabel1.setEnabled(local);
        name2.setEnabled(local);
        jLabel2.setEnabled(local);
        RBclient.setEnabled(net);
        RBserver.setEnabled(net);
        server.setEnabled(client);
        jLabel4.setEnabled(client);
        locName.setEnabled(net);
        jLabel3.setEnabled(net);
        port.setEnabled(net);
        jLabel5.setEnabled(net);
        jLabel10.setEnabled(ai);
        locAIName.setEnabled(ai);
        loadButton.setEnabled(!client);
        fileNameLabel.setEnabled(!client);
    }

    /**
     * feed infos from command line
     */

    public void feed(String mode, String portArg, String serverArg,
            String name1Arg, String name2Arg, String boardFileArg) {
        if (mode.equals("local")) {
            RBlocal.setSelected(true);
        } else if (mode.equals("client")) {
            RBnetwork.setSelected(true);
            RBclient.setSelected(true);
        } else if (mode.equals("server")) {
            RBnetwork.setSelected(true);
            RBserver.setSelected(true);
        } else if (mode.equals("ai")) {
            RBcomputer.setSelected(true);
        } else {
            throw new RuntimeException("unsupported mode: " + mode);
        }

        if (portArg != null) {
            port.setText(portArg);
        }
        if (serverArg != null) {
            server.setText(serverArg);
        }
        if (name1Arg != null) {
            name1.setText(name1Arg);
            locName.setText(name1Arg);
            locAIName.setText(name1Arg);
        }
        if (name2Arg != null) {
            name2.setText(name2Arg);
        }
        if (boardFileArg != null) {
            boardFile = new File(boardFileArg);
            fileNameLabel.setText(boardFile.getName());
            loadButton.setSelected(true);
            invertSnapshot.setEnabled(true);
        }

    }

    public void loadButtonChanged() {

        if (loadButton.isSelected()) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.addChoosableFileFilter(boardFileFilter);
            fileChooser.setAccessory(new BoardFileView(fileChooser));
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                boardFile = fileChooser.getSelectedFile();
                fileNameLabel.setText(fileChooser.getSelectedFile().getName());
                invertSnapshot.setEnabled(true);
            } else {
                boardFile = null;
                fileNameLabel.setText("");
                loadButton.setSelected(false);
                invertSnapshot.setEnabled(false);
            }
        } else {
            boardFile = null;
            fileNameLabel.setText("");
            invertSnapshot.setEnabled(false);
        }

    }

    private FileFilter boardFileFilter = new FileFilter() {
        public boolean accept(File pathname) {
            return (pathname.getName().toLowerCase().endsWith(".board")
                    || pathname.isDirectory());
        }

        public String getDescription() {
            return msg.getString("boardFilter");
        }
    };
    private JLabel jLabel10 = new JLabel();
    private JTextField locAIName = new JTextField();

    private AsynchronousWaitingWindow mkWaitingWindow() {
        return new AsynchronousWaitingWindow(
                JGammon.jgammon().getFrame(),
                msg.getString("openingServer"),
                msg.getString("serverListening"),
                msg.getString("abort"));
    }

}
