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
 * <p>
 * Title: JGam - Java Backgammon
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2005
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author Mattias Ulbrich
 * @version 1.0
 */
public class ProbRaceNeuralNetAI extends NeuralNetAI {

    public static final int NUMBER_OF_RACE_FEATURES = 52;

    public ProbRaceNeuralNetAI() {
    }

    /**
     * make sure the neural network has the right size!
     * 
     * @param NeuralNetwork
     */
    public ProbRaceNeuralNetAI(NeuralNet network) {
        super(network);
    }

    /**
     * get a short description of this method.
     * 
     * @return String
     */
    public String getDescription() {
        return "Artificial Neural Net Evaluator for the racing condition";
    }

    /**
     * get the name of this AI Method.
     * 
     * @return String
     */
    public String getName() {
        return "ProbabilityRaceNeuralNet";
    }

    public void init() throws Exception {
        super.init();
        loadSer("probraceneuralnet.ser");
    }

    public int getNumberOfFeatures() {
        return NUMBER_OF_RACE_FEATURES;
    }

    /**
     * collect features!
     * 
     * This is endgame, so the points suffice. No need to store the 24/25 point
     * (there cant be anyone if separated)
     * 
     * 
     * @param setup BoardSetup to inspect
     * @param features double[] null or array to write to
     * @param player Player to inspect (1 or 2)
     * @return double[] features if not null, otherwise new memory
     */
    public static double[] extractFeatures(BoardSetup setup, double[] features) {

        if (features == null) {
            features = new double[NUMBER_OF_RACE_FEATURES];
        }

        int player = setup.getPlayerAtMove();
        for (int i = 1; i <= 24; i++) {
            features[i - 1] = setup.getPoint(player, i) / 5.;
        }

        for (int i = 1; i <= 24; i++) {
            features[23 + i] = setup.getPoint(3 - player, i) / 5.;
        }

        int pip1 = setup.calcPip(player);
        int pip2 = setup.calcPip(3 - player);
        features[48] = pip1;
        features[49] = pip2;

        features[50] = satDiv(pip1, pip2) / 10.;
        features[51] = satDiv(pip2, pip1) / 10.;

        return features;
    }

    private static double satDiv(int a, int b) {
        if(b == 0) {
            return 10.;
        } else {
            return Math.min(10., a / b);
        }
    }

    public double[] getFeatures(BoardSetup setup, double[] features) {
        return extractFeatures(setup, features);
    }

    /**
     * applying the neural net is different:
     * make the range easier to access:
     * 
     * instead of 0 .. 1 make the desired values 0.1 .. 0..9
     * 
     */
    public double applyNeuralNet(double[] features) {
        double d = super.applyNeuralNet(features);

        return Math.max(0, Math.min(1, (d-.1)/.8));
    }
    
    

}
