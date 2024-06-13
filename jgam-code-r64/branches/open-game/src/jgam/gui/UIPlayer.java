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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import jgam.JGammon;
import jgam.board.BoardMouseListener;
import jgam.game.BoardSetup;
import jgam.game.Game;
import jgam.game.MoveChain;
import jgam.game.Player;
import jgam.game.PlayerMessage;
import jgam.game.PossibleMoves;
import jgam.game.SingleMove;
import jgam.util.ActionManager;

//source 1.5

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
public class UIPlayer extends Player implements ActionListener {

    // message formatting
    private final ResourceBundle msg = JGammon.getResources("jgam.msg.Player");
    // no longer needed? private MessageFormat msgFormat = new MessageFormat("");

    // Action Manager
    private final ActionManager actionManager;

    // provides access to most of th UI
    private final JGammon jgam;

    /**
     * construct a new UIPlayer.
     * @param name Name of the playr
     * @param jgam underlying JGammon
     * @param reporting iff this player is to output infos in the frame
     */
    public UIPlayer(String name, JGammon jgam) throws Exception {
        super(name);
        this.jgam = jgam;

        actionManager = jgam.getFrame().getActionManager();
        actionManager.subscribeHandler("roll", this);
        actionManager.subscribeHandler("yield", this);
        actionManager.subscribeHandler("double", this);
        actionManager.subscribeHandler("undo", this);
        actionManager.subscribeHandler("giveup", this);
    }

    /**
     * inform
     *
     * @param playerMessage PlayerMessage
     */
    @Override
    synchronized public void inform(final PlayerMessage playerMessage) {
//        System.err.println("called inform on " + this +" for " + playerMessage);

        switch (playerMessage.getMessage()) {

            // ---------------------------------
        case MY_TURN:
            if (playerMessage.getOwner() == this) {
                actionManager.enable("roll");
                if(game.getGameBoard().mayDouble(getNumber())) {
                    actionManager.enable("double");
                }
            }
            break;

            // ---------------------------------
        case DOUBLE:
            if(playerMessage.getOwner() != this) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        acceptDouble();
                    }
                });
                actionManager.disable("double");
            }
            break;

            // ---------------------------------
        case UNDO_REQUEST:
            if (isCurrentPlayer()) {
                game.sendPlayerMessage(new PlayerMessage(this, PlayerMessage.MessageType.UNDO));
            }
            break;

            // ---------------------------------
        case GIVEUP_REQUEST:
            if (acceptGiveup(playerMessage.getValue())) {
                game.sendPlayerMessage(new PlayerMessage(this, PlayerMessage.MessageType.GIVEUP_TAKEN, playerMessage.getValue()));
            } else {
                getOtherPlayer().inform(new PlayerMessage(this, PlayerMessage.MessageType.GIVEUP_DROPPED));
            }

            break;

            // ---------------------------------
        case GIVEUP_DROPPED:
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JOptionPane.showMessageDialog(jgam.getFrame(),
                            msg.getString("giveupdropped"));
                }
            });
            break;

            // ---------------------------------
