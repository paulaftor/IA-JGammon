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

import java.util.List;

import jgam.game.BoardSetup;
import jgam.game.MoveChain;
import jgam.game.PossibleMoves;
import jgam.game.SingleMove;

/**
 * RoundEstimatingAI is u
 * 
 * @author Mattias Ulbrich
 * @version 1.0
 */
public abstract class RoundEstimatingAI extends EvaluatingAI {

    public RoundEstimatingAI() {
    }

    /**
     * please see documentation/bearoff.png for details for this function.
     * 
     * calculate the estimated throws needed to finish for both players. Then
     * use the approximation for the win probability based on these data.
     * 
     * The formula is described in documentation/raceapprox.html.
     * 
     * In the case that the player at move will surely win with the next move,
     * return 1.0.
     * 
     * If the setup is not separated throw a CannotDecideException because these
     * calculations are no longer valid then.
     * 
     * @param setup BoardSetup to evaluate
     * @return the probability that the player at move wins this game.
     */
    public double probabilityToWin(BoardSetup setup)
            throws CannotDecideException {

        if (!setup.isSeparated()) {
            throw new CannotDecideException("The setup is not separated");
        }

        float est1 = getEstimatedMoves(setup, setup.getPlayerAtMove());

        // I win if guaranteed that I only need one move
        if (est1 <= 1f)
            return 1.0;

        float est2 = getEstimatedMoves(setup, 3 - setup.getPlayerAtMove());

        double arg = -0.0315 * est1 * est1 + 0.0230 * est2 * est2 + 3.5864
                * est1 / est2 + 1.3112 * est1 - 1.0800 * est2 - 5.6852;

        return 1 / (1 + Math.exp(arg));
    }

    /**
     * get the estimated number of turns needed to play out all checkers.
     * 
     * @param setup BoardSetup
     * @param player int
     * @return float
     */
    public abstract float getEstimatedMoves(BoardSetup setup, int player)
            throws CannotDecideException;

    /**
     * given a board make decide which moves to make.
     * 
     * Ask the neural net for the setup with fewest turns.
     * 
     * @param boardSetup BoardSetup to evaluate
     * @return SingleMove[] a complete set of moves.
     */
    public MoveChain makeMoves(BoardSetup boardSetup) throws CannotDecideException {
        int player = boardSetup.getPlayerAtMove();
    
        if (!boardSetup.isSeparated()) {
            throw new CannotDecideException("not separated");
        }
    
        double bestValue = Double.MAX_VALUE;
        int bestIndex = -1;
    
        PossibleMoves pm = new PossibleMoves(boardSetup);
    
        List<BoardSetup> list = pm.getPossbibleNextSetups();
        int index = 0;
        for(BoardSetup setup : list) {
            float value = getEstimatedMoves(setup, player);
            if (value < bestValue) {
                bestValue = value;
                bestIndex = index;
            }
            index ++;
        }
    
        if (bestIndex == -1) {
            return MoveChain.EMPTY;
        } else {
            return pm.getMoveChain(bestIndex);
        }
    
    }
}
