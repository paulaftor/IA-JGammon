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

/**
 * 
 * Combines the following AIs:
 *
 * NORMAL : NomalNeuralNetAI
 * BEAROFF : BearOffAI
 * UNSEPARATED_BEAROFF : BearOffAI
 * RACE: RaceNeuralNetAI
 * 
 * @author Mattias Ulbrich
 * @version 1.0
 */
public class NeuralCombiAI extends CombiAI {

    public NeuralCombiAI() {
        setFallbackAI(new RandomAI());
        setAIForCategory(Mode.RACE, new RaceNeuralNetAI());
        setAIForCategory(Mode.NORMAL, new NormalNeuralNetAI());
        setAIForCategory(Mode.BEAROFF, new BearOffAI());
        setAIForCategory(Mode.UNSEPARATED_BEAROFF, new BearOffAI());
    }

    /**
     * get a short description of this method.
     * 
     * @return String
     */
    public String getDescription() {
        return "Combination of neural net AIs";
    }

    /**
     * get the name of this AI Method.
     * 
     * @return String
     */
    public String getName() {
        return "Neural Net AI";
    }

}
