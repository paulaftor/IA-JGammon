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

import jgam.ai.AI;
import jgam.ai.BearOffAI;
import jgam.ai.CannotDecideException;
import jgam.ai.CombiAI;
import jgam.ai.CombiAI.Mode;
import jgam.ai.InitialAI;
import jgam.ai.RaceNeuralNetAI;
import jgam.game.BoardSetup;
import jgam.game.MoveChain;
import jgam.game.PossibleMoves;
import jgam.util.CommandLine;
import jgam.util.CommandLineException;
import mattze.ann.MultiLayerNeuralNet;
import mattze.ann.NeuralNet;
import mattze.ann.SimpleNeuralNet;
import util.AverageBuffer;
import util.Levels;

public class RaceTrainer {

    private static final int NUMBER_OF_HIDDEN_NODES = 5;

    double alpha = 1;
    double anneal = .001;
    int gameNo = 0;
    
    public PrintWriter err = new PrintWriter(System.err, true);
    public PrintWriter log = null;
    public BearOffAI bearoffAi = new BearOffAI();
    public RaceNeuralNetAI ai = new RaceNeuralNetAI();
    public AI initAi = new InitialAI(); // possibly NormalNetAI at some point ...
    public CombiAI combiAI = new CombiAI();
    public AverageBuffer errorHistory = new AverageBuffer(200);
    
    double[] features = new double[RaceNeuralNetAI.NUMBER_OF_RACE_FEATURES];

    private Levels levels = new Levels(20, -10);
    private int saveInterval = -1;
    private String saveFile;

    public RaceTrainer() throws Exception {
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
            System.err.println("Learning rate i " + rate);
            
            TrainingSetup s = new TrainingSetup();
            while (!s.isSeparated()) {
                s.roll();
                s.performMoves(combiAI.makeMoves(s));
                s.switchPlayers();
            }

//            s.debugOut();
            
            apply(1, s);
            apply(2, s);

            err.println("Game#: " + gameNo);
            err.println("Mean error " + errorHistory);
            gameNo ++;
            
            if(saveInterval > 0 && gameNo % saveInterval == 0) {
                saveNet(saveFile);
            }
        }
            
    }

    public void apply(int player, BoardSetup bs) throws CannotDecideException {
        TrainingSetup s = new TrainingSetup(bs);
        s.setPlayerAtMove(player);

        // go through race and bearoff
        while(s.winstatus() == 0) {
//            System.out.println("Calc pos with pip " + s.calcPip(player));
            // built-in look-ahead of 1. for anything else, this method is too
            // slow :-(
            double estMoves = calcValue(s, 1);
//            System.out.println(estMoves);
            RaceNeuralNetAI.extractFeatures(s, features, player);
            NeuralNet nn = ai.getNeuralNet();
            double error = ai.getEstimatedMoves(s, player) - estMoves;
            errorHistory.add(error);
            double[] target = { estMoves/80. };
            nn.train(features, target);
            
            s.roll();
            s.performMoves(combiAI.makeMoves(s));
        }
    }

    private double regression(BoardSetup bs) {
        int pip = bs.calcPip(bs.getPlayerAtMove());
        double regr = 2.55716 + .11427*pip;
        return regr;
    }
    
    private double rollout(TrainingSetup trainingSetup, int rounds)
            throws CannotDecideException {

        TrainingSetup t = new TrainingSetup();
        int count = 0;
        for (int i = 0; i < rounds; i++) {
            t.copyFrom(trainingSetup);
            while (t.winstatus() == 0) {
                t.roll();
                t.performMoves(combiAI.makeMoves(t));
                count ++;
            }
        }
        
        double ret = ((double)count) / rounds;
        return ret;
    }


    double calcValue(TrainingSetup s, int ply) throws CannotDecideException {
        Mode cat = CombiAI.getCategory(s, s.getActivePlayer());
        switch (cat) {
        case BEAROFF:
            // bearoff does not need plying ... optimal values!
            return bearoffAi.getEstimatedMoves(s, s.getActivePlayer());

        case RACE:
            if(ply == 0) {
                return ai.getEstimatedMoves(s, s.getActivePlayer());
            }
            break;

        default:
            assert false : "There should not be any other category";
        }
        
        double sum = 0.;
        levels.push(21);
        for (int i = 1; i <= 6; i++) {
            for (int j = i; j <= 6; j++) {
                s.roll(i, j);
                double bestValue = getBestMoveValue(s, ply);
                sum += bestValue;
                if(i != j) {
                    sum += bestValue;
                }
                levels.dec();
            }
        }
        levels.pop();

        double result = (sum / 36.) + 1;
        return result;
    }

    private double getBestMoveValue(TrainingSetup s, int ply)
            throws CannotDecideException {
        PossibleMoves pm = new PossibleMoves(s);
        double bestValue = Double.MAX_VALUE;
        levels.push(pm.getPossibleMoveChains().size());
        for (MoveChain move : pm.getPossibleMoveChains()) {
            TrainingSetup setup;
            setup = new TrainingSetup(s);
            setup.performMoves(move);
            double value = calcValue(setup, ply-1);
            if(value < bestValue) {
                bestValue = value;
            }
            levels.dec();
        }
        levels.pop();
        return bestValue;
    }

    private void createNewNeuralNet(int hiddenSize) {
//        MultiLayerNeuralNet net = new MultiLayerNeuralNet();
//        net.setInputLayer(new SigmoidLayer(RaceNeuralNetAI.NUMBER_OF_RACE_FEATURES, 
//                hiddenSize));
//        net.addLayer(new SigmoidLayer(1));
        SimpleNeuralNet net = new SimpleNeuralNet(
                RaceNeuralNetAI.NUMBER_OF_RACE_FEATURES,
                hiddenSize, 
                1);
        net.randomize(.2);
        ai.setNeuralNet(net);

    }

    public static void main(String[] args) throws Exception {
        CommandLine cl = new CommandLine();
        cl.addOption("-in", "file", "The serialised net to read in");
        cl.addOption("-out", "file", "The serialised net to write to");
        cl.addOption("-saveAfter", "n", "Save the result every n games");
        cl.addOption("-runs", "n", "Number of games to play (defaults to 10000)");
        cl.addOption("-cache", "n", "Activate caching of positions, size of cache, disabled by default");
        cl.addOption("-hidden", "n", "Number of hidden nodes in ANN (defaults to " + NUMBER_OF_HIDDEN_NODES + ")");
        System.out.println("Command line options are:");
        cl.printUsage(System.out);
        cl.parse(args);
        
        RaceTrainer t = new RaceTrainer();
        
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
