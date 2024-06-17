package jgam.ai;

import jgam.game.BoardSetup;
import jgam.game.MoveChain;
import jgam.game.PossibleMoves;
import jgam.game.SingleMove;

import java.util.Iterator;
import java.util.List;

public class OnssasAI implements AI {
    public void init() throws Exception {

    }

    public void dispose() {

    }

    public String getName() {
        return "Onssas AI";
    }

    public String getDescription() {
        return "Onssas' first AI attempt";
    }

    private double heuristica(BoardSetup bs) {
        double eval = 0.0;

        int player = bs.getPlayerAtMove();
        int opponent = 3 - player;

        int checkersInLastSix = 0;
        for (int i = 1; i < 25; i++) {
            int numCheckers = bs.getPoint(player, i);
            int opponentNumCheckers = bs.getPoint(opponent, i);

            if (opponentNumCheckers == 1)
                eval += 75.0;
            else if (opponentNumCheckers == 0)
                eval += 25.0;
            else
                eval -= 100.0;
            
            if (numCheckers >= 3)
                eval -= 10.0 * numCheckers;
            else if (numCheckers == 2)
                eval += 100.0;
            else if (numCheckers == 1)
                eval -= 75.0;
            else
                eval -= 25.0 * numCheckers;

            if ((player == 1 && i >= 19) || (player == 2 && i <= 6))
                checkersInLastSix += numCheckers;
        }

        if (checkersInLastSix == 15)
            eval += 1000.0;
        else if (checkersInLastSix >= 12)
            eval += 100.0;
        else if (checkersInLastSix >= 9)
            eval += 50.0;
        else if (checkersInLastSix >= 6)
            eval += 20.0;
        else if (checkersInLastSix >= 3)
            eval += 10.0;

        int totalPoints = bs.getPoint(player, 25);
        eval += 50.0 * totalPoints;

        return eval;
    }

    public MoveChain makeMoves(BoardSetup bs) throws CannotDecideException {
        double eval = Double.NEGATIVE_INFINITY;
        int movimento = -1;

        PossibleMoves pm = new PossibleMoves(bs);
        List moveList = pm.getPossbibleNextSetups();

        int i = 0;
        for (Iterator iter = moveList.iterator(); iter.hasNext(); i++) {
            BoardSetup boardSetup = (BoardSetup) iter.next();
            double thisEvaluation = heuristica(boardSetup);
            if (thisEvaluation > eval) {
                eval = thisEvaluation;
                movimento = i;
            }
        }

        if (movimento == -1)
            return new MoveChain();
        else
            return pm.getMoveChain(movimento);
    }

    public int rollOrDouble(BoardSetup boardSetup) throws CannotDecideException {
        return ROLL;
    }

    public int takeOrDrop(BoardSetup boardSetup) throws CannotDecideException {
        return TAKE;
    }
}