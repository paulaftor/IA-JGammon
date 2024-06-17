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

package jgam.ai;

import jgam.game.BoardSetup;
import jgam.game.MoveChain;
import jgam.game.PossibleMoves;
import util.AverageBuffer;

import javax.swing.*;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

// source 1.5

/**
 *
 * Use table lookup for the last phase of the game, the bearoff:
 *
 * All ca 55000 positions are loaded into a hashtable.
 *
 * Values are stored in a short with fixed decimal point.
 * It is
 *    floatval = intval / 256f.
 *    intval = (int)(floatval * 256f)
 *
 * @author Mattias Ulbrich
 * @version 1.0
 */
public class BearOff12AI extends RoundEstimatingAI {

    private HashMap<Integer, Float> table;
    private int count;

    public static final String FILENAME = "bearoff12.dat";
    public static final int SIZE = 17383862;
    public static final int RANGE = 12;

    public BearOff12AI() {
    }

    /**
     * free all used resources.
     *
     */
    public void dispose() {
        table = null;
    }

    /**
     * get a short description of this method.
     *
     * @return String
     */
    public String getDescription() {
        return "BearOff12 AI - can be used in end games";
    }

    /**
     * get the name of this AI Method.
     *
     * @return String
     */
    public String getName() {
        return "BearOff12 AI";
    }

    /*public Map<Long, Float> getTable() {
        return Collections.unmodifiableMap(table);
    }*/
    /**
     * initialize this instance.
     *
     * load the table from disk.
     *
     * @throws Exception if sth goes wrong during init.
     */
    public void init() throws Exception {
        
        InputStream is = getClass().getResourceAsStream(FILENAME);
        if(is == null) {
            throw new IOException("The bearoff database cannot be found: " + FILENAME);
        }
        DataInputStream dis = new DataInputStream(is);
        
        table = new HashMap<>();
        table.put((int) 0l, 0.0f);
        
        for (long print = 1; print <= 0x00f00000000000l; print = nextPrint(print)) {

            // check whether print is a valid print:
            // only 15 checkers coded.
            // else go for next round.
            int checksum = 0;
            for (int j = 1; j <= RANGE; j++) {
                checksum += getPoint(print, j);
            }
            if (checksum > 15) {
                continue;
            }

            int read = dis.readShort();

            float floatval = read / 256f;
            table.put((int) print, floatval);
        }

        dis.close();
    }

    /**
     * given a board make decide which moves to make.
     *
     * simply look up in the table;
     *
     * if the players are not separated yet, minimize blots first
     *
     * @todo better un-separated case!
     * @param boardSetup BoardSetup to evaluate
     * @return SingleMove[] a complete set of moves.
     */
    public MoveChain makeMoves(BoardSetup boardSetup) throws CannotDecideException {

        boolean separated = boardSetup.isSeparated();
        int player = boardSetup.getPlayerAtMove();

        if (boardSetup.getMaxPoint(player) > 6) {
            throw new CannotDecideException("max point > 6");
        }

        float bestEstimate = 1000;
        int bestIndex = -1;

        PossibleMoves pm = new PossibleMoves(boardSetup);

        List<BoardSetup> list = pm.getPossbibleNextSetups();
        int index = 0;
        for(BoardSetup setup : list) {
            int fingerprint = makePrint(setup, player);
            float value;
            value = lookup(fingerprint);
            if (!separated) {
                // if not sep. take the number of blots as the major argument!
                // 20 > max entry in the database.
                value += countBlots(boardSetup) * 20;
            }
            if (value < bestEstimate) {
                bestEstimate = value;
                bestIndex = index;
            }
        }

        if (bestIndex == -1) {
            return MoveChain.EMPTY;
        } else {
//            System.out.println("Estimated number of throws for this move: " + bestEstimate);
            return pm.getMoveChain(bestIndex);
        }
    }

    /**
     * count blots for active player
     *
     * @param boardSetup BoardSetup
     * @return number of blots
     */
    private static int countBlots(BoardSetup boardSetup) {
        int cnt = 0;
        for (int i = 1; i <= 24; i++) {
            if (boardSetup.getPoint(boardSetup.getPlayerAtMove(), i) == 1) {
                cnt++;
            }
        }
        return cnt;
    }

    /**
     * look up a fingerprint in the table.
     *
     * @param fingerprint fingerprint
     * @return int value from table converted to float
     */
    long b = 0;
    int bc = 0;
    public float lookup(long fingerprint) {
        try {
            //long t0 = System.currentTimeMillis();
            Float result = table.get(fingerprint);
          /*  b += System.currentTimeMillis() - t0;
            if(bc++ == 5000000) {
                System.out.println("Mean lookup " + (b/5000000.));
                bc = 0; b= 0;
            }*/
            return result;
        } catch (NullPointerException ex) {
            System.err.println("Unknown print in table: " + Long.toHexString(fingerprint));
            throw ex;
        }
    }

    /**
     * make finger print of a bearoff situation.
     *
     * @param boardSetup BoardSetup
     * @return a value between 0 and 0x00ffffff.
     */
    public static int makePrint(BoardSetup boardSetup, int player) {

        assert boardSetup.getMaxPoint(player) <= 6:
                "Not all at home";
        int ret = 0;
        for (int i = 1; i <= RANGE; i++) {
            ret |= boardSetup.getPoint(player, i) << ((i - 1) * 4);
        }

        return ret;
    }


