package jgam.ai;

import jgam.game.BoardSetup;
import jgam.game.PossibleMoves;
import jgam.game.SingleMove;

import java.util.Iterator;
import java.util.List;

/**
 * Created by Bruno on 29/05/2017.
 */
public class Runner_AI implements AI {
    public void init() throws Exception {

    }

    public void dispose() {

    }

    public String getName() {
        return "Runner AI";
    }

    public String getDescription() {
        return "Runner IA";
    }

    private double heuristicPart1(BoardSetup bs) {
        double evaluation = 0.0;

        int player = bs.getPlayerAtMove();
        int opponent = 3 - player;

        for (int i = 1; i < 25; i++) {
            int numCheckers = bs.getPoint(player, i);
            if (numCheckers > 3 || numCheckers == 1)
                evaluation -= numCheckers * 25.0;
            else
                evaluation += numCheckers * 50.0;

        }
        int totalPoints = bs.getPoint(player, 0);
        evaluation -= totalPoints * 50.0;

        totalPoints = bs.getPoint(player, 25);
        evaluation += totalPoints * 25.0;

        return evaluation;
    }

    public SingleMove[] makeMoves(BoardSetup bs) throws CannotDecideException {
        double evaluation = Double.NEGATIVE_INFINITY;
        int decision = -1;

        PossibleMoves pm = new PossibleMoves(bs);
        List moveList = pm.getPossbibleNextSetups();

        int i = 0;
        for (Iterator iter = moveList.iterator(); iter.hasNext(); i++) {
            BoardSetup boardSetup = (BoardSetup) iter.next();
            double thisEvaluation = heuristicPart1(boardSetup);
            if (thisEvaluation > evaluation) {
                evaluation = thisEvaluation;
                decision = i;
            }
        }

        if (decision == -1)
            return new SingleMove[0];
        else
            return pm.getMoveChain(decision);
    }

    public int rollOrDouble(BoardSetup boardSetup) throws CannotDecideException {
        return ROLL;
    }

    public int takeOrDrop(BoardSetup boardSetup) throws CannotDecideException {
        return TAKE;
    }
}
