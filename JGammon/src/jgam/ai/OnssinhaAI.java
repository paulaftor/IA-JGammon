package jgam.ai;

import jgam.game.BoardSetup;
import jgam.game.PossibleMoves;
import jgam.game.SingleMove;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class OnssinhaAI implements AI {
    private Map<String, Double> memoizationMap = new HashMap<>();
    private long timeCutoff; // Cutoff time in milliseconds


    public void init() throws Exception {

    }

    public void dispose() {

    }

    public String getName() {
        return "Onssinha AI";
    }

    public String getDescription() {
        return "Onssinha";
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
                eval += 15.0;
            else if (opponentNumCheckers == 0)
                eval += 10.0;
            else
                eval -= 20.0;

            if (numCheckers >= 4)
                eval -= 15.0 * numCheckers;
            else if (numCheckers >= 2)
                eval += 30.0;
            else if (numCheckers == 1)
                eval -= 50.0;

            if ((player == 1 && i >= 13) || (player == 2 && i <= 12))
                checkersInLastHalf += numCheckers;

            if ((player == 1 && i <= 6) || (player == 2 && i >= 19))
                checkersInFirstSix += numCheckers;

            if ((player == 1 && i <= 13) || (player == 2 && i >= 12))
                eval -= 10.0 * numCheckers;
            else
                eval += 10.0 * numCheckers;
        }

        if (checkersInLastHalf == 15)
            eval += 50.0;
        else if (checkersInLastHalf >= 12)
            eval += 30.0;
        else if (checkersInLastHalf >= 9)
            eval += 20.0;
        else if (checkersInLastHalf >= 6)
            eval += 10.0;
        else if (checkersInLastHalf >= 3)
            eval += 5.0;

        if (checkersInFirstSix == 0)
            eval += 60.0;
        else if (checkersInFirstSix == 1)
            eval -= 70.0;
        else if (checkersInFirstSix >= 2)
            eval -= 90.0;

        if (player == 1)
            eval -= 30.0 * bs.getPoint(1, 24);
        else
            eval -= 30.0 * bs.getPoint(2, 1);

        int totalPoints = bs.getPoint(player, 25);
        eval += 10.0 * totalPoints;

        return eval;
    }

    public SingleMove[] makeMoves(BoardSetup bs) throws CannotDecideException {
        int player = bs.getPlayerAtMove();
    
        int depth = 3;
        long startTime = System.currentTimeMillis();
        long timeLimit = 20000; 
        timeCutoff = startTime + timeLimit;
    
        double bestEval = Double.NEGATIVE_INFINITY;
        int bestMoveIndex = -1;
    
        PossibleMoves pm = new PossibleMoves(bs);
        List<BoardSetup> moveList = pm.getPossbibleNextSetups();
    
        for (int i = 0; i < moveList.size(); i++) {
            BoardSetup boardSetup = moveList.get(i);
            double evaluation = minimax(boardSetup, depth, -10000000, 10000000, false);
            if (evaluation > bestEval) {
                bestEval = evaluation;
                bestMoveIndex = i;
            }
        }
    
        if (bestMoveIndex == -1) {
            return new SingleMove[0];
        } else {
            return pm.getMoveChain(bestMoveIndex);
        }
    }
    
    private double minimax(BoardSetup bs, int depth, double alpha, double beta, boolean maximizingPlayer) {
        String key = bs.hashCode() + ":" + depth + ":" + maximizingPlayer;
        if (memoizationMap.containsKey(key)) {
            return memoizationMap.get(key);
        }

        if (System.currentTimeMillis() > timeCutoff) {
            return heuristica(bs);
        }

        if (depth == 0 || bs.getPoint(1, 0)==15 || bs.getPoint(2, 0)==15) {
            return heuristica(bs);
        }

        double minEval = 1000000000;
        double maxEval = -1000000000;

        if (maximizingPlayer) {
            PossibleMoves pm = new PossibleMoves(bs);
            List<BoardSetup> moveList = pm.getPossbibleNextSetups();
    
            for (BoardSetup child : moveList) {
                double eval = minimax(child, depth - 1, alpha, beta, false);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) {
                    break; // Poda beta
                }
            }
            //return maxEval;
        } else {
            PossibleMoves pm = new PossibleMoves(bs);
            List<BoardSetup> moveList = pm.getPossbibleNextSetups();
    
            for (BoardSetup child : moveList) {
                double eval = minimax(child, depth - 1, alpha, beta, true);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) {
                    break; // Poda alpha
                }
            }
            //return minEval;
        }

        memoizationMap.put(key, (maximizingPlayer ? maxEval : minEval));
        return maximizingPlayer ? maxEval : minEval;
    }

    public int rollOrDouble(BoardSetup boardSetup) throws CannotDecideException {
        return ROLL;
    }

    public int takeOrDrop(BoardSetup boardSetup) throws CannotDecideException {
        return TAKE;
    }
}
