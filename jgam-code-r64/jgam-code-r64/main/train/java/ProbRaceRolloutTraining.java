import java.io.*;
import java.util.*;

import jgam.*;
import jgam.ai.*;
import jgam.game.*;
import mattze.ann.*;
import util.Vec;

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
public class ProbRaceRolloutTraining extends Thread implements ConsoleApp {

    boolean interrupted = false;

    SigmoidSimpleNeuralNet neuralnet;

    RandomAI randomAi;

    PrintWriter dbgWriter;

    PrintWriter logWriter;

    boolean suspendWish = false;

    BoardSetup startSetup = BoardSnapshot.INITIAL_SETUP;

    BoundedDoubleWindow errorWindow = new BoundedDoubleWindow(400);

    public int debuglevel = 0;

    // initial learning rate
    double alpha = .1;

    // learning rate annealing
    double anneal = .3;

    // rate to make random moves.
    double exploration = .1;

    Timer timer;

    private double[] features = new double[ProbRaceNeuralNetAI.NUMBER_OF_RACE_FEATURES];

    private BearOffAI boAI;

    private ProbRaceNeuralNetAI raceAI;

    private InitialAI detai;

    // how many rounds
    // some research: 1000 rounds for .06 interval of 95% confidence
    private int rounds = 1000;

    // into which depth at max
    private int depth = 40;

    private int offset = 0;

    // size of hidden layer
    private int hidden = 10;

    public ProbRaceRolloutTraining() throws Exception {
        super("Probability Race Rollout Trainer");
        randomAi = new RandomAI();
        randomAi.init();
        boAI = new BearOffAI();
        boAI.init();
        detai = new InitialAI();
        detai.init();
    }

