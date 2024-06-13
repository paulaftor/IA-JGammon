//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.ObjectInputStream;
//import java.io.ObjectOutputStream;
//import java.io.PrintWriter;
//import java.util.HashMap;
//import java.util.Map;
//
//import jgam.ai.AI;
//import jgam.ai.BearOffAI;
//import jgam.ai.CannotDecideException;
//import jgam.ai.CombiAI;
//import jgam.ai.CombiAI.Mode;
//import jgam.ai.InitialAI;
//import jgam.ai.RaceNeuralNetAI;
//import jgam.game.BoardSetup;
//import jgam.game.MoveChain;
//import jgam.game.PossibleMoves;
//import jgam.util.CommandLine;
//import jgam.util.CommandLineException;
//import mattze.ann.MultiLayerNeuralNet;
//import mattze.ann.NeuralNet;
//import util.AverageBuffer;
//
///**
// * Train the race net by full rollout of lost-contact situations. 
// */
//public class RolloutRaceTrainer {
//
//    @SuppressWarnings("serial")
//    private static class LimitException extends Exception {}
//    
//    double alpha = 3;
//    double anneal = .001;
//    int gameNo = 0;
//    private int limit = 100000;
//    
//    public PrintWriter err = new PrintWriter(System.err, true);
//    public PrintWriter log = null;
//    public BearOffAI bearoffAi = new BearOffAI();
//    public RaceNeuralNetAI ai = new RaceNeuralNetAI();
//    public AI initAi = new InitialAI(); // possibly NormalNetAI at some point ...
//    public CombiAI combiAI = new CombiAI();
//    public AverageBuffer errorHistory = new AverageBuffer(200);
//    
//    double[] features = new double[RaceNeuralNetAI.NUMBER_OF_RACE_FEATURES];
//
////    private Levels levels = new Levels(20, 1);
//    private Map<String, Double> boardTable = new HashMap<String, Double>();
//
//    public RolloutRaceTrainer() throws Exception {
//        combiAI.setAIForCategory(Mode.BEAROFF, bearoffAi);
//        combiAI.setAIForCategory(Mode.RACE, ai);
//        combiAI.setAIForCategory(Mode.UNSEPARATED_BEAROFF, bearoffAi);
//        combiAI.setAIForCategory(Mode.NORMAL, initAi);
//    }
//    
//    public void train(int games) throws CannotDecideException {
//        
//        while (games == -1 || gameNo < games) {
//            TrainingSetup s = new TrainingSetup();
//            while (!s.isSeparated()) {
//                s.roll();
//                s.performMoves(combiAI.makeMoves(s));
//                s.switchPlayers();
//            }
//
//            s.debugOut();
//            
//            apply(1, s);
//            apply(2, s);
//
//            err.println("Game#: " + gameNo);
//            err.println("Mean error " + errorHistory.average()*80);
//            gameNo ++;
//        }
//            
//    }
//
//    public void apply(int player, BoardSetup bs) throws CannotDecideException {
//        TrainingSetup s = new TrainingSetup(bs);
//        NeuralNet nn = ai.getNeuralNet();
//        s.setPlayerAtMove(player);
//        boardTable.clear();
//        
//        try {
//            calcValue(s);
//        } catch(LimitException ex) {
//            System.out.println("Limit of " + limit + " reached.");
//        }
//        
//        System.out.println(boardTable.size() + 
//                " setups rolled out for player " + player);
//
//        for (Map.Entry<String, Double> entry : boardTable.entrySet()) {
//            extractFeatures(entry.getKey());
//            double estMoves = entry.getValue();
//            estMoves = estMoves / 80.;
//            double[] target = { estMoves };
//            double error = nn.error(features, target);
//            errorHistory.add(Math.sqrt(error));
//            double rate = alpha
//                    * Math.pow(1-anneal, nn.getTrainCount() / 1000.);
//            nn.setLearningRate(rate);
//            nn.train(features, target);
//        }
//        
//    }
//
//
//    private void extractFeatures(String key) {
//        for (int i = 0; i < key.length(); i++) {
//            features[i] = Integer.parseInt(key.substring(i, i+1), 16);
//        }
//        for (int i = key.length(); i < features.length; i++) {
//            features[i] = 0.0;
//        }
//    }
//
//    double calcValue(TrainingSetup s) throws CannotDecideException, LimitException {
//
//        String key = makeKey(s);
//        if(boardTable.containsKey(key)) {
//            return boardTable.get(key);
//        }
//        
//        Mode cat = CombiAI.getCategory(s, s.getActivePlayer());        
//        if(cat == Mode.BEAROFF) {
//            // bearoff database ... optimal values!
//            double result = bearoffAi.getEstimatedMoves(s, s.getActivePlayer());
//            boardTable.put(key, result);
//            return result;
//        }
//        
//        double sum = 0.;
//        for (int i = 1; i <= 6; i++) {
//            for (int j = i; j <= 6; j++) {
//                s.roll(i, j);
//                double bestValue = getBestMoveValue(s);
//                sum += bestValue;
//                if(i != j) {
//                    // only double throws appear once, others have double prob.
//                    sum += bestValue;
//                }
//            }
//        }
//
//        double result = (sum / 36.) + 1;
//        boardTable.put(key, result);
//        if((boardTable.size() % 1000) == 0)
//            System.out.println(boardTable.size());
//        if(boardTable.size() > limit)
//            throw new LimitException();
//        return result;
//    }
//
//    private String makeKey(BoardSetup s) {
//        int p = s.getActivePlayer();
//        int max = s.getMaxPoint(p);
//        StringBuilder sb = new StringBuilder();
//        for (int i = 0; i <= max; i++) {
//            sb.append(Integer.toHexString(s.getPoint(p, i)));
//        }
//        return sb.toString();
//    }
//
//    private double getBestMoveValue(TrainingSetup s)
//            throws CannotDecideException, LimitException {
//        PossibleMoves pm = new PossibleMoves(s);
//        double bestValue = Double.MAX_VALUE;
//        for (MoveChain move : pm.getPossibleMoveChains()) {
//            TrainingSetup setup;
//            setup = new TrainingSetup(s);
//            setup.performMoves(move);
//            double value = calcValue(setup);
//            if(value < bestValue) {
//                bestValue = value;
//            }
//        }
//        return bestValue;
//    }
//
//    public static void main(String[] args) throws Exception {
//        CommandLine cl = new CommandLine();
//        cl.addOption("-in", "file", "The serialised net to read in");
//        cl.addOption("-out", "file", "The serialised net to write to");
//        cl.addOption("-runs", "n", "Number of games to play (defaults to 10)");
//        cl.addOption("-limit", "n", "Number of positions to store per game (defaults to 100000)");
//        System.out.println("Command line options are:");
//        cl.printUsage(System.out);
//        cl.parse(args);
//        
//        RolloutRaceTrainer t = new RolloutRaceTrainer();
//        
//        if(!cl.isSet("-in")) {
//            throw new CommandLineException("-in must be set, train a new net with RaceTrainer");
//        }
//        
//        t.loadNet(cl.getString("-in", ""));
//        int runs = cl.getInteger("-runs", 10);
//        t.limit = cl.getInteger("-limit", t.limit);
//        
//        t.init();
//        t.train(runs);
//        
//        System.out.println(t.ai.getNeuralNet().toString());
//        
//        if(cl.isSet("-out")) {
//            t.saveNet(cl.getString("-out", ""));
//        }
//    }
//
//    private void init() throws Exception {
//        combiAI.init();
//        NeuralNet net = ai.getNeuralNet();
//        if (net instanceof MultiLayerNeuralNet) {
//            MultiLayerNeuralNet mln = (MultiLayerNeuralNet) net;
//            mln.enableTraining();
//        }
//    }
//
//    private void loadNet(String fileName) throws IOException, ClassNotFoundException {
//        ObjectInputStream ois = new ObjectInputStream(
//                new FileInputStream(fileName));
//        NeuralNet net = (NeuralNet) ois.readObject();
//        ai.setNeuralNet(net);
//        ois.close();
//    }
//    
//    private void saveNet(String filename) throws IOException {
//        FileOutputStream fos = new FileOutputStream(filename);
//        ObjectOutputStream os = new ObjectOutputStream(fos);
//        os.writeObject(ai.getNeuralNet());
//        os.close();
//    }
//
//}
