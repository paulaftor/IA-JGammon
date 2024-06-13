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
import java.text.*;
import java.util.*;

import javax.swing.*;

import jgam.*;
import jgam.board.*;
import jgam.game.*;
import jgam.util.*;

// source 1.5

/**
 * This is the MainWindow-class.
 *
 * On top it has got several Buttons and a message area. In the center lies the
 * board (a JComponent). There is also a menubar.
 *
 * Buttons' actions are partly handled by JGam and partly by this class.
 *
 * @author Mattias Ulbrich
 */
@SuppressWarnings("serial")
public class JGamFrame extends JFrame {
    private JPanel contentPane;

    private final BorderLayout borderLayout1 = new BorderLayout();

    private final JButton buttonNew = new JButton();

    private final JButton buttonGiveUp = new JButton();

    private final JButton buttonDouble = new JButton();

    private final JButton buttonRoll = new JButton();

    private final JButton buttonYield = new JButton();

    private final JLabel label = new JLabel();

    private final BoardComponent board;

    private final JGammon jGam;

    private final JButton buttonTurn = new JButton();

    private final JButton buttonFlip = new JButton();

    private final JButton buttonHistory = new JButton();

    private final JButton buttonUndo = new JButton();

    private final JButton buttonChat = new JButton();

    private final JButton buttonHelp = new JButton();

    private final JButton buttonSave = new JButton();

    private final JMenuBar jMenuBar = new JMenuBar();

    private final JMenu jMenuGame = new JMenu();

    private final JMenuItem jMenuItemNew = new JMenuItem();

    private final JMenuItem jMenuItemAbort = new JMenuItem();

    private final JMenuItem jMenuItemSave = new JMenuItem();

    private final JMenu jMenuWindow = new JMenu();

    private final JMenuItem jMenuItemHistory = new JMenuItem();

    private final JMenuItem jMenuItemChat = new JMenuItem();

    private final JMenu jMenuHelp = new JMenu();

    private final JMenuItem jMenuItem6 = new JMenuItem();

    private final JMenuItem jMenuAbout = new JMenuItem();

    private final JMenu jMenuDesign = new JMenu();

    private final JMenuItem jMenuItem8 = new JMenuItem();

    private final JMenuItem jMenuItem9 = new JMenuItem();

    private final JToolBar jToolBarTop = new JToolBar();

    private final JMenuItem jMenuItemUndo = new JMenuItem();

    private final JMenuItem jMenuItemExit = new JMenuItem();

    private final ButtonGroup buttonGroupDesigns = new ButtonGroup();

    private final Component component1 = Box.createHorizontalStrut(12);

    private final Component component2 = Box.createHorizontalStrut(12);

    private final Component component3 = Box.createHorizontalStrut(12);

    private final JMenuItem jMenuItemColorSwap = new JMenuItem();

    private final Component component4 = Box.createHorizontalStrut(12);

    private final JButton buttonSwap = new JButton();

    private final JCheckBoxMenuItem jMenuEnableResizing = new JCheckBoxMenuItem();

    private final JCheckBoxMenuItem jMenuShowPreviewCheckers = new JCheckBoxMenuItem();

    private final JMenuItem jMenuItemDemo = new JMenuItem();

    private Player labelPlayer = null;

    private final ImageIcon imageDice = icon("dice.gif");

    private final ImageIcon yieldIcon = icon("transfer.png");

    private final ImageIcon turnIcon = icon("turn.gif");

    private final ImageIcon flipIcon = icon("flip.gif");

    private final ImageIcon msgIcon = icon("msg.gif");

    // private ImageIcon newMsgIcon = icon("newmsg.gif");
    private final ImageIcon saveIcon = icon("save.gif");

    private final ImageIcon newIcon = icon("go.gif");

    private final ImageIcon whiteFlagIcon = icon("whiteflag.gif");

    private final ImageIcon x2Icon = icon("x2.gif");

    private final ImageIcon undoIcon = icon("undo.gif");

    private final ImageIcon helpIcon = icon("help.gif");

    private final ImageIcon historyIcon = icon("book.gif");

    private final ImageIcon swapColorIcon = icon("swapcolors.gif");

    public static ImageIcon smallClock = icon("smallclock.gif");

