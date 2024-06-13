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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jgam.ai.*;
import jgam.ai.CombiAI.Mode;
import jgam.game.ArrayBoardSetup;
import jgam.game.BoardSetup;
import jgam.game.PossibleMoves;
import jgam.game.SingleMove;
import jgam.util.Util;
import mattze.ann.MultiLayerNeuralNet;
import mattze.ann.NeuralNet;
import mattze.ann.SigmoidLayer;
import util.AverageBuffer;
import util.Levels;

/**
 * Train the race net using the bear off ai.
 */
public class BearOffSupervisedTrainer {

    private static final int NUMBER_OF_HIDDEN_NODES = 15;

    double alpha = 3;
    double anneal = .001;
    boolean suspendWish = false;
    int gameNo = 0;

    public PrintWriter err = new PrintWriter(System.err, true);
    public PrintWriter log = null;
    public BearOffAI bearoffAi = new BearOffAI();
    public RaceNeuralNetAI ai = new RaceNeuralNetAI();
    public AI initAi = new InitialAI(); // possibly NormalNetAI at some point ...
    public CombiAI combiAI = new NeuralCombiAI();
    public AverageBuffer errorHistory = new AverageBuffer(200);
    
    double[] features = new double[RaceNeuralNetAI.NUMBER_OF_RACE_FEATURES];

    private Levels levels = new Levels(20, 2);

    public BearOffSupervisedTrainer() throws Exception {
        combiAI.setAIForCategory(Mode.BEAROFF, bearoffAi);
        combiAI.setAIForCategory(Mode.RACE, ai);
        combiAI.setAIForCategory(Mode.UNSEPARATED_BEAROFF, bearoffAi);
        combiAI.setAIForCategory(Mode.NORMAL, initAi);
    }
    
    public void train(int games) throws CannotDecideException {
        
        Map<Integer, Float> table = bearoffAi.getTable();
        List<Integer> allPrints = new ArrayList<Integer>(table.keySet());
        
        while (games == -1 || gameNo < games) {
            
            Integer print = Util.chooseRandom(allPrints);
            Float val = table.get(print); 
            BoardSetup setup = makeSetup(print);
            RaceNeuralNetAI.extractFeatures(setup, features, 1);
            double[] target = { val / 80. };
            NeuralNet nn = ai.getNeuralNet();
            double error = nn.error(features, target);
            errorHistory.add(Math.sqrt(error));
            double rate = alpha
                    * Math.pow(1-anneal, nn.getTrainCount() / 1000.);
            System.err.printf("Rate: %g, Error: %g\n", rate, errorHistory.average()*80);
            nn.setLearningRate(rate);
            nn.train(features, target);
            
            gameNo ++;
        }
    }

    private BoardSetup makeSetup(int print) {
        TrainingSetup bs = new TrainingSetup(true);
        bs.setPlayerAtMove(1);
        for (int i = 0; i < 6; i++) {
            int v = print & 0xf;
            bs.setPoint(1, i+1, v);
            print >>= 4;
        }
        return bs;
    }

    private void createNewNeuralNet() {
        MultiLayerNeuralNet net = new MultiLayerNeuralNet();
        net.setInputLayer(new SigmoidLayer(RaceNeuralNetAI.NUMBER_OF_RACE_FEATURES, 
                NUMBER_OF_HIDDEN_NODES));
        net.addLayer(new SigmoidLayer(1));
        net.randomize(.5);
        net.enableTraining();
        net.setMomentum(.2);
//        SimpleNeuralNet net = new SimpleNeuralNet(
//                RaceNeuralNetAI.NUMBER_OF_RACE_FEATURES, 
//                NUMBER_OF_HIDDEN_NODES, 
//                1);
//        net.randomize(.5);
        ai.setNeuralNet(net);
    }

    public static void main(String[] args) throws Exception {
        BearOffSupervisedTrainer t = new BearOffSupervisedTrainer();
        
        if(args.length > 0) {
            t.loadNet(args[0]);
        } else {
            t.createNewNeuralNet();
        }
        
        t.init();
        t.train(10000000);
        
        System.out.println(t.ai.getNeuralNet().toString());
    }

    private void init() throws Exception {
        combiAI.init();
    }

    private void loadNet(String fileName) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(fileName));
        ai.setNeuralNet((NeuralNet) ois.readObject());
        ois.close();
    }

}
