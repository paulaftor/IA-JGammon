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



package jgam.net;

import java.io.*;
import java.util.*;

import javax.swing.JTable.PrintMode;

import jgam.game.*;

/**
 *
 * This is a remote Player using a JGammonConnection
 *
 * @author Mattias Ulbrich
 * @version 1.0
 */
public class JGammonNetPlayer extends Player implements ChannelListener {

    JGammonConnection connection;
    Writer writer;

    public JGammonNetPlayer(JGammonConnection connection) throws Exception {
        super(connection.getRemoteName());
        this.connection = connection;
        connection.addChannelListener(this);
        writer = connection.getChannelWriter(1);
    }

    /**
     * inform
     *
     * @param playerMessage PlayerMessage
     */
    @Override
    public void inform(PlayerMessage playerMessage) {
//        System.err.println("called inform on " + this +" for " + playerMessage);

        try {

            switch (playerMessage.getMessage()) {

            case DOUBLE:
                if (playerMessage.getOwner() != this) {
                    writer.write("DOUBLE\n");
                    writer.flush();
                }
                break;

                // ---------------------------------
            case DOUBLE_TAKEN:
                if (playerMessage.getOwner() != this) {
                    writer.write("TAKE\n");
                    writer.flush();
                }
                break;

                // ---------------------------------
            case DOUBLE_DROPPED:
                if (playerMessage.getOwner() != this) {
                    writer.write("DROP\n");
                    writer.flush();
                }
                break;

                // ---------------------------------
            case MOVE:
                if (playerMessage.getOwner() != this) {
                    SingleMove sm = (SingleMove) playerMessage.getObject();
                    writer.write("MOVE " + sm + "\n");
                    writer.flush();
                }
                break;

                // ---------------------------------
            case YIELD:
                if (playerMessage.getOwner() != this) {
                    writer.write("YIELD\n");
                    writer.flush();
                }
                break;

                // ---------------------------------
            case ROLL:
                if (playerMessage.getOwner() != this) {
                    writer.write("ROLL\n");
                    writer.flush();
                }
                break;

                // ---------------------------------
            case UNDO:
                if (playerMessage.getOwner() != this) {
                    writer.write("UNDO\n");
                    writer.flush();
                }
                break;

                // ---------------------------------
            case UNDO_REQUEST:
                writer.write("UNDO_REQUEST\n");
                writer.flush();
                break;

                // ---------------------------------
            case GIVEUP_REQUEST:
                writer.write("GIVEUP_REQUEST " + playerMessage.getValue() + "\n");
                writer.flush();
                break;

                // ---------------------------------
            case GIVEUP_TAKEN:
                writer.write("GIVEUP_TAKEN " + playerMessage.getValue() + "\n");
                writer.flush();
                break;

                // ---------------------------------
            case GIVEUP_DROPPED:
                writer.write("GIVEUP_DROPPED\n");
                writer.flush();
                break;

                // ---------------------------------
            case ABNORMAL_ABORT:
                String msgobj = (String) playerMessage.getObject();
                connection.close(msgobj);
                break;

//            default:
//                System.err.println("unhandled in NetPlayer: " + playerMessage);

            }
        } catch (Exception ex) {
            game.sendPlayerMessage(new PlayerMessage(this, PlayerMessage.MessageType.ABNORMAL_ABORT, ex));
        }
    }

    /**
     * a network player is not local.
     *
     * @return false
     */
    @Override
    public boolean isLocal() {
        return false;
    }

    /**
     * receiveChannelMessage.
     *
     * Parse the message stream and send corresponding message to game.
     *
     * @param channel only listen to 1 and 0
     * @param message the message
     */
    @Override
    public void receiveChannelMessage(int channel, String message) {
        if (channel == 0) {
            //
            // exception
            if (message.startsWith("EXCEPTION ")) {
                game.sendPlayerMessage(new PlayerMessage(this, PlayerMessage.MessageType.ABNORMAL_ABORT, new IOException(message)));
            }

            //
            // closed stream
            else if (message.equals("CLOSEDSTREAM")) {
                game.sendPlayerMessage(new PlayerMessage(this, PlayerMessage.MessageType.ABNORMAL_ABORT, new EOFException()));
            }

            //
            // stream is about to close
            else if (message.startsWith("CLOSING ")) {
                game.sendPlayerMessage(new PlayerMessage(this, PlayerMessage.MessageType.ABNORMAL_ABORT,
                        new EOFException(message.substring(8))));
            }

        } else if (channel == 1) {

            String submessages[] = message.split(" ");

            //
            // double
            if (message.equals("DOUBLE")) {
                game.sendPlayerMessage(new PlayerMessage(this, PlayerMessage.MessageType.DOUBLE));
            }

            else if (message.equals("ROLL")) {
                game.sendPlayerMessage(new PlayerMessage(this, PlayerMessage.MessageType.ROLL));
            }

            else if (message.equals("TAKE")) {
                game.sendPlayerMessage(new PlayerMessage(this, PlayerMessage.MessageType.DOUBLE_TAKEN));
            }

            else if (message.equals("DROP")) {
                game.sendPlayerMessage(new PlayerMessage(this, PlayerMessage.MessageType.DOUBLE_DROPPED));
            }

            else if (message.equals("UNDO")) {
                game.sendPlayerMessage(new PlayerMessage(this, PlayerMessage.MessageType.UNDO));
            }

            else if (submessages[0].equals("MOVE")) {
                SingleMove m = new SingleMove(getNumber(), submessages[1]);
                game.sendPlayerMessage(new PlayerMessage(this, PlayerMessage.MessageType.MOVE, m));
            }

            else if (submessages[0].equals("YIELD")) {
                game.sendPlayerMessage(new PlayerMessage(this, PlayerMessage.MessageType.YIELD));
            }

            else if (submessages[0].equals("GIVEUP_TAKEN")) {
                game.sendPlayerMessage(new PlayerMessage(this, PlayerMessage.MessageType.GIVEUP_TAKEN,
                        Integer.parseInt(submessages[1])));
            }

            else if (message.equals("UNDO_REQUEST")) {
                getOtherPlayer().inform(new PlayerMessage(this, PlayerMessage.MessageType.UNDO_REQUEST));
            }

            else if (submessages[0].equals("GIVEUP_REQUEST")) {
                getOtherPlayer().inform(new PlayerMessage(this, PlayerMessage.MessageType.
                        GIVEUP_REQUEST,
                        Integer.parseInt(submessages[1])));
            }

            else if (message.equals("GIVEUP_DROPPED")) {
                getOtherPlayer().inform(new PlayerMessage(this, PlayerMessage.MessageType.GIVEUP_DROPPED));
            }

        }
    }

    /**
     * called to "finish" the player so that it can close open connections to
     * deallocate resources, unsubsribe from handlers etc.
     *
     * Close connection and the writer.
     *
     */
    @Override
    public void dispose() {
        connection.close(null);
    }
}