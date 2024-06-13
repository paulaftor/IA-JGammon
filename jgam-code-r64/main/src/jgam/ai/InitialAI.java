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

import java.util.*;

import jgam.game.*;

/**
 * Very simple rules for an initial AI
 * 
 * @author Mattias Ulbrich
 * @version 1.0
 */
public class InitialAI implements AI {
    public InitialAI() {
    }

    /**
     * get a short description of this method.
     * 
     * @return String
     */
    public String getDescription() {
        return "Poor initial player";
    }

    /**
     * get the name of this AI Method.
     * 
     * @return String
     */
    public String getName() {
        return "InitialAI";
    }

    public void init() {
    }

    public void dispose() {
    }

    /**
     * given a board and a double offer, take or drop.
     * 
     * just always take ...
     * 
     * @param boardSetup BoardSetup
     * @return TAKE
     */
    public int takeOrDrop(BoardSetup boardSetup) {
        return TAKE;
    }

    public MoveChain makeMoves(BoardSetup boardSetup) {

        double bestValue = Double.NEGATIVE_INFINITY;
        int bestIndex = -1;

        PossibleMoves pm = new PossibleMoves(boardSetup);
        List list = pm.getPossbibleNextSetups();
        int index = 0;
        for (Iterator iter = list.iterator(); iter.hasNext(); index++) {
            BoardSetup setup = (BoardSetup) iter.next();
            double value = eval(setup);
            if (value > bestValue) {
                bestValue = value;
                bestIndex = index;
            }
        }

        if (bestIndex == -1)
            return MoveChain.EMPTY;
        else {
            // Sy stem.out.println("Evaluation for this move: "+bestValue);
            return pm.getMoveChain(bestIndex);
        }
    }

    private double eval(BoardSetup setup) {
        int blots = 0;
        int holding = 0;
        int block = 0;
        int player = setup.getPlayerAtMove();

        // xx block --> 1 point
        // xxx block --> 3 points
        // xxxx --> 6
        // xxxxx --> 10
        // xxxxxx --> 15
        // xxxxxxxxx... --> 15

        int curBlock = 0;
        for (int i = 1; i <= 24; i++) {

            int p = setup.getPoint(player, i);
            if (p == 1) {
                blots++;
                curBlock = 0;
            } else if (p >= 2) {
                holding++;
                block += curBlock;
                curBlock ++;
            } else {
                curBlock = 0;
            }
        }

        int hisbar = setup.getBar(3 - player);

        double v = setup.calcPip(3 - player) / 100.;
        //v += -blots * .5;
        v -= Hits.computeExpectedBarIncrease(setup) * 2;
        v += holding * .2;
        v += block * .2;
        v += hisbar * .5;


        return v;

    }

    public int rollOrDouble(BoardSetup boardSetup) throws CannotDecideException {
        // TODO Auto-generated method stub
        return ROLL;
    }
}
