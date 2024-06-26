package jgam.ai;

import jgam.game.BoardSetup;
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

        int checkersInLastHalf = 0;
        int checkersInFirstSix = 0;

        for (int i = 1; i < 25; i++) {
            int numCheckers = bs.getPoint(player, i);
            int opponentNumCheckers = bs.getPoint(opponent, i);

            if (opponentNumCheckers == 1)
                eval += 80.0;
            else if (opponentNumCheckers == 0)
                eval += 40.0;
            else
                eval -= 150.0;

            if (numCheckers >= 4)
                eval -= 30.0 * numCheckers;
            else if (numCheckers >= 2)
                eval += 200.0;
            else if (numCheckers == 1)
                eval -= 250.0;

            if ((player == 1 && i >= 13) || (player == 2 && i <= 12))
                checkersInLastHalf += numCheckers;

            if ((player == 1 && i <= 6) || (player == 2 && i >= 19))
                checkersInFirstSix += numCheckers;

            if ((player == 1 && i <= 13) || (player == 2 && i >= 12))
                eval -= 20.0 * numCheckers;
            else
                eval += 20.0 * numCheckers;
        }

        if (checkersInLastHalf == 15)
            eval += 1000.0;
        else if (checkersInLastHalf >= 12)
            eval += 400.0;
        else if (checkersInLastHalf >= 9)
            eval += 250.0;
        else if (checkersInLastHalf >= 6)
            eval += 40.0;
        else if (checkersInLastHalf >= 3)
            eval += 15.0;

        if (checkersInFirstSix == 0)
            eval += 1000.0;
        else if (checkersInFirstSix == 1)
            eval -= 500.0;
        else if (checkersInFirstSix >= 2)
            eval -= 1000.0;

        if (player == 1)
            eval -= 1000.0 * bs.getPoint(1, 24);
        else
            eval -= 1000.0 * bs.getPoint(2, 1);

        int totalPoints = bs.getPoint(player, 25);
        eval += 500.0 * totalPoints;

        return eval;
    }

    public SingleMove[] makeMoves(BoardSetup bs) throws CannotDecideException {
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
            return new SingleMove[0];
        else
            return pm.getMoveChain(movimento);
    }

    public boolean vantagemClara(BoardSetup boardSetup, int player) {
        int opponent = 3 - player;
        int checkersInLastHalf = 0;
        int opponentCheckersInLastHalf = 0;

        for (int i = 1; i < 25; i++) {
            int numCheckers = boardSetup.getPoint(player, i);
            int opponentNumCheckers = boardSetup.getPoint(opponent, i);

            if ((player == 1 && i >= 13) || (player == 2 && i <= 12))
                checkersInLastHalf += numCheckers;

            if ((opponent == 1 && i >= 13) || (opponent == 2 && i <= 12))
                opponentCheckersInLastHalf += opponentNumCheckers;
        }

        if (checkersInLastHalf == 15 && opponentCheckersInLastHalf < 12)
            return true;
        else
            return false;
    }

    public int rollOrDouble(BoardSetup boardSetup) throws CannotDecideException {
        int player = boardSetup.getPlayerAtMove();
        if(vantagemClara(boardSetup, player)){
            System.out.println("Vantagem clara encontrada pela " + getName() + ".");
            return DOUBLE;
        }    
        else
            return ROLL;
    }

    public int takeOrDrop(BoardSetup boardSetup) throws CannotDecideException {
        int opponent = 3-boardSetup.getPlayerAtMove();
        if(vantagemClara(boardSetup, opponent)){
            System.out.println("Double recusado pela " + getName() + ".");
            return DROP;
        }    
        else
            return TAKE;
    }
}
