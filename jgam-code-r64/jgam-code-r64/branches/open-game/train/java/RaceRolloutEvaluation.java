import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import jgam.FileBoardSetup;
import jgam.ai.AI;
import jgam.ai.BearOffAI;
import jgam.ai.CannotDecideException;
import jgam.ai.CombiAI;
import jgam.ai.CombiAI.Mode;
import jgam.ai.InitialAI;
import jgam.ai.RaceNeuralNetAI;
import jgam.game.ArrayBoardSetup;
import mattze.ann.NeuralNet;
import util.AverageBuffer;

/**
 * <p> Title: JGam - Java Backgammon </p>
 * 
 * <p> Description: </p>
 * 
 * <p> Copyright: Copyright (c) 2005 </p>
 * 
 * <p> Company: </p>
 * 
 * @author Mattias Ulbrich
 * @version 1.0
 */
public class RaceRolloutEvaluation {

    // how many games
    private int rounds = 100;

    public RaceNeuralNetAI aiUnderTest = new RaceNeuralNetAI();
    public AI rolloutAI;
    public AverageBuffer errorHistory = new AverageBuffer(200);
    private BearOffAI bearoff;
    
    public RaceRolloutEvaluation() {
        CombiAI combi = new CombiAI();
        combi.setAIForCategory(Mode.NORMAL, new InitialAI());
        combi.setAIForCategory(Mode.RACE, aiUnderTest);
        bearoff = new BearOffAI();
        combi.setAIForCategory(Mode.BEAROFF, bearoff);
        combi.setAIForCategory(Mode.UNSEPARATED_BEAROFF, bearoff);
        rolloutAI = combi;
    }

    public void eval() throws Exception {
        TrainingSetup basis = new TrainingSetup(true);
        basis.setPoint(1, 19, 1);
        basis.setPoint(2, 1, 1);
        basis.setPlayerAtMove(1);
        basis.roll(1, 2);
        int count = 0;
        while (++count < 100) {

            TrainingSetup setup = new TrainingSetup();

            while (!setup.isSeparated()) {
                do {
                    setup.roll();
                } while (setup.undecidedPlayer());
                setup.performMoves(rolloutAI.makeMoves(setup));
                setup.switchPlayers();
            }

            //
            // play a game
            //
            while (CombiAI.getCategory(setup, setup.getPlayerAtMove()) == Mode.RACE) {
                
                double aiEst = aiUnderTest.getEstimatedMoves(setup, setup.getPlayerAtMove());
                double rollout = rollout(setup, rounds);
                
                int pip = setup.calcPip(setup.getPlayerAtMove());
                double regr = 2.55716 + .11427*pip;
                System.out.printf("#race pip: %3d roll: %5.2f regr: %5.2f (%5.2f) ai: %5.2f (%5.2f)\n",
                        pip, rollout,
                        regr,
                        regr - rollout,
                        aiEst,
                        aiEst - rollout);
                System.out.println(pip +" " + rollout + " " + regr + " " + aiEst);
                
                errorHistory.add(rollout - aiEst);
                
                setup.performMoves(rolloutAI.makeMoves(setup));
                setup.roll();
            }
            
            while(setup.winstatus() == 0) {
                double aiEst = bearoff.getEstimatedMoves(setup, setup.getPlayerAtMove());
                double rollout = rollout(setup, rounds);
                
                int pip = setup.calcPip(setup.getPlayerAtMove());
                double regr = 2.55716 + .11427*pip;
                System.out.printf("#boff pip: %3d roll: %5.2f regr: %5.2f (%5.2f) ai: %5.2f (%5.2f)\n",
                        pip, rollout,
                        regr,
                        regr - rollout,
                        aiEst,
                        aiEst - rollout);
                System.out.println(pip +" " + rollout + " " + regr + " " + aiEst);
                
                errorHistory.add(rollout - aiEst);
                
                setup.performMoves(rolloutAI.makeMoves(setup));
                setup.roll();
            }
//            System.out.println("#Errors: " + errorHistory);
        }
    }

    private double rollout(TrainingSetup trainingSetup, int rounds)
            throws CannotDecideException {

        TrainingSetup t = new TrainingSetup();
        int count = 0;
        for (int i = 0; i < rounds; i++) {
            t.copyFrom(trainingSetup);
            while (t.winstatus() == 0) {
                t.roll();
                t.performMoves(rolloutAI.makeMoves(t));
                count ++;
            }
        }
        
        double ret = ((double)count) / rounds;
        return ret;

    }

    public static void main(String[] args) throws Exception {
        RaceRolloutEvaluation t = new RaceRolloutEvaluation();
        t.loadNet(args[0]);
        t.init();
        t.eval();
    }
    
    private void init() throws Exception {
        rolloutAI.init();
    }
    
    private void loadNet(String fileName) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(fileName));
        aiUnderTest.setNeuralNet((NeuralNet) ois.readObject());
        ois.close();
    }

}
