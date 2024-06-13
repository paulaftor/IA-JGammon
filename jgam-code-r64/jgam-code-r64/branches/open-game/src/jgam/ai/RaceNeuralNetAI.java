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

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URL;

import jgam.game.BoardSetup;
import jgam.util.Util;
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
public class RaceNeuralNetAI extends RoundEstimatingAI {

    public final static int NUMBER_OF_RACE_FEATURES = 24;

    private NeuralNet neuralNet;

    private double[] featureBuffer = new double[NUMBER_OF_RACE_FEATURES];
    
    public RaceNeuralNetAI() {
        super();
    }

    public RaceNeuralNetAI(NeuralNet neuralNet) {
        this.neuralNet = neuralNet;
    }

    /**
     * get the name of this AI Method.
     * 
     * @return String
     * @todo Implement this jgam.ai.AI method
     */
    public String getName() {
        return "NeuralRacing";
    }

    /**
     * get a short description of this method.
     * 
     * @return String
     * @todo Implement this jgam.ai.AI method
     */
    public String getDescription() {
        return "Neural Net for Racing";
    }

    /**
     * collect features!
     * 
     * This is endgame, number of turns is to be estimated. It suffices to only
     * look at my own checkers.
     * 
     * @param setup BoardSetup to inspect
     * @param features double[] null or array to write to
     * @param player Player to inspect (1 or 2)
     * @return double[] features if not null, otherwise new memory
     */
    public static double[] extractFeatures(BoardSetup setup, double[] features,
            int player) {

        if (features == null) {
            features = new double[NUMBER_OF_RACE_FEATURES];
        }

        for (int i = 1; i <= 24; i++) {
            features[i - 1] = setup.getPoint(player, i);
        }

        return features;
    }

    /**
     * initialize this instance.
     * 
     * Load the net from resources.
     * 
     * @throws Exception if sth goes wrong during init.
     * @todo Implement this jgam.ai.AI method
     */
    public void init() throws Exception {
        String resource = "raceneuralnet.ser";
        if (neuralNet == null) {
            InputStream is = getClass().getResourceAsStream(resource);
            if (is == null) {
                throw new FileNotFoundException(resource + " not in resources");
            }
            ObjectInputStream ois = new ObjectInputStream(is);
            neuralNet = (NeuralNet) ois.readObject();
            ois.close();
        }
    }

    /**
     * @throws CannotDecideException 
     */
    public float getEstimatedMoves(BoardSetup setup, int player) throws CannotDecideException {
        
        if(!setup.isSeparated()) {
            throw new CannotDecideException("Setup is not separated");
        }
        
        extractFeatures(setup, featureBuffer, player);
        double result = neuralNet.applyTo(featureBuffer)[0];
        result *= 80;
        return (float)result;
    }

    public void dispose() {
        neuralNet = null;
    } 

    /**
     * @return Returns the neuralNet.
     */
    public NeuralNet getNeuralNet() {
        return neuralNet;
    }

    /**
     * @param neuralNet The neuralNet to set.
     */
    public void setNeuralNet(NeuralNet neuralNet) {
        this.neuralNet = neuralNet;
    }
    
    /**
     * use the neural net on a set of features.
     * @param features array of appropriate size
     * @return the output of the neural net
     */
    public double useNeuralNet(double[] features) {
        return neuralNet.applyTo(features)[0];
    }

}