    private final ActionManager actionManager = new ActionManager();

    ResourceBundle msg = JGammon.getResources("jgam.msg.JGamFrame");

    private int designIndex = 0;

    public JGamFrame(JGammon jgam) {
        jGam = jgam;
        board = new BoardComponent(getMenuBoardImplementation(), null);
        new BoardMouseListener(board, jGam);
        try {
            setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
            // setResizable(false);
            jbInit();
            subscribeJGam();
            setGame(false);
            initDesignMenu();
            setPropertyDimension();
            pack();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    /**
     * initDesignMenu
     */
    private void initDesignMenu() {
        ItemListener itemListener = new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                designChanged(e);
            }
        };

        int count = 0;
        for (BoardImplementation bi : JGammon.getBoardImplementations()) {
            JRadioButtonMenuItem item = new JRadioButtonMenuItem(bi.toString());
            jMenuDesign.add(item);
            buttonGroupDesigns.add(item);
            // only select first!
            item.setSelected(count == 0);
            item.setActionCommand(Integer.toString(count));
            item.addItemListener(itemListener);
            count++;
        }

        jMenuDesign.addSeparator();
        jMenuDesign.add(jMenuShowPreviewCheckers);
        jMenuDesign.add(jMenuEnableResizing);
    }

    /**
     * if in the properties-file the property "jgam.dimension" is set to a
     * string like "111x2222"
     *
     * the dimension of the BoardComponent will be set. Otherwise standard will
     * be used.
     */
    private void setPropertyDimension() {
        try {
            String dimstring = System.getProperty("jgam.dimension");
            if (dimstring != null) {
                String sizes[] = dimstring.split("x");
                Dimension d = new Dimension(Integer.parseInt(sizes[0]), Integer
                        .parseInt(sizes[1]));
                setSize(d);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // for development tests
//    private JGamFrame() {
//        try {
//            jbInit();
//        } catch (Exception exception) {
//            exception.printStackTrace();
//        }
//    }

    private void subscribeJGam() {
        actionManager.subscribeHandler("newgame", jGam);
        actionManager.subscribeHandler("saveboard", jGam);
        actionManager.subscribeHandler("help", jGam);
        actionManager.subscribeHandler("close", jGam);
        actionManager.subscribeHandler("chat", jGam);
        actionManager.subscribeHandler("demo", jGam);
    }

    /**
     * Component initialization.
     *
     * @throws java.lang.Exception
     */
    private void jbInit() throws Exception {
        contentPane = (JPanel) getContentPane();

        contentPane.setLayout(borderLayout1);
        this.setJMenuBar(jMenuBar);
        setTitle("JGammon");
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                frameResized(e);
            }
        });
        this.addWindowListener(new JGamFrame_this_windowAdapter(this));
        buttonNew.setToolTipText(msg.getString("newgame"));
        actionManager.add(buttonNew, "newgame");
        buttonNew.setIcon(newIcon);
        actionManager.add(buttonGiveUp, "giveup");
        buttonGiveUp.setIcon(whiteFlagIcon);
        buttonGiveUp.setToolTipText(msg.getString("giveup"));
        actionManager.add(buttonDouble, "double");
        buttonDouble.setIcon(x2Icon);
        buttonDouble.setToolTipText(msg.getString("double"));
        actionManager.add(buttonRoll, "roll");
        buttonRoll.setIcon(imageDice);
        buttonRoll.setText(msg.getString("roll"));
        buttonYield.setText("YIELD");
        buttonYield.setIcon(yieldIcon);
        actionManager.add(buttonYield, "yield");
        label.setMaximumSize(new Dimension(10000, 32));
        label.setPreferredSize(new Dimension(182, 25));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setText(msg.getString("welcome") + " JGammon " + JGammon.VERSION);
        buttonTurn.setToolTipText(msg.getString("turnboard"));
        buttonTurn.setIcon(turnIcon);
        buttonTurn.addActionListener(new JGamFrame_buttonTurn_actionAdapter(
                this));
        buttonFlip.setToolTipText(msg.getString("flipboard"));
        actionManager.add(buttonFlip, "flipboard");
        actionManager.add(buttonTurn, "turnboard");
        buttonFlip.setIcon(flipIcon);
        buttonFlip.addActionListener(new JGamFrame_buttonFlip_actionAdapter(
                this));
        buttonHistory.setIcon(historyIcon);
        buttonHistory.setToolTipText(msg.getString("history"));
        actionManager.add(buttonHistory, "history");
        actionManager.add(buttonUndo, "undo");
        buttonUndo.setIcon(undoIcon);
        buttonUndo.setToolTipText(msg.getString("undo"));
        actionManager.add(buttonChat, "chat");
        buttonChat.setIcon(msgIcon);
        buttonChat.setToolTipText(msg.getString("chat"));

        actionManager.add(buttonHelp, "help");
        buttonHelp.setIcon(helpIcon);
        buttonHelp.setToolTipText(msg.getString("help"));

        buttonSave.setToolTipText(msg.getString("saveboard"));
        actionManager.add(buttonSave, "saveboard");
        buttonSave.setIcon(saveIcon);
        jMenuGame.setText(msg.getString("game_menu"));
        actionManager.add(jMenuItemNew, "newgame");
        jMenuItemNew.setText(msg.getString("newgame"));
        actionManager.add(jMenuItemDemo, "demo");
        jMenuItemDemo.setText(msg.getString("demo"));
        actionManager.add(jMenuItemAbort, "abort");
        jMenuItemAbort.setText(msg.getString("abort"));
        actionManager.add(jMenuItemSave, "saveboard");
        jMenuItemSave.setText(msg.getString("saveboard"));
        jMenuWindow.setText(msg.getString("window_menu"));
        jMenuItemHistory.setText(msg.getString("history"));
        actionManager.add(jMenuItemHistory, "history");
        actionManager.subscribeHandler("history", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showHistory();
            }
        });
        jMenuItemChat.setText(msg.getString("chat"));
        actionManager.add(jMenuItemChat, "chat");
        jMenuHelp.setText(msg.getString("help"));
        jMenuItem6.setText(msg.getString("help"));
        jMenuAbout.setText(msg.getString("about"));
        jMenuAbout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                about();
            }
        });
        jMenuDesign.setText(msg.getString("design_menu"));
        jMenuItem8.setText(msg.getString("turnboard"));
        jMenuItem8.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                turnBoard();
            }
        });
        jMenuItem9.setText(msg.getString("flipboard"));
        jMenuItem9.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                flipBoard();
            }
        });
        actionManager.add(jMenuItemUndo, "undo");
        jMenuItemUndo.setText(msg.getString("undo"));
        actionManager.add(jMenuItemExit, "close");
        jMenuItemExit.setText(msg.getString("exit"));
        jToolBarTop.setFloatable(true);
        actionManager.add(jMenuItemColorSwap, "swapcolors");
        jMenuItemColorSwap.setText(msg.getString("swapColors"));
        jMenuItemColorSwap.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                swapColors();
            }
        });
        actionManager.add(buttonSwap, "swapcolors");
        buttonSwap.setToolTipText(msg.getString("swapColors"));
        buttonSwap.setIcon(swapColorIcon);
        buttonSwap.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                swapColors();
            }
        });
        jMenuShowPreviewCheckers.setText(msg.getString("showpreviewcheckers"));
        jMenuShowPreviewCheckers.setSelected(true);
        jMenuEnableResizing.setText(msg.getString("enableresize"));
        jMenuEnableResizing.setEnabled(true);
        jMenuEnableResizing.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                jMenuEnableResizing_actionPerformed(e);
            }
        });
        jMenuBar.add(jMenuGame);
        jMenuBar.add(jMenuWindow);
        jMenuBar.add(jMenuDesign);
        jMenuBar.add(jMenuHelp);
        jMenuGame.add(jMenuItemNew);
        jMenuGame.add(jMenuItemDemo);
        jMenuGame.add(jMenuItemAbort);
        jMenuGame.add(jMenuItemSave);
        jMenuGame.addSeparator();
        jMenuGame.add(jMenuItemUndo);
        jMenuGame.add(jMenuItemExit);
        jMenuWindow.add(jMenuItemHistory);
        jMenuWindow.add(jMenuItemChat);
        jMenuWindow.addSeparator();
        jMenuWindow.add(jMenuItem8);
        jMenuWindow.add(jMenuItem9);
        jMenuWindow.add(jMenuItemColorSwap);
        jMenuHelp.add(jMenuItem6);
        jMenuHelp.add(jMenuAbout);
        jToolBarTop.add(buttonNew);
        jToolBarTop.add(buttonSave);
        jToolBarTop.add(component3);
        jToolBarTop.add(buttonHistory);
        jToolBarTop.add(buttonTurn);
        jToolBarTop.add(buttonFlip);
        jToolBarTop.add(buttonSwap);
        jToolBarTop.add(buttonChat);
        jToolBarTop.add(component1);
        jToolBarTop.add(buttonGiveUp);
        jToolBarTop.add(buttonUndo);
        jToolBarTop.add(buttonDouble);
        jToolBarTop.add(component2);
        jToolBarTop.add(buttonHelp);
        jToolBarTop.add(component4);
        jToolBarTop.add(label);
        jToolBarTop.add(buttonYield);
        jToolBarTop.add(buttonRoll);
        contentPane.add(board, java.awt.BorderLayout.CENTER);
        contentPane.add(jToolBarTop, java.awt.BorderLayout.NORTH);
        // actionManager.traceButtons();
    }

    public void about() {
        new JGamFrame_AboutBox(this).setVisible(true);
    }

    public void showHistory() {
        if (jGam.getGame() != null) {
            jGam.getGame().getHistory().showUp();
        }
    }

    public void turnBoard() {
        board.toggleTurn();
    }

    public void setLabel(String string) {
        label.setText(string);
        label.setToolTipText(string);
        labelPlayer = null;
    }

    /**
     * set the label to show the "Name's turn (color)" information
     *
     * @param player Player to set.
     */
    public void setLabelPlayer(Player player) {
        labelPlayer = player;
        updateLabel();
    }

    /**
     * once there is stores a labelPlayer, update the label according to this
     * player. As the color name or color may change, the label text may change
     * too! Dont do anything, if the label is not set to a player-notice.
     */
    private void updateLabel() {
        if (labelPlayer != null) {
            ResourceBundle gameRes = JGammon.getResources("jgam.msg.Player");
            MessageFormat msgFormat = new MessageFormat("");
            msgFormat.applyPattern(gameRes.getString("turn"));
            String color = getBoard().getColorName(labelPlayer.getNumber());
            String M = msgFormat.format(new Object[] { labelPlayer.getName(),
                    color });
            label.setText(M);
            label.setToolTipText(M);
            if (!labelPlayer.isLocal()) {
                setIcon(smallClock);
            } else {
                setIcon(board.getCheckerIcon(labelPlayer.getNumber()));
            }
        }
    }

    /**
     * set the icon for the status label. The icon is scaled down to a height of
     * 30.
     *
     * @param icon ImageIcon to be set or null.
     */
    public void setIcon(ImageIcon icon) {
        if (icon == null) {
            label.setIcon(null);
        } else {
            if (icon.getIconHeight() > 30) {
                Image smallerImage = icon.getImage().getScaledInstance(-1, 30,
                    Image.SCALE_SMOOTH);
                ImageIcon smallerIcon = new ImageIcon(smallerImage);
                label.setIcon(smallerIcon);
            } else {
                label.setIcon(icon);
            }
        }
    }

    public void flipBoard() {
        board.toggleLeftRight();
    }

    public BoardComponent getBoard() {
        return board;
    }

    /**
     * set the presence of a game. if none present: disable many buttons!
     *
     * @param present boolean
     */
    public void setGame(boolean present) {
        actionManager.setState("history", present);
        actionManager.setState("roll", present);
        actionManager.setState("yield", present);
        actionManager.setState("double", present);
        actionManager.setState("giveup", present);
        actionManager.setState("saveboard", present);
        actionManager.setState("undo", present);
        actionManager.setState("abort", present);
        actionManager.setState("swapcolors", present);
        actionManager.setState("flipboard", present);
        actionManager.setState("turnboard", present);
        actionManager.setState("chat", jGam.getGameConnection() != null);
    }

    public void closed() {
        jGam.handle("close");
    }

    /**
     * get the BoardImplementation that is currently selected from the available
     * implementations. This is not an initialized copy!
     *
     * @return an instance of the currently selected BoardImplementation,
     */
    private BoardImplementation getMenuBoardImplementation() {
        try {
            return JGammon.getBoardImplementations().get(
                designIndex);
        } catch (Exception ex) {
            throw new Error("There is no BoardImplementation available!", ex);
        }
    }

    /**
     * called when the design is changed in the menu
     *
     * @param e ItemEvent
     */
    private void designChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            JRadioButtonMenuItem item = (JRadioButtonMenuItem) e.getItem();
            designIndex = Integer.parseInt(item.getActionCommand());
        }
        BoardImplementation impl = getMenuBoardImplementation();
        board.setBoardImplementation(impl);
        Dimension d = board.getBoardImplementation().getPreferredSize();
        board.setSize(d);
        updateLabel();
        pack();
    }

    /**
     * set the design by index.
     *
     * the corresponding menu entry is selected.
     *
     * @param designIndex index to select.
     */
    public void setDesignIndex(int designIndex) {
        Enumeration<AbstractButton> en = buttonGroupDesigns.getElements();
        JRadioButtonMenuItem menu = null;
        String indexString = Integer.toString(designIndex);
        while (en.hasMoreElements()) {
            menu = (JRadioButtonMenuItem) en.nextElement();
            if (indexString.equals(menu.getActionCommand())) {
                break;
            }
        }
        menu.setSelected(true);
    }

    /**
     * create an icon from the given ressource-address.
     *
     * The complete ressource is "/jgam/img" + string
     *
     * ((Better use ClassLoader.getSystemResource, but JBuilder doesnt like it))
     *
     * @param string ressource to be searched
     * @return found ImageIcon
     */
    public static ImageIcon icon(String string) {
        return new ImageIcon(JGamFrame.class.getResource("/jgam/img/" + string));
    }

    void swapColors() {
        Game game = jGam.getGame();
        if (game != null) {
            board.toggleSwapColors();
            board.toggleTopBottom();
            updateLabel();
            repaint();
        }
    }

    public void frameResized(ComponentEvent e) {
        board.resizeEvent(e);
        board.setPreferredSize(board.getBoardImplementation().getSize());
        pack();
    }

    public void jMenuEnableResizing_actionPerformed(ActionEvent e) {
        boolean resizable = jMenuEnableResizing.isSelected();
        setResizable(resizable);
        if (resizable) {
            Dimension d = board.getBoardImplementation().getPreferredSize();
            board.setSize(d);
            board.setPreferredSize(d);
            pack();
        }
    }

    /**
     * use this <i>getter</i> method to query the value of the field
     * <code>actionManager</code>.
     *
     * @return the value of <code>actionManager</code>
     */
    public ActionManager getActionManager() {
        return this.actionManager;
    }

    /**
     * set the title plus a leading [!] if desired
     *
     * @param b boolean
     */
    public void setActiveTitle(boolean active) {
        setTitle(active ? "[!] JGammon" : "JGammon");
    }

    /**
     * check whether preview checkers on possible targets are to be painted.
     *
     * @return true iff preview is to be shown.
     */
    public boolean showPreviewCheckers() {
        return jMenuShowPreviewCheckers.isSelected();
    }

}

class JGamFrame_this_windowAdapter extends WindowAdapter {
    private final JGamFrame adaptee;

    JGamFrame_this_windowAdapter(JGamFrame adaptee) {
        this.adaptee = adaptee;
    }

    @Override
    public void windowClosing(WindowEvent e) {
        adaptee.closed();
    }
}

class JGamFrame_buttonFlip_actionAdapter implements ActionListener {
    private final JGamFrame adaptee;

    JGamFrame_buttonFlip_actionAdapter(JGamFrame adaptee) {
        this.adaptee = adaptee;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        adaptee.flipBoard();
    }
}

class JGamFrame_buttonTurn_actionAdapter implements ActionListener {
    private final JGamFrame adaptee;

    JGamFrame_buttonTurn_actionAdapter(JGamFrame adaptee) {
        this.adaptee = adaptee;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        adaptee.turnBoard();
    }
}
