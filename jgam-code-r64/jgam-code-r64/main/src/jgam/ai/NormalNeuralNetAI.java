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

import jgam.game.*;
import mattze.ann.NeuralNet;

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
public class NormalNeuralNetAI extends NeuralNetAI {

    public static final int NUMBER_OF_NORMAL_FEATURES = 153;

    public NormalNeuralNetAI() {}

    /**
     * make sure the neural network has the right size!
     * @param NeuralNetwork
     */
    public NormalNeuralNetAI(NeuralNet network) {
        super(network);
    }

    /**
     * get a short description of this method.
     *
     * @return String
     */
    public String getDescription() {
        return "Artificial Neural Net Evaluator";
    }

    /**
     * get the name of this AI Method.
     *
     * @return String
     */
    public String getName() {
        return "Neural Net AI";
    }

   public void init() throws Exception {
       super.init();
       loadSer("neuralnet.ser");
   }


    public int getNumberOfFeatures() {
        return NUMBER_OF_NORMAL_FEATURES;
    }

    /**
     * extract Features:
     *
     * for k = 1..24
     * 6k-4      - my holds on k   [i.e. 1 iff I hold k]
     * 6k-4 + 1  - my blots on k   [i.e. 1 iff I have a blot on k]
     * 6k-4 + 2  - point(k) / 5.
     * 6k-4 + 3  - his holds on k
     * 6k-4 + 4  - his blots on k
     * 6k-4 + 5  - hispoint(k) / 5.
     * end
     * 144 - my bar
     * 145 - his bar
     * 146 - my off
     * 147 - his off
     * 148 - my maxpoint
     * 149 - his maxpoint
     * 150 - my pip / 167 (starting pip)
     * 151 - his pip
     * 150 - my chance to be hit
     *
     * @param setup BoardSetup
     * @return double[]
     */
    public static double[] extractFeatures(BoardSetup setup, double[] features) {

        // always eval for player 1 (invert otherwise)
        if (setup.getPlayerAtMove() == 2) {
            setup = new InvertedBoardSetup(setup);
        }

        if (features == null) {
            features = new double[NUMBER_OF_NORMAL_FEATURES];
        }

        features[0] = setup.calcPip(1) / 167.;
        features[1] = setup.calcPip(2) / 167.;
        for (int k = 0; k < 24; k++) {
            features[6 * k] = setup.getPoint(1, k + 1) > 1 ? 1 : 0;
            features[1 + 6 * k] = setup.getPoint(1, k + 1) == 1 ? 1 : 0;
            features[2 + 6 * k] = setup.getPoint(1, k + 1) / 5.;
            features[3 + 6 * k] = setup.getPoint(2, k + 1) > 1 ? 1 : 0;
            features[4 + 6 * k] = setup.getPoint(2, k + 1) == 1 ? 1 : 0;
            features[5 + 6 * k] = setup.getPoint(2, k + 1) / 5.;
        }
        features[144] = setup.getBar(1) / 5.;
        features[145] = setup.getBar(2) / 5.;
        features[146] = setup.getOff(1) / 5.;
        features[147] = setup.getOff(2) / 5.;
        features[148] = setup.getMaxPoint(1) / 24.;
        features[149] = setup.getMaxPoint(2) / 24.;
        features[150] = setup.calcPip(1) / 167.;
        features[151] = setup.calcPip(2) / 167.;
        features[152] = calcWeightedBlots(setup, 1);

        return features;
    }


    public double[] getFeatures(BoardSetup setup, double[] features) {
        return extractFeatures(setup, features);
    }

    /**
     * calcBlots
     *
     * count the number of point which have exactly one checker.
     * betweeen startat and startat+2
     *
     * Result is normalized to get value approx. betw. 0 and 1:
     * div. by 3;
     *
     * @param setup BoardSetup
     * @param player player to look at
     * @return 0 <= x <= 1

     * @param setup BoardSetup
     * @param i int
     * @param i1 int
     * @return double
     */
    private static double calcBlots(BoardSetup setup, int player, int startAt) {
        double count = 0;
        for (int i = startAt; i < startAt + 3; i++) {
            if (setup.getPoint(player, i) == 1) {
                count++;
            }
        }
        return count / 3.;
    }

    /**
     * calcWeightedBlots
     *
     * add up "chance" to be shot.
     *
     * @param setup BoardSetup
     * @param player player to check
     * @return double > 0
     */
    private static double calcWeightedBlots(BoardSetup setup, int player) {
        double sum = 0;
        for (int i = 1; i <= 25; i++) {
            if (setup.getPoint(player, i) == 1) {
                sum += chanceToHit(setup, 3 - player, 25 - i);
            }
        }

        return sum;
    }

    /**
     * calcHold (number of held points)
     *
     * count the number of point which have at least two checkers.
     * betweeen startat and startat+2
     *
     * Result is normalized to get value approx. betw. 0 and 1:
     * div. by 3;
     *
     * @param setup BoardSetup
     * @param player player to look at
     * @return > 0
     */
    private static double calcHold(BoardSetup setup, int player, int startat) {

        int count = 0;
        for (int i = startat; i < startat + 3; i++) {
            if (setup.getPoint(player, i) >= 2) {
                count++;
            }
        }

        return count / 3.;
    }

    /**
     * chance for player to hit point in the next turn.
     *
     * check all 6 possible dice values.
     * poss = #possibl dice values.
     *
     * return poss/6.
     *
     * @param setup BoardSetup
     * @param player int
     * @param point int
     * @return double
     */
    protected static double chanceToHit(BoardSetup setup, int player, int point) {

        int poss = 0;
        for (int i = 1; i < 6; i++) {
            if (point + i < 25 && setup.getPoint(player, point + i) > 0) {
                poss++;
            }
        }

        return poss / 6.;
    }


}