//        default:
//            System.err.println("  ... which is an unhandled message");
        }

    }

    private boolean acceptGiveup(int level) {

        // bugfix: first switch off input if at move
        ((BoardMouseListener)JGammon.jgammon().getBoard().getMouseListeners()[0]).drop();

        String[] winlevels = msg.getString("winlevels").split(", *");
        String mess = MessageFormat.format(msg.getString("confirmGiveup"),
                      new Object[] {getOtherPlayer().getName(), winlevels[level - 1]});
        return JOptionPane.showConfirmDialog(jgam.getFrame(),
                mess,
                msg.getString("confirm"),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE) ==
                JOptionPane.YES_OPTION;

    }

    private void acceptDouble() {
        String M = MessageFormat.format(msg.getString("confirmDouble"),
                   new Object[] {
                   getOtherPlayer().getName(),
                   new Integer(2 * game.getGameBoard().getDoubleCube())});

        boolean answer = JOptionPane.showConfirmDialog(jgam.getFrame(),
                         M,
                         msg.getString("confirm"),
                         JOptionPane.YES_NO_OPTION,
                         JOptionPane.QUESTION_MESSAGE,
                         jgam.getBoard().getCheckerIcon(getNumber())) ==
                         JOptionPane.YES_OPTION;

        if(answer) {
            game.sendPlayerMessage(new PlayerMessage(this, PlayerMessage.MessageType.DOUBLE_TAKEN));
        } else {
            game.sendPlayerMessage(new PlayerMessage(this, PlayerMessage.MessageType.DOUBLE_DROPPED));
        }
    }

    /**
     * this player is local!
     */
    @Override
    public boolean isLocal() {
        return true;
    }

    /**
     * waitingForMoves
     *
     * @return boolean
     */
    public boolean isWaitingForUIMove() {
        return game.getCurrentPlayer() == this && game.getState() == Game.STATE_AT_MOVE;
    }

    /**
     * handleMove
     *
     * @param object Object
     */
    synchronized public void handleMove(SingleMove move) {
        PlayerMessage pm = new PlayerMessage(this, PlayerMessage.MessageType.MOVE);
        pm.setObject(move);
        game.sendPlayerMessage(pm);
    }

    /**
     * getPossibleMovesFrom returns a List with all Moves that are possible
     * from point on given the current remaining hops.
     *
     * @param point point to start
     * @return List of Move-Objects
     */
    public Set<MoveChain> getPossibleMovesFrom(int point) {
//        PossibleMoves pm = new PossibleMoves(game.getGameBoard(),
//                           game.getGameBoard().getDicesObject().getSteps());
//
//        return pm.getPossibleMoveChainsFrom(point);
        Set<MoveChain> res = new HashSet<MoveChain>();
        int[] pegs = game.getGameBoard().getBoardAsArray(getOtherPlayer().getNumber());
        for (int i = 0; i < 24; i++) {
            if(i != point && (i == 0 || pegs[25-i] <= 1)) {
                res.add(new MoveChain(new SingleMove(getNumber(), point, i, false)));
            }
        }
        return res;
    }

    /**
     * when clicking on a point twice do this move.
     *
     * This is the longest one hop possible.
     * @param point int
     * @return Move
     */
    public SingleMove getDoubleClickMove(int point) {
        PossibleMoves pm = new PossibleMoves(game.getGameBoard(),
                           game.getGameBoard().getDicesObject().getSteps());
        SingleMove longest = null;

        for(MoveChain chain : pm.getPossibleMoveChains()) {
            SingleMove m = chain.get(0);
            if (m.from() == point && (longest == null || longest.to() > m.to())) {
                longest = m;
            }
        }

        return longest;
    }

    /**
     * called to "finish" the player so that it can close open connections to
     * deallocate resources, unsubsribe from handlers etc.
     *
     * Here: unsubscribe from the buttons.
     *
     */
    @Override
    public void dispose() {
        jgam.getFrame().getActionManager().removeHandler(this);
    }

    /**
     * react to button presses in the frame
     * @param e event that has been issued.
     */
    @Override
    public void actionPerformed(ActionEvent e) {

        if (e.getActionCommand().equals("roll") && isCurrentPlayer()) {
            PlayerMessage pm = new PlayerMessage(this, PlayerMessage.MessageType.ROLL);
            game.sendPlayerMessage(pm);
            actionManager.enable("yield");
        } else

        if (e.getActionCommand().equals("double") && isCurrentPlayer()) {
            if (game.getGameBoard().mayDouble(getNumber())) {
                PlayerMessage pm = new PlayerMessage(this, PlayerMessage.MessageType.DOUBLE);
                game.sendPlayerMessage(pm);
            } else {
                JOptionPane.showMessageDialog(jgam.getFrame(),
                        msg.getString("doublenotallowed"));

            }
        } else

        if (e.getActionCommand().equals("undo") && game.getUndoPlayer() == this) {
            if (game.getState() == Game.STATE_AT_MOVE) {
                PlayerMessage pm = new PlayerMessage(this, PlayerMessage.MessageType.UNDO);
                game.sendPlayerMessage(pm);
            } else {
                getOtherPlayer().inform(new PlayerMessage(this, PlayerMessage.MessageType.UNDO_REQUEST));
            }
        } else

        if (e.getActionCommand().equals("giveup") && (!getOtherPlayer().isLocal() || isCurrentPlayer())) {
            int level = showGiveUpWindow();
            if (level != 0) {
                getOtherPlayer().inform(new PlayerMessage(this, PlayerMessage.MessageType.GIVEUP_REQUEST, level));
            }
        } else

        if (e.getActionCommand().equals("yield") && isCurrentPlayer()) {
            PlayerMessage pm = new PlayerMessage(this, PlayerMessage.MessageType.YIELD);
            actionManager.disable("yield");
            game.sendPlayerMessage(pm);
        }
    }

    private int showGiveUpWindow() {
        String[] winlevels = msg.getString("winlevels").split(", *");
        String answer = (String)
                        JOptionPane.showInputDialog(jgam.getFrame(),
                        msg.getString("proposeGiveup"),
                        msg.getString("choose"),
                        JOptionPane.PLAIN_MESSAGE,
                        jgam.getBoard().getCheckerIcon(getNumber()),
                        winlevels, winlevels[0]);
        for (int i = 0; i < winlevels.length; i++) {
            if (winlevels[i].equals(answer)) {
                return i + 1;
            }
        }

        return 0;
    }


}
