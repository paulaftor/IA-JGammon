package jgam.ai;

import jgam.game.BoardSetup;
import jgam.game.MoveChain;
import jgam.util.ProgressMonitor;

/**
 * Use this AI to wrap another AI and and use rollout to evalutate a board
 * 
 * @author Mattias Ulbrich
 * 
 */
public abstract class RolloutAI extends EvaluatingAI {

    protected AI wrappedAI;

    private int rounds = 50;

    /**
     * create a new RolloutAI and wrap a specific AI. wrappedAI does not need to
     * be initialized.
     * 
     * @param wrappedAI AI to wrap
     */
    protected RolloutAI(AI wrappedAI) {
        this.wrappedAI = wrappedAI;
    }

    public double probabilityToWin(BoardSetup setup)
            throws CannotDecideException {

        ProgressMonitor pm = new ProgressMonitor(null,
                "Please hold on. Rolling out ...", null, 0, rounds);

        if (rounds != -1) {
            return rollout(pm, setup);
        } else {

            if (wrappedAI instanceof EvaluatingAI) {
                EvaluatingAI evalAI = (EvaluatingAI) wrappedAI;
                return evalAI.probabilityToWin(setup);
            }
        }

        throw new CannotDecideException(
                "cannot determine the probability, aborted");
    }

    private double rollout(ProgressMonitor pm, BoardSetup setup)
            throws CannotDecideException {

        int sum = 0;
        RolloutSetup rs = new RolloutSetup(wrappedAI);

        for (int i = 0; i < rounds; i++) {
            if (pm != null) {
                if (pm.isCanceled()) {
                    return i == 0 ? .5 : ((double) sum) / i;
                }
                pm.setProgress(pm.getProgress() + 1);
            }

            rs.assignFrom(setup);
            sum += rs.rollout();

        }
        return ((double) sum) / rounds;
    }

    public MoveChain makeMoves(BoardSetup boardSetup)
            throws CannotDecideException {

            return wrappedAI.makeMoves(boardSetup);

    }

    /**
     * get the number of rollout games that this object uses. Returns -1 if the
     * rollout mechanism is switched off
     * 
     * @return number of games >= 1 or -1 if switched off.
     */
    public int getRounds() {
        return rounds;
    }

    /**
     * set the number of games used during rollout. -1 to switch the rollout
     * mechnism off.
     * 
     * @param rounds Rounds or -1
     */
    public void setRounds(int rounds) {
        if (rounds < -1 || rounds == 0)
            throw new IllegalArgumentException("rounds is illegal: " + rounds);
        this.rounds = rounds;
    }

    /**
     * init the wrapped AI.
     */
    public void init() throws Exception {
        wrappedAI.init();
    }

    /**
     * dispose the wrapped AI.
     */
    public void dispose() {
        wrappedAI.dispose();
    }

    /**
     * try to use the rollout mechnism to evaluate the situtation. If not
     * possible, delegate to wrapped AI
     * 
     * @param boardSetup setup to evaluate
     * @throws CannotDecideException only if the the wrappedAI also fails
     */
    public int rollOrDouble(BoardSetup boardSetup) throws CannotDecideException {
        if(rounds == -1)
            return wrappedAI.rollOrDouble(boardSetup);
        
        try {
            return super.rollOrDouble(boardSetup);
        } catch (CannotDecideException ex) {
            return wrappedAI.rollOrDouble(boardSetup);
        }
    }

    /**
     * try to use the rollout mechnism to evaluate the situtation. If not
     * possible, delegate to wrapped AI
     * 
     * @param boardSetup setup to evaluate
     * @throws CannotDecideException only if the the wrappedAI also fails
     */
    public int takeOrDrop(BoardSetup boardSetup) throws CannotDecideException {
        if(rounds == -1) 
            return wrappedAI.takeOrDrop(boardSetup);
        
        try {
            return super.takeOrDrop(boardSetup);
        } catch (CannotDecideException ex) {
            return wrappedAI.takeOrDrop(boardSetup);
        }
    }

    public String getName() {
        return "Rollout" + wrappedAI.getName();
    }

    public String getDescription() {
        return "Rolling out " + wrappedAI.getDescription();
    }

}