    public void run() {

        Random random = new Random();
        int games = 0;

        try {
            while (true) {

                TrainingSetup setup = new TrainingSetup(startSetup);
                debugout(1, "Games " + games + "  Setups "
                        + neuralnet.getTrainCount());

                //
                // play till separated using the predefined player
                while (!setup.isSeparated()) {
                    do {
                        setup.roll();
                    } while (setup.undecidedPlayer());
                    setup.performMoves(detai.makeMoves(setup));
                    setup.switchPlayers();
                }

                //
                // play a game
                //
                while (setup.winstatus() == 0) {
                    do {
                        setup.roll();
                    } while (setup.undecidedPlayer());

                    // exploit step

                    debugout(3, setup);
                    debugout(3, "Current Value: "
                            + raceAI.probabilityToWin(setup));

                    Vec mywin = rollout(setup, rounds);
                    train(setup, mywin);

                    if (random.nextDouble() < exploration) {
                        // exploration move!!
                        debugout(2, "Exploration step");
                        setup.performMoves(randomAi.makeMoves(setup));
                    } else {
                        if (setup.getMaxPoint(setup.getPlayerAtMove()) <= 6) {
                            setup.performMoves(boAI.makeMoves(setup));
                        }
                        else {
                            setup.performMoves(raceAI.makeMoves(setup));
                        }
                    }

                    setup.switchPlayers();
                    checkSuspendWish();
                }
                games++;
                debugout(3, setup);
                debugout(3, "is lost, prob: " + raceAI.probabilityToWin(setup));
                train(setup, new Vec(0,0,0));

                debugout(1, "Sqr mean error after " + games + " games " +
                        "(" + errorWindow.size() + " pos): " +
                        errorWindow.mean());
            }
        } catch (CannotDecideException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * train
     */
    private void train(BoardSetup setup, Vec target) {

        raceAI.getFeatures(setup, features);

        // project onto .1 .. .9
        target = target.map(x -> x * .8 + .1);

        synchronized (neuralnet) {
            double rate = alpha
                    / Math.pow(
                        (neuralnet.getTrainCount() - offset) / 1000. + 1.,
                        anneal);
            neuralnet.setLearningRate(rate);

            debugout(3, "Learning rate: " + rate);
            Vec oldval = new Vec(neuralnet.applyTo(features));

            neuralnet.train(features, target.toArray());

            Vec newval = new Vec(neuralnet.applyTo(features));
            Vec error = oldval.sub(target);

            debugout(2, String.format("Train  %s -> %s ==> %s (%s)",
                    oldval.toString("%.3g"),
                    target.toString("%.3g"),
                    newval.toString("%.3g"),
                    error.toString("%.3g")));

            errorWindow.push(error.sqrSum());

            if (logWriter != null) {
                //logWriter.println("" + neuralnet.applyTo(features)[0] + " "
                //        + target[0] + " " + rate);
                // logWriter.flush();
            }
            //System.out.printf("%d %d %g%n",
            //        setup.calcPip(setup.getPlayerAtMove()),
            //        setup.calcPip(3 - setup.getPlayerAtMove()),
            //        target.get(0));
        }
    }

    public void setLog(PrintWriter w) {
        dbgWriter = w;
    }

    private void debugout(int level, Object o) {
        if (debuglevel >= level) {
            if (o instanceof BoardSetup) {
                ((BoardSetup) o).debugOut(dbgWriter);
            } else {
                dbgWriter.println(o);
            }
            dbgWriter.flush();
        }
    }

    void eval(int number) {
        try {
            TrainingSetup setup = new TrainingSetup(startSetup);

            //
            // play till separated using the predefined player
            while (!setup.isSeparated()) {
                do {
                    setup.roll();
                } while (setup.undecidedPlayer());
                setup.performMoves(detai.makeMoves(setup));
                setup.switchPlayers();
            }

            while (setup.winstatus() == 0) {
                debugout(0, "" + rollout(setup, 25) + " "
                        + raceAI.probabilityToWin(setup));

                setup.roll();
                setup.performMoves(raceAI.makeMoves(setup));
                setup.switchPlayers();
            }
        } catch (CannotDecideException ex) {
            ex.printStackTrace(dbgWriter);
        }
    }

    private Vec rollout(TrainingSetup trainingSetup, int rounds)
            throws CannotDecideException {

        TrainingSetup t = new TrainingSetup();
        double wincount = 0;
        double wonGammon = 0.;
        double lostGammon = 0.;

        int i;
        for (i = 0; i < rounds; i++) {
            t.copyFrom(trainingSetup);
            int initialplayer = t.getPlayerAtMove();
            int cnt = 0;

            while (t.winstatus() == 0 && cnt < 2 * depth) {
                // t.debugOut();
                t.roll();
                if (t.getMaxPoint(t.getPlayerAtMove()) <= 6)
                    t.performMoves(boAI.makeMoves(t));
                else
                    t.performMoves(raceAI.makeMoves(t));

                t.switchPlayers();
                cnt++;
            }

            if (t.winstatus() == initialplayer) {
                wincount++;
                if (t.isGammon()) {
                    wonGammon++;
                }
            } else if (t.winstatus() == 3 - initialplayer) {
                if(t.isGammon()) {
                    lostGammon++;
                }
            } else {
                // depth has been reached.
                assert t.getActivePlayer() == initialplayer;
                wincount += raceAI.probabilityToWin(t);
            }
        }

        return new Vec(wincount / rounds, wonGammon/rounds, lostGammon/rounds);

    }

    void save(String filename) {
        try {
            FileOutputStream fos = new FileOutputStream(filename);
            ObjectOutputStream os = new ObjectOutputStream(fos);

            synchronized (neuralnet) {
                os.writeObject(neuralnet);
            }

            os.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        ProbRaceRolloutTraining t = new ProbRaceRolloutTraining();
        if (args.length > 0) {
            new ConsoleFrame(t).setVisible(true);
        } else {
            ConsoleFrame.commandline(t, "Reinforcement JGAMMON Trainer Probability Race AI");
        }
    }

    /**
     * newNet
     * 
     * @throws Exception
     */
    public void newNet(double range) throws Exception {
        // New net!
        // TODO soll das aussehen?
        SigmoidSimpleNeuralNet neuralnet = new SigmoidSimpleNeuralNet(
                ProbRaceNeuralNetAI.NUMBER_OF_RACE_FEATURES, hidden, 3);
        neuralnet.randomize(range);
        setNet(neuralnet);
    }

    /**
     * setNet
     * 
     * @param neuralNetwork NeuralNetwork
     * @throws Exception by AI.init
     */
    public void setNet(SigmoidSimpleNeuralNet neuralNetwork) throws Exception {
        neuralnet = neuralNetwork;
        raceAI = new ProbRaceNeuralNetAI(neuralNetwork);
        raceAI.init();
    }

    /**
     * getNeuralNet
     * 
     * @return Object
     */
    public Object getNeuralNet() {
        return neuralnet;
    }

    synchronized void checkSuspendWish() {
        if (suspendWish) {
            try {
                wait();
            } catch (InterruptedException ex) {
            }
        }
    }

    public boolean command(String command) throws Exception {
        String args[] = command.split(" +", 2);

        if (command.equals("exit") || command.equals("quit")) {
            System.exit(0);
        } else

        if (args[0].equals("newnet")) {
            newNet(Double.parseDouble(args[1]));
        } else

        if (args[0].equals("loadnet")) {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
                    args[1]));
            setNet((SigmoidSimpleNeuralNet) ois.readObject());
            ois.close();
        } else

        if (args[0].equals("savenet")) {
            ObjectOutputStream ois = new ObjectOutputStream(
                    new FileOutputStream(args[1]));
            ois.writeObject(getNeuralNet());
        } else

        if (args[0].equals("dbg")) {
            try {
                debuglevel = Integer.parseInt(args[1]);
            } catch (Exception ex) {
                debuglevel = 0;
            }
        } else

        if (args[0].equals("log")) {
            if (logWriter != null) {
                logWriter.close();
            }
            if (args.length > 1) {
                logWriter = new PrintWriter(new FileWriter(args[1]));
            } else {
                logWriter = null;
            }
        } else

        if (command.equals("flushlog")) {
            logWriter.flush();
        } else

        if (command.startsWith("alpha ")) {
            alpha = Double.parseDouble(command.substring(6));
        } else

        if (args[0].equals("offset")) {
            offset = Integer.parseInt(args[1]);
        } else

        if (args[0].equals("anneal")) {
            anneal = Double.parseDouble(args[1]);
        } else

        if (args[0].equals("hidden")) {
            hidden = Integer.parseInt(args[1]);
        } else

        if (args[0].equals("rounds")) {
            rounds = Integer.parseInt(args[1]);
        } else

        if (args[0].equals("depth")) {
            depth = Integer.parseInt(args[1]);
        } else

        if (args[0].startsWith("explor")) {
            exploration = Double.parseDouble(args[1]);
        } else

        if (args[0].equals("eval")) {
            debugout(0, "hang on ... evaluating ...");
            new Thread() {
                public void run() {
                    eval(0);
                }
            }.start();

        } else

        if (command.startsWith("sus")) {
            suspendWish = true;
        } else

        if (command.equals("resume")) {
            synchronized (this) {
                suspendWish = false;
                notify();
            }
        } else

        if (command.equals("step") || command.equals("s")) {
            synchronized (this) {
                suspendWish = true;
                notify();
            }
        } else

        if (args[0].equals("setup")) {
            startSetup = new FileBoardSetup(new File(args[1]));
        } else

        if (command.equals("run")) {
            if (isAlive()) {
                dbgWriter.println("thread already running");
            } else {
                start();
            }
        } else

        if (args[0].equals("autosave")) {
            if (timer != null) {
                timer.cancel();
            }
            timer = new Timer();
            int sec = Integer.parseInt(args[1]) * 60000;
            timer.schedule(new TimerTask() {
                public void run() {
                    save("racerollout.autosave.ser");
                    debugout(1, ">> autosave " + new Date());
                }
            }, sec, sec);
        } else

        {
            return false;
        }

        return true;
    }
}
