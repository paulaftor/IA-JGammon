package jgam.ai;

import jgam.game.BoardSetup;
import jgam.game.PossibleMoves;
import jgam.game.SingleMove;

import java.util.Iterator;
import java.util.List;

/**
 * Created by Bruno on 29/05/2017.
 */
public class Blocker_AI implements AI {
    public void init() throws Exception {

    }

    public void dispose() {

    }

    public String getName() {
        return "Blocker AI";
    }

    public String getDescription() {
        return "Blocker IA";
    }

    private double heuristic(BoardSetup bs) {
        double evaluation = 0.0;

        int player = bs.getPlayerAtMove();

        for (int i = 1; i < 25; i++) {
            int numCheckers = bs.getPoint(player, i);
            if (numCheckers == 2)
                evaluation += 100.0;
            else if (numCheckers == 1)
                evaluation -= 75.0;
            else
                evaluation -= 25.0 * numCheckers;
        }

        int totalPoints = bs.getPoint(player, 25);
        evaluation += 50.0 * totalPoints;

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
            double thisEvaluation = heuristic(boardSetup);
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
