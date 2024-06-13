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

package mattze.ann;

import java.io.*;
import java.util.*;

import junit.framework.TestCase;

/**
 * the xor example
 *
 * @author Mattias Ulbrich
 * @version 2006-05-10
 */
public class TestMultiLayer extends TestCase {

    private static double[][] traindata = { {1, 0}, {0, 1}, {1, 1}, {0, 0}
    };
    // Targets are: xor, and, or
    private static double[][] target = { {1, 0, 1}, {1, 0, 1}, {0, 1, 1}, {0, 0, 0}
    };

    public void testForward() {
        MultiLayerNeuralNet nn = new MultiLayerNeuralNet();
        nn.setInputLayer(new LinearLayer(2, 2));
        nn.addLayer(new SigmoidLayer(2,1));
        
        nn.getLayer(0).setWeight(0, 0, 1.);
        nn.getLayer(0).setWeight(0, 1, 2.);
        nn.getLayer(0).setWeight(1, 0, 2.);
        nn.getLayer(0).setWeight(1, 1, 1.);
        
        nn.getLayer(1).setWeight(0, 0, 1.);
        nn.getLayer(1).setWeight(0, 1, 1.);
        
        double val = nn.applyTo(new double[] {2., 1.})[0];
        
        double expectedHidden1 = 2.*1. + 1.*2.;
        double expectedHidden2 = 2.*2. + 1.*1.;
        double expected = sigmoid(expectedHidden1 + expectedHidden2); 
        
        assertEquals(expected, val);
    }
    
    public void testBackward1() throws Exception {
        MultiLayerNeuralNet nn = new MultiLayerNeuralNet();
        nn.setInputLayer(new LinearLayer(2, 1));
        nn.enableTraining();
        nn.setMomentum(0);
        nn.setLearningRate(.5);
        nn.getLayer(0).setWeight(0, 0, 1);
        nn.getLayer(0).setWeight(0, 1, 2);
        nn.getLayer(0).setWeight(0, 2, 1); // bias
        
        double[] input = new double[] {1, 1};
        double[] shouldbe = new double[] {10};
        assertEquals(4., nn.applyTo(input)[0]);
        
        nn.train(input, shouldbe);
        
        // old + eta * out_diff * in * delta
        assertEquals(1 + .5 * 1. * 1. * 6., nn.getLayer(0).getWeight(0, 0));
        assertEquals(2 + .5 * 1. * 1. * 6., nn.getLayer(0).getWeight(0, 1));
        assertEquals(1 + .5 * 1. * 1. * 6., nn.getLayer(0).getWeight(0, 2));
        
    }
    
    public void testBackward2() throws Exception {
        MultiLayerNeuralNet nn = new MultiLayerNeuralNet();
        nn.setInputLayer(new SigmoidLayer(1, 1));
        nn.enableTraining();
        nn.setMomentum(0);
        nn.setLearningRate(.5);
        nn.getLayer(0).setWeight(0, 0, 1);
        
        double[] input = new double[] {1};
        double[] shouldbe = new double[] {.5};
        double sig1 = sigmoid(1.);
        assertEquals(sig1, nn.applyTo(input)[0]);
        
        nn.train(input, shouldbe);
        double sd = sigmoidDiff(1.);
        double expected = 1 + .5 * sd * 1. * (.5 - sig1); 
        assertEquals(expected, nn.getLayer(0).getWeight(0, 0));
        // old + eta * out_diff * in * delta
    }
    

    private double sigmoid(double d) {
        return 1. / (1. + Math.exp(-d));
    }
    
    private double sigmoidDiff(double d) {
        double s = sigmoid(d);
        return s * (1 - s);
    }


    public static void main(String[] args) throws IOException {

        MultiLayerNeuralNet nn = new MultiLayerNeuralNet();
        nn.setInputLayer(new SigmoidLayer(2, 3));
        nn.addLayer(new SigmoidLayer(3,3));

        for (int i = 0; i < 4; i++) {
//            traindata[i][0] = Layer.sigmoid(traindata[i][0]);
//            traindata[i][1] = Layer.sigmoid(traindata[i][1]);
        }

        nn.randomize(5);
        nn.setLearningRate(.3);
        nn.setMomentum(0.);
        nn.enableTraining();

        Random r = new Random();

        for (int i = 0; i < 10000000; i++) {
            int j = r.nextInt(4);
            nn.train(traindata[j], target[j]);
            if (i % 100000 == 0) {
                double mse = 0;
                for (int k = 0; k < 4; k++) {
                    mse += mse(nn.applyTo(traindata[k]), target[k]);
                }
                System.out.println(mse);
            }
        }

        double mse = 0;
        for (int i = 0; i < 4; i++) {
            mse += mse(nn.applyTo(traindata[i]), target[i]);
        }

        System.out.println(mse);

        for (int i = 0; i < 3; i++) {
            System.out.println();
            System.out.println(nn.applyTo(traindata[0])[i]);
            System.out.println(nn.applyTo(traindata[1])[i]);
            System.out.println(nn.applyTo(traindata[2])[i]);
            System.out.println(nn.applyTo(traindata[3])[i]);
        }
    }

    static public double mse(double[] v1, double[] v2) {
        double mse = 0;
        for (int i = 0; i < v2.length; i++) {
            mse += (v1[i] - v2[i]) * (v1[i] - v2[i]);
        }
        return mse;
    }
}
