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

import java.util.EnumMap;

import jgam.game.*;

/**
 * 
 * Combine AIs for three different situations:
 * 
 * 1. "normal" AI ---> FeatureEvaluator
 * 2. race condition ---> RaceAI
 * 3. bearing off ---> BearoffAI
 * 
 * @author Mattias Ulbrich
 * @version 1.0
 */
public abstract class CombiAI extends EvaluatingAI {

    public static enum Mode {
        NORMAL, BEAROFF, RACE, UNSEPARATED_BEAROFF
    }
    
    private AI fallbackAI;

    private EnumMap<Mode, AI> mapAI = new EnumMap<Mode, AI>(Mode.class);

    /**
     * free all used resources.
     */
    public void dispose() {
        fallbackAI.dispose();
        for (AI ai : mapAI.values()) {
            ai.dispose();
        };
        mapAI.clear();
        mapAI = null;
    }

    /**
     * initialize this instance.
     *
     * Set a specialised AI for all phases if non has been set so far.
     *
     * @throws Exception if sth goes wrong during init.
     */
    public void init() throws Exception {
        fallbackAI.init();

        for (AI ai : mapAI.values()) {
            ai.init();
        }
    }

    /**
     * given a board make decide which moves to make.
     * 
     * delegate to category-ai
     * 
     * @param boardSetup BoardSetup to evaluate
     * @return SingleMove[] a complete set of moves.
     * @exception CannotDecideException if even the fall back ai fails
     */
    public MoveChain makeMoves(BoardSetup boardSetup)
            throws CannotDecideException {
        Mode category = getCategory(boardSetup, boardSetup.getPlayerAtMove());
        System.out.println(category + " " + getAIForCategory(category));
        return getAIForCategory(category).makeMoves(boardSetup);
    }

    /**
     * given a board make decide whether to roll or to double.
     * 
     * delegate to category-ai
     * 
     * @param boardSetup BoardSetup
     * @return either DOUBLE or ROLL
     * @exception CannotDecideException if even the fall back ai fails
     */
    public int rollOrDouble(BoardSetup boardSetup) throws CannotDecideException {
        Mode category = getCategory(boardSetup, boardSetup.getPlayerAtMove());
        return getAIForCategory(category).rollOrDouble(boardSetup);

    }

    /**
     * given a board and a double offer, take or drop.
     * 
     * delegate to the category - ai. But take the other player - for its his
     * position!
     * 
     * @param boardSetup BoardSetup
     * @return either TAKE or DROP
     * @exception CannotDecideException if even the fall back ai fails
     */
    public int takeOrDrop(BoardSetup boardSetup) throws CannotDecideException {
        Mode category = getCategory(boardSetup, 3 - boardSetup.getPlayerAtMove());
        return mapAI.get(category).takeOrDrop(boardSetup);
    }

    /**
     * get the category in which a setup falls for a given player
     * 
     * @param setup setup to categorize
     * @param player player
     * @return NORMAL, RACE, BEAROFF
     */
    public static Mode getCategory(BoardSetup setup, int player) {

        int max = setup.getMaxPoint(player);

        if (max <= 6) {
            if(setup.isSeparated()) {
                return Mode.BEAROFF;
            } else {
                return Mode.UNSEPARATED_BEAROFF;
            }
        }

        if (setup.isSeparated()) {
            return Mode.RACE;
        }

        return Mode.NORMAL;
    }

    /**
     * get the AI used for a category
     * 
     * @param category NORMAL, BEARoFF, RACE
     * @return AI
     */
    public AI getAIForCategory(Mode category) {
        AI ai = mapAI.get(category);
        if(ai == null) {
            ai = fallbackAI;
        }
        return ai;
    }

    /**
     * set the AI to be used for a category.
     * 
     * @param category NORMAL, BEarOFF, RACE
     * @param catai AI to be used
     */
    public void setAIForCategory(Mode category, AI catai) {
        mapAI.put(category, catai);
    }

    public AI getFallbackAI() {
        return fallbackAI;
    }

    public void setFallbackAI(AI fallbackAI) {
        this.fallbackAI = fallbackAI;
    }

    /**
     * delegate this to the underlying ai for the categories.
     * 
     * But also check a in/lose situation first.
     * 
     * @param boardSetup BoardSetup
     * @return double
     * @throws CannotDecideException
     */
    public double probabilityToWin(BoardSetup boardSetup)
            throws CannotDecideException {

        if (boardSetup.getOff(boardSetup.getPlayerAtMove()) == 15) {
            return 1.0;
        }

        if (boardSetup.getOff(3 - boardSetup.getPlayerAtMove()) == 15) {
            return 0.0;
        }

        Mode category = getCategory(boardSetup, boardSetup.getPlayerAtMove());
        AI ai = mapAI.get(category);
        if (ai instanceof EvaluatingAI) {
            EvaluatingAI evalAI = (EvaluatingAI) ai;
            return evalAI.probabilityToWin(boardSetup);
        } else {
            throw new CannotDecideException("AI for mode " + category + " is not an evaluating AI.");
        }
        
    }
}