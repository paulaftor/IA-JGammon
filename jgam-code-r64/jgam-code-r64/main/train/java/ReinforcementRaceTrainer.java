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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.Arrays;

import jgam.ai.*;
import jgam.ai.CombiAI.Mode;
import jgam.game.BoardSetup;
import jgam.util.CommandLine;
import jgam.util.CommandLineException;
import mattze.ann.SimpleNeuralNet;
import mattze.ann.MultiLayerNeuralNet;
import mattze.ann.NeuralNet;
import mattze.ann.ReLUSimpleNeuralNet;
import util.AverageBuffer;

public class ReinforcementRaceTrainer {

    private static final int NUMBER_OF_HIDDEN_NODES = 20;

    private static final double AVERAGE_DICE = 49./6.;

    double alpha = .3;
    double anneal = .001;
    double lambda = .3;
    int gameNo = 0;
    
    public PrintWriter err = new PrintWriter(System.err, true);
    public PrintWriter log = null;
    public BearOffAI bearoffAi = new BearOffAI();
    public RaceNeuralNetAI ai = new RaceNeuralNetAI();
    public AI initAi = new InitialAI(); // possibly NormalNetAI at some point ...
    public CombiAI combiAI = new NeuralCombiAI();
    public AverageBuffer errorHistory = new AverageBuffer(200);
    
    double[] features = new double[RaceNeuralNetAI.NUMBER_OF_RACE_FEATURES];

    private int saveInterval = -1;
    private String saveFile;

    public ReinforcementRaceTrainer() throws Exception {
        combiAI.setAIForCategory(Mode.BEAROFF, bearoffAi);
        combiAI.setAIForCategory(Mode.RACE, ai);
        combiAI.setAIForCategory(Mode.UNSEPARATED_BEAROFF, bearoffAi);
        combiAI.setAIForCategory(Mode.NORMAL, initAi);
    }
    
    public void train(int games) throws CannotDecideException, IOException {
        TrainingSetup basis = new TrainingSetup(true);
        basis.setPoint(1, 19, 1);
        basis.setPoint(2, 1, 1);
        basis.setPlayerAtMove(1);
        basis.roll(1, 2);
        
        while (games == -1 || gameNo < games) {
            NeuralNet nn = ai.getNeuralNet();
            double rate = alpha
                    * Math.pow(1-anneal, nn.getTrainCount() / 1000.);
            nn.setLearningRate(rate);
            System.err.println("Learning rate is " + rate);
            
            TrainingSetup s = new TrainingSetup();
            while (!s.isSeparated()) {
                s.roll();
                s.performMoves(combiAI.makeMoves(s));
                s.switchPlayers();
            }

//            s.debugOut();
            
            s.setPlayerAtMove(1);
            apply(s);
            s.setPlayerAtMove(2);
            apply(s);

            err.println("Game#: " + gameNo);
            err.println(errorHistory);
            gameNo ++;
            
            if(saveInterval > 0 && gameNo % saveInterval == 0) {
                saveNet(saveFile);
            }
        }
            
    }

    public double apply(TrainingSetup bs) throws CannotDecideException {
        if(bs.winstatus() != 0) {
            return 0.;
        }
        
        TrainingSetup s = new TrainingSetup(bs);
        s.roll();
        s.performMoves(combiAI.makeMoves(s));
        
        // bs.debugOut();

        if(CombiAI.getCategory(bs, bs.getActivePlayer()) == Mode.RACE) {
            double evaled = apply(s) + 1;//diceVal(s) / AVERAGE_DICE;

            NeuralNet nn = ai.getNeuralNet();
            RaceNeuralNetAI.extractFeatures(bs, features, bs.getPlayerAtMove());
            int pip = bs.calcPip(bs.getPlayerAtMove());
            evaled = 2.55716 + .11427*pip;

            double oldVal = ai.getEstimatedMoves(bs, bs.getActivePlayer());
            double newVal = evaled; //(1-lambda)*oldVal + lambda * evaled;
            System.out.println(Arrays.toString(features) + " " + evaled + " " + oldVal + " -> " + newVal);
            double[] target = { newVal/80. };
            errorHistory.add(newVal - oldVal);
            nn.train(features, target);
            // ((SimpleNeuralNet)nn).dump();
            return newVal;
        } else {
            apply(s);
            double bearOffVal = bearoffAi.getEstimatedMoves(bs, bs.getActivePlayer());
            double[] target = { bearOffVal/80. };
           //nn.train(features, target);
            return bearOffVal;
        }
    }
    
    private double diceVal(BoardSetup s) {
        int[] dice = s.getDice();
        if(dice[0] == dice[1])
            return 4*dice[0];
        else
            return dice[0] + dice[1];
    }

    private void createNewNeuralNet(int hiddenSize) {
//        MultiLayerNeuralNet net = new MultiLayerNeuralNet();
//        net.setInputLayer(new SigmoidLayer(RaceNeuralNetAI.NUMBER_OF_RACE_FEATURES, 
//                hiddenSize));
//        net.addLayer(new SigmoidLayer(1));
        SimpleNeuralNet net = new ReLUSimpleNeuralNet(
                RaceNeuralNetAI.NUMBER_OF_RACE_FEATURES,
                hiddenSize, 
                1);
        net.randomize(1.);
        ai.setNeuralNet(net);

    }

    public static void main(String[] args) throws Exception {
        CommandLine cl = new CommandLine();
        cl.addOption("-in", "file", "The serialised net to read in");
        cl.addOption("-out", "file", "The serialised net to write to");
        cl.addOption("-saveAfter", "n", "Save the result every n games");
        cl.addOption("-runs", "n", "Number of games to play (defaults to 10000)");
        cl.addOption("-lambda", "d", "set lambda value of reinforcement");
        cl.addOption("-cache", "n", "Activate caching of positions, size of cache, disabled by default");
        cl.addOption("-hidden", "n", "Number of hidden nodes in ANN (defaults to " + NUMBER_OF_HIDDEN_NODES + ")");
        System.out.println("Command line options are:");
        cl.printUsage(System.out);
        cl.parse(args);
        
        ReinforcementRaceTrainer t = new ReinforcementRaceTrainer();
        
        if(cl.isSet("-lambda")) {
            t.lambda = cl.getDouble("-lambda", t.lambda);
        }
        
        if(cl.isSet("-in")) {
            t.loadNet(cl.getString("-in", ""));
        } else {
            int hiddenSize = cl.getInteger("-hidden", NUMBER_OF_HIDDEN_NODES);
            t.createNewNeuralNet(hiddenSize);
        }
        
        if(cl.isSet("-saveAfter")) {
            if(!cl.isSet("-out")) {
                throw new CommandLineException("-saveAfter needs -out");
            }
            t.saveFile = cl.getString("-out", "");
            t.saveInterval = cl.getInteger("-saveAfter", 0);
        }
        
        int runs = cl.getInteger("-runs", 10000);
        
        t.init();
        t.train(runs);
        
        System.out.println(t.ai.getNeuralNet().toString());
        
        if(cl.isSet("-out")) {
            t.saveNet(cl.getString("-out", ""));
        }
    }

    private void init() throws Exception {
        combiAI.init();
        NeuralNet net = ai.getNeuralNet();
        if (net instanceof MultiLayerNeuralNet) {
            MultiLayerNeuralNet mln = (MultiLayerNeuralNet) net;
            mln.enableTraining();
        }
    }

    private void loadNet(String fileName) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(fileName));
        NeuralNet net = (NeuralNet) ois.readObject();
        ai.setNeuralNet(net);
        ois.close();
    }
    
    private void saveNet(String filename) throws IOException {
        FileOutputStream fos = new FileOutputStream(filename);
        ObjectOutputStream os = new ObjectOutputStream(fos);
        os.writeObject(ai.getNeuralNet());
        os.close();
    }

}
