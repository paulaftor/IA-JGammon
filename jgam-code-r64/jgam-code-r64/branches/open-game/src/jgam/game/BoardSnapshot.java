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

import jgam.util.Base64;


/**
 * An immutable ArrayBoardSetup.
 *
 * The content is fixed after the execution of the constructor
 * @author Mattias Ulbrich
 */
final public class BoardSnapshot extends ArrayBoardSetup {

    /**
     * to serialize objects of this class use this ID. 
     */
    private static final long serialVersionUID = -509352906796324898L;
    
    /**
     * The initial setup used by the game.
     */
    public static final BoardSnapshot INITIAL_SETUP =
            new BoardSnapshot(System.getProperty("jgam.initialboard"));

    /**
     * take a snapshot of a boardSetup.
     *
     * @param bs BoardSetup to be snapshot
     */
    public BoardSnapshot(BoardSetup bs) {
        super(bs);
    }

    /**
     * take a snapshot of a boardSetup.
     *
     * @param bs BoardSetup to be snapshot
     */
    public static BoardSnapshot takeSnapshot(BoardSetup bs) {
        return new BoardSnapshot(bs);
    }

    private final static String hexchars = "0123456789abcdef";

    /**
     * The initial board can also be set by a string argument
     */
    private BoardSnapshot(String initial) {
        super();
        if (initial == null) {
            checkers1[24] = checkers2[24] = 2;
            checkers1[13] = checkers2[13] = 5;
            checkers1[8] = checkers2[8] = 3;
            checkers1[6] = checkers2[6] = 5;
        } else {
            initial = initial.toLowerCase();
            for (int i = 0; i < 26; i++) {
                checkers1[i] = checkers2[i] = (byte) hexchars.indexOf(initial.charAt(i));
            }
      }
    }
    
    ////////////////////////////////////
    // Base64   coding
    ////////////////////////////////////

    /**
     * create a board from the base64 coded information about it.
     * @param base64coded String
     */
    public static BoardSetup decodeBase64(String base64coded) {
        byte[] data = Base64.decode(base64coded);
        ArrayBoardSetup ret = new BoardSnapshot(INITIAL_SETUP);

        // 1. the board
        int player1Total = 0, player2Total = 0;
        for (int i = 0; i < 24; i++) {
            if (data[i] > 0) {
                ret.checkers1[i + 1] = data[i];
                player1Total += data[i];
            } else {
                ret.checkers2[24 - i] = (byte) ( -data[i]);
                player2Total += -data[i];
            }
        }

        // 2. the bars
        ret.checkers1[25] = data[24];
        player1Total += data[24];
        ret.checkers2[25] = data[25];
        player2Total += data[25];

        // ==> the offs
        ret.checkers1[0] = (byte) (15 - player1Total);
        ret.checkers2[0] = (byte) (15 - player2Total);

        // 3. doubleCube
        ret.doubleCube = data[26];

        // 4. player at move
        ret.activePlayer = data[27];

        // 5. dice
        if (data[28] != -1) {
            ret.dice = new int[2];
            ret.dice[0] = (data[28] / 6) + 1;
            ret.dice[1] = (data[28] % 6) + 1;
        }

        return ret;
    }

    public static String encodeBase64(BoardSetup bs) {
        byte[] data = new byte[29];

        // 1. the board
        for (int i = 0; i < 24; i++) {
            data[i] = (byte) (bs.getPoint(1, i + 1) - bs.getPoint(2, 24 - i));
        }

        // 2. the bars
        data[24] = (byte) bs.getBar(1);
        data[25] = (byte) bs.getBar(2);

        // 3.+4. doubleCube and white's turn
        data[26] = (byte) bs.getDoubleCube();
        data[27] = (byte) bs.getActivePlayer();

        int[] dice = bs.getDice();
        if (dice == null) {
            data[28] = -1;
        } else {
            data[28] = (byte) ((dice[0] - 1) * 6 + dice[1] - 1);
        }

        return Base64.encode(data);
    }
}
