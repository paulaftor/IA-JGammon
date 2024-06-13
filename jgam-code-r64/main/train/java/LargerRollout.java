///*
// * JGammon: A backgammon client written in Java
// * Copyright (C) 2005/06 Mattias Ulbrich
// *
// * JGammon includes: - playing over network
// *                   - plugin mechanism for graphical board implementations
// *                   - artificial intelligence player
// *                   - plugin mechanism for AI players
// *
// * This program is free software; you can redistribute it and/or
// * modify it under the terms of the GNU General Public License
// * as published by the Free Software Foundation; either version 2
// * of the License, or (at your option) any later version.
// *
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License
// * along with this program; if not, write to the Free Software
// * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
// */
//
//import java.io.DataInputStream;
//import java.io.DataOutputStream;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import javax.swing.ProgressMonitor;
//
//import jgam.ai.CannotDecideException;
//import jgam.ai.RoundEstimatingAI;
//import jgam.game.BoardSetup;
//import jgam.game.BoardSnapshot;
//import jgam.game.MoveChain;
//import jgam.game.PossibleMoves;
//
//// source 1.5
//
///**
// *
// * Use table lookup for the last phase of the game, the bearoff:
// *
// * All ca 55000 positions are loaded into a hashtable.
// *
// * Values are stored in a short with fixed decimal point.
// * It is
// *    floatval = intval / 256f.
// *    intval = (int)(floatval * 256f)
// *
// * @author Mattias Ulbrich
// * @version 1.0
// */
//public class LargerRollout {
//
//    private HashMap<String, Float> table;
//
//    /**
//     * given a board make decide which moves to make.
//     *
//     * simply look up in the table;
//     *
//     * if the players are not separated yet, minimize blots first
//     *
//     * @todo better un-separated case!
//     * @param boardSetup BoardSetup to evaluate
//     * @return SingleMove[] a complete set of moves.
//     */
//    public MoveChain makeMoves(BoardSetup boardSetup) throws CannotDecideException {
//
//        boolean separated = boardSetup.isSeparated();
//        int player = boardSetup.getPlayerAtMove();
//
//        if (boardSetup.getMaxPoint(player) > 6) {
//            throw new CannotDecideException("max point > 6");
//        }
//
//        float bestEstimate = 1000;
//        int bestIndex = -1;
//
//        PossibleMoves pm = new PossibleMoves(boardSetup);
//
//        List<BoardSetup> list = pm.getPossbibleNextSetups();
//        int index = 0;
//        for(BoardSetup setup : list) {
//            int fingerprint = makePrint(setup, player);
//            float value;
//            value = lookup(fingerprint);
//            if (!separated) {
//                // if not sep. take the number of blots as the major argument!
//                // 20 > max entry in the database.
//                value += countBlots(boardSetup) * 20;
//            }
//            if (value < bestEstimate) {
//                bestEstimate = value;
//                bestIndex = index;
//            }
//        }
//
//        if (bestIndex == -1) {
//            return MoveChain.EMPTY;
//        } else {
////            System.out.println("Estimated number of throws for this move: " + bestEstimate);
//            return pm.getMoveChain(bestIndex);
//        }
//    }
//
//    /**
//     * look up a fingerprint in the table.
//     *
//     * @param fingerprint fingerprint
//     * @return int value from table converted to float
//     */
//    public float lookup(BoardSetup bs) {
//        return table.get(BoardSnapshot.encodeBase64(bs));
//    }
//
//    /**
//     * make finger print of a bearoff situation.
//     *
//     * @param boardSetup BoardSetup
//     */
//    public static String makePrint(BoardSetup boardSetup) {
//        return BoardSnapshot.encodeBase64(boardSetup);
//    }
//
//    /**
//     * make up the bearoff table!
//     *
//     * Put in the empty board with an estimated 0 rounds.
//     */
//    private void makeTable(ProgressMonitor progress) {
//        table = new HashMap<String, Float>();
//        table.put(makePrint(BoardSnapshot.INITIAL_SETUP), 0.0f);
//        return ret;
//    }
//
//    /**
//     * get the estimated number of turns needed to play out all checkers.
//     *
//     * If this is not a bearoff situation throw a CannotDecideException
//     *
//     * @param setup BoardSetup
//     * @param player int
//     * @return float
//     * @throws CannotDecideException if this is not a Bearoffsituation
//     */
//    public float getEstimatedMoves(BoardSetup setup, int player) throws CannotDecideException{
//        boolean athome = setup.getMaxPoint(player) <= 6;
//        if (athome) {
//            return lookup(makePrint(setup, player));
//        } else {
//            throw new CannotDecideException();
//        }
//    }
//
//    /**
//     * evaluate a print by looking up all possible consecutive positions.
//     *
//     * expactation over all possible dices.
//     *
//     * @param print fingerprint
//     * @return expactation value
//     */
//    private float eval(int print) {
//        float estimate = 0;
//
//        // normal throws:
//        int dice[] = new int[2];
//        for (dice[0] = 1; dice[0] <= 6; dice[0]++) {
//            for (dice[1] = 1; dice[1] < dice[0]; dice[1]++) {
//                estimate += (2. / 36.) * (eval(print, dice, 2) + 1);
//            }
//        }
//
//        // doubles:
//        dice = new int[4];
//        for (int d = 1; d <= 6; d++) {
//            dice[0] = dice[1] = dice[2] = dice[3] = d;
//            estimate += (1. / 36.) * (eval(print, dice, 4) + 1);
//        }
//
//        return estimate;
//    }
//
//    /**
//     * eval all possible following positions and take the best
//     *
//     * @param print finger print
//     * @param dice avaiable dice
//     * @param movestomake how many dice values are left
//     * @return estimation for the best next position
//     */
//    private float eval(int print, int[] dice, int movestomake) {
//
//        // no more moves --> eval print
//        // end of game --> eval prin
//        if (movestomake == 0 || print == 0) {
//            return lookup(print);
//        }
//
//        movestomake--;
//
//        // current dice
//        int d = dice[movestomake];
//
//        // is d more than the maxpoint?
//        // one checker on the d-point: point(d, 1)
//        // ---> d = maxpoint
//        while (print < point(d, 1)) {
//            d--;
//        }
//
//        float bestval = 1000;
//        for (int p = d; p <= 6; p++) {
//            if ((print & point(p, 0xf)) != 0) {
//                // remove one checker from point p
//                int nextprint = setPoint(print, p, getPoint(print, p) - 1);
//                // and possibly add it to p-d
//                if (p != d) {
//                    nextprint = setPoint(nextprint, p - d, getPoint(nextprint, p - d) + 1);
//                }
//                float v = eval(nextprint, dice, movestomake);
//                if (v < bestval) {
//                    bestval = v;
//                }
//            }
//        }
//
//        return bestval;
//    }
//
//    /*
//     * value checkers on point d
//     */
//    private static int point(int d, int value) {
//        return value << ((d - 1) * 4);
//    }
//
//    private static int setPoint(int print, int d, int value) {
//        // blank out d
//        print &= ~point(d, 0xf);
//        return print + point(d, value);
//    }
//
//    private static int getPoint(int print, int d) {
//        return (print >> ((d - 1) * 4)) & 0xf;
//    }
//
//    public static void main(String[] args) throws IOException {
//        System.out.println("Calculating bear off database:");
//        LargerRollout ai = new LargerRollout();
//        short values[] = ai.makeTable(null);
//
//        if (values != null) {
//            ai.saveTable(args.length == 0 ? FILENAME : args[0], values);
//        }
//    }
//
//    /**
//     * saveTable
//     */
//    private void saveTable(String file, short[] values) throws IOException {
//        FileOutputStream os = new FileOutputStream(file);
//        DataOutputStream dos = new DataOutputStream(os);
//        assert values.length == SIZE;
//
//        for (int i = 0; i < values.length; i++) {
//            dos.writeShort(values[i]);
//        }
//        dos.close();
//    }
//}