    /**
     * get the next integer that describes a board via finger print.
     *
     * @param print int
     * @return int
     */
    public static long nextPrint(long print) {
        int quer = 0;

        for (int i = 0; i < RANGE; i++) {
            quer += (print >> (i * 4)) & 0xf;
        }

        if (quer != 15) {
            print ++;
        } else {
            for (int i = 1; i <= RANGE; i++) {
                if (getPoint(print, i) != 0) {
                    print = setPoint(print, i, 0);
                    print = setPoint(print, i+1, getPoint(print, i+1)+1);
                    break;
                }
            }
        }
        return print;

    }

    /**
     * make up the bearoff table!
     *
     * Put in the empty board with an estimated 0 rounds.
     */
    private short[] makeTable(ProgressMonitor progress) {
        table = new HashMap<>();

        // return the entries in an array
        short[] ret = new short[SIZE];
        count = 0;

        long start = System.currentTimeMillis();

        // thats the base case
        table.put((int) 0l, 0.0f);

        for (long print = 1; print <= 1l << RANGE*4; print = nextPrint(print)) {
            if (count % 100000 == 0) {
                long ms = (System.currentTimeMillis() - start);
                System.out.println(Long.toHexString(print) +
                        " " + ((float) print) /  0x00f00000000000l +
                        " " + count + " " + ms/1000 + " " + (1000.*count) / ms);
            }

            // check whether this is a valid print:
            // only 15 checkers coded.
            // else go for next round.
            int checksum = 0;
            for (int j = 1; j <= 6; j++) {
                checksum += getPoint(print, j);
            }
            if (checksum > 15) {
                throw new Error("should not be reached. If so remove this check");
            }

            float floatval = eval(print);
            ret[count++] = (short) (floatval * 256);
            table.put((int) print, floatval);
        }
        return ret;
    }

    /**
     * get the estimated number of turns needed to play out all checkers.
     *
     * If this is not a bearoff situation throw a CannotDecideException
     *
     * @param setup BoardSetup
     * @param player int
     * @return float
     * @throws CannotDecideException if this is not a Bearoffsituation
     */
    public float getEstimatedMoves(BoardSetup setup, int player) throws CannotDecideException{
        boolean athome = setup.getMaxPoint(player) <= 6;
        if (athome) {
            return lookup(makePrint(setup, player));
        } else {
            throw new CannotDecideException();
        }
    }

    /**
     * evaluate a print by looking up all possible consecutive positions.
     *
     * expactation over all possible dices.
     *
     * @param print fingerprint
     * @return expactation value
     */
    private float eval(long print) {
        float estimate = 0;

        // normal throws:
        int dice[] = new int[2];
        for (dice[0] = 1; dice[0] <= 6; dice[0]++) {
            for (dice[1] = 1; dice[1] < dice[0]; dice[1]++) {
                estimate += (2. / 36.) * (eval(print, dice, 2) + 1);
            }
        }

        // doubles:
        dice = new int[4];
        for (int d = 1; d <= 6; d++) {
            dice[0] = dice[1] = dice[2] = dice[3] = d;
            estimate += (1. / 36.) * (eval(print, dice, 4) + 1);
        }

        return estimate;
    }

    /**
     * eval all possible following positions and take the best
     *
     * @param print finger print
     * @param dice avaiable dice
     * @param movestomake how many dice values are left
     * @return estimation for the best next position
     */
    private float eval(long print, int[] dice, int movestomake) {

        // no more moves --> eval print
        // end of game --> eval prin
        if (movestomake == 0 || print == 0) {
            return lookup(print);
        }

        movestomake--;

        // current dice
        int d = dice[movestomake];

        // is d more than the maxpoint?
        // one checker on the d-point: point(d, 1)
        // ---> d = maxpoint
        while (print < point(d, 1)) {
            d--;
        }

        float bestval = 1000;
        for (int p = d; p <= 6; p++) {
            if ((print & point(p, 0xf)) != 0) {
                // remove one checker from point p
                long nextprint = setPoint(print, p, getPoint(print, p) - 1);
                // and possibly add it to p-d
                if (p != d) {
                    nextprint = setPoint(nextprint, p - d, getPoint(nextprint, p - d) + 1);
                }
                float v = eval(nextprint, dice, movestomake);
                if (v < bestval) {
                    bestval = v;
                }
            }
        }

        return bestval;
    }

    /*
     * value checkers on point d
     */
    private static long point(int d, int value) {
        return ((long)value) << ((d - 1) * 4);
    }

    private static long setPoint(long print, int d, int value) {
        // blank out d
        print &= ~point(d, 0xf);
        return print + point(d, value);
    }

    private static int getPoint(long print, int d) {
        return (int)((print >> ((d - 1) * 4)) & 0xf);
    }

    public static void main(String[] args) throws IOException {
        System.out.println("Calculating bear off database:");
        BearOff12AI ai = new BearOff12AI();
        short values[] = ai.makeTable(null);

        if (values != null) {
            ai.saveTable(args.length == 0 ? FILENAME : args[0], values);
        }
    }

    /**
     * saveTable
     */
    private void saveTable(String file, short[] values) throws IOException {
        FileOutputStream os = new FileOutputStream(file);
        BufferedOutputStream bos = new BufferedOutputStream(os);
        DataOutputStream dos = new DataOutputStream(bos);
        assert values.length == SIZE;

        for (int i = 0; i < count; i++) {
            dos.writeShort(values[i]);
        }
        dos.close();
    }
}
