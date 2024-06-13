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

package jgam.game;

import java.io.Serializable;

/**
 * A BoardSetup implementation that simply stores the data in ints and arrays.
 * It is used by a couple of derived classes.
 *
 * Derived classes may alter the contents of these containers.
 *
 * Do such changes only in synchronized blocks. So a thread-safe mode can be
 * established.
 *
 * @author Mattias Ulbrich
 */
public class ArrayBoardSetup extends BoardSetup implements Serializable {
    
    /**
     * to serialize objects of this class use this ID. 
     */
    private static final long serialVersionUID = 6959949394622264379L;
    
    protected byte checkers1[] = new byte[26];
    protected byte checkers2[] = new byte[26];
    protected int activePlayer = 0;
    protected int doubleCube = 1;
    protected int doublePlayer = 0;
    protected int dice[] = null;

    /**
     * create an empty ArrayBoardSetup.
     * Arrays are initialized but not dice, and no checkers.
     */
    public ArrayBoardSetup() { }

    /**
     * initialize this object from another BoardSetup
     *
     * @param bs BoardSetup to setup from
     */
    protected ArrayBoardSetup(BoardSetup bs) {
        assert bs != null;
        synchronized(bs) {
            for (int i = 0; i < 26; i++) {
                checkers1[i] = (byte) bs.getPoint(1, i);
                checkers2[i] = (byte) bs.getPoint(2, i);
            }
            doubleCube = bs.getDoubleCube();
            dice = bs.getDiceCopy();

            activePlayer = bs.getActivePlayer();
            doublePlayer = bs.mayDouble(1) ? 1 : 2;
        }
    }

    /**
     * get the dice.
     *
     * @return int[] an array of length 2 and each element between 1 and 6
     *   or null
     */
    public int[] getDice() {
        return dice;
    }

    /**
     * get the value of the doubling cube.
     *
     * @return 1, 2, 4, 8, 16, 32, 64
     */
    public int getDoubleCube() {
        return doubleCube;
    }

    /**
     * get the number of the player who may make the next move.
     *
     * @return 1 for player1, 2 for player2, 0 if this is the initial setup
     */
    public int getActivePlayer() {
        return activePlayer;
    }

    /**
     * get the number of checkers on a specific point for a player.
     *
     * @param player Player to check for (1 or 2)
     * @param pointnumber Point to check (1-24 for points, 0 for off, 25 for
     *   bar)
     * @return a value between 0 and 15
     */
    public int getPoint(int player, int pointnumber) {
        return player == 1 ? checkers1[pointnumber] : checkers2[pointnumber];
    }

    /**
     * may a player double the game value.
     *
     * @param playerno player to check
     * @return true iff playerno may double when its his turn
     */
    public boolean mayDouble(int playerno) {
        return (doubleCube == 1 || playerno == doublePlayer) && doubleCube < 64;
    }

    /**
     * Creates and returns a copy of this object.
     *
     * Copy checker-board, dices, cube and activeplayer info
     *
     * @return a clone of this instance.
     */
    public Object clone() throws CloneNotSupportedException {

        ArrayBoardSetup ret = (ArrayBoardSetup) super.clone();
        ret.checkers1 = (byte[])checkers1.clone();
        ret.checkers2 = (byte[])checkers2.clone();
        ret.dice = dice == null ? null : (int[])dice.clone();
        ret.doubleCube = doubleCube;
        ret.activePlayer = activePlayer;
        ret.doublePlayer = doublePlayer;

        return ret;
    }


}
