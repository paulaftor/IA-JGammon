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

import java.io.*;

import jgam.game.BoardSetup;
import mattze.ann.NeuralNet;

public abstract class NeuralNetAI extends EvaluatingAI {

    public NeuralNetAI() {
    }

    public NeuralNetAI(NeuralNet neuralNet) {
        this.neuralNet = neuralNet;
    }

    /**
     * get the features of a setup for this neural net.
     * 
     * @param setup setup to examine
     * @param features target buffer
     * @return features
     */
    public abstract double[] getFeatures(BoardSetup setup, double[] features);

    public abstract int getNumberOfFeatures();

    // use this buffer during the game in order to not always allocate
    // new memory
    protected double[] featureBuffer;

    protected NeuralNet neuralNet;

    /**
     * getNeuralNet
     * 
     * @return NeuralNetwork
     */
    public NeuralNet getNeuralNet() {
        return neuralNet;
    }

    public void setNeuralNet(NeuralNet neuralNet) {
        this.neuralNet = neuralNet;
    }

    /**
     * load the neuralnet from a file.
     * 
     * The given resource must be findable by the Classloader and must be the
     * serialized version of an appropriate NeuralNet
     */
    public void loadSer(String resource) throws IOException,
            ClassNotFoundException {
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

    public void init() throws Exception {
        featureBuffer = new double[getNumberOfFeatures()];
    }

    /**
     * give free the resources used by the neural net
     */
    public void dispose() {
        neuralNet = null;
        featureBuffer = null;
    }

    /**
     * evaluate a BoardSetup.
     * 
     * @param setup BoardSetup
     * @return double between 0 and 1.
     */
    public double probabilityToWin(BoardSetup setup)
            throws CannotDecideException {
        double[] features = getFeatures(setup, featureBuffer);
        double res = applyNeuralNet(features);
        return res;
    }

    /**
     * get the value of the neural net for a specific feature set. default usage
     * of the neural net is simply applying it to the data
     * 
     * @param features the features to apply the net on
     * @return the output value of the feature
     */
    public double applyNeuralNet(double[] features) {
        return neuralNet.applyTo(features)[0];
    }
}
