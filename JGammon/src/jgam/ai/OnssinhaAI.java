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
        int player = bs.getPlayerAtMove();
    
        
        long startTime = System.currentTimeMillis();
        long timeLimit = 4000; 
        timeCutoff = startTime + timeLimit;
    
        double bestEval = Double.NEGATIVE_INFINITY;
        int bestMoveIndex = -1;
    
        PossibleMoves pm = new PossibleMoves(bs);
        List<BoardSetup> moveList = pm.getPossbibleNextSetups();    
        int depth = moveList.size() > 12 ? 2 : 3;
    
        for (int i = 0; i < moveList.size(); i++) {
            if (System.currentTimeMillis() - startTime > timeLimit - 1000) { 
                break; 
            }
            BoardSetup boardSetup = moveList.get(i);
            double evaluation = minimax(boardSetup, depth, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, new int[]{0,0});
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

    private double minimax(BoardSetup bs, int depth, double alpha, double beta, int[] maximizingPlayer) {
    
        if (depth == 0 || bs.getOff(1) == 15 || bs.getOff(2) == 15 || System.currentTimeMillis() > timeCutoff) {
            if(depth==0)
                System.out.println("Depth 0");
            return heuristica(bs);
        }
    
        double minEval = Double.POSITIVE_INFINITY;
        double maxEval = Double.NEGATIVE_INFINITY;
    
        BoardSetup bsAuxiliar = bs;
        int[][] combinacoesDados = {{1, 1}, {1, 2}, {1, 3}, {1, 4}, {1, 5}, {1, 6}, {2, 2}, {2, 3}, {2, 4}, {2, 5}, {2, 6}, {3, 3}, {3, 4}, {3, 5}, {3, 6}, {4, 4}, {4, 5}, {4, 6}, {5, 5}, {5, 6}, {6, 6}};
        if (maximizingPlayer[0] == 0) {
            PossibleMoves pm = new PossibleMoves(bsAuxiliar);
            List<BoardSetup> moveList = pm.getPossbibleNextSetups();
            for (BoardSetup child : moveList) {
                int[] newMaximizingPlayer = {2, 0};
                double eval = minimax(child, depth - 1, alpha, beta, newMaximizingPlayer);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha || System.currentTimeMillis() > timeCutoff) {
                    System.out.println("poda");
                    break;
                }
            }
            return maxEval;
        } else if (maximizingPlayer[0] == 1) {
            PossibleMoves pm = new PossibleMoves(bsAuxiliar);
            List<BoardSetup> moveList = pm.getPossbibleNextSetups();
            for (BoardSetup child : moveList) {
                int[] newMaximizingPlayer = {2, 1};
                double eval = minimax(child, depth - 1, alpha, beta, newMaximizingPlayer);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha || System.currentTimeMillis() > timeCutoff) {
                    System.out.println("poda");
                    break;
                }
            }
            return minEval;
        } else {
            int N = combinacoesDados.length;
            double sum = 0;
            double U = Double.POSITIVE_INFINITY;
            double L = Double.NEGATIVE_INFINITY;
            double A = N * (alpha - U) + U;
            double B = N * (beta - L) + L;

            for (int i = 0; i < N; i++) {
                PossibleMoves pm = new PossibleMoves(bsAuxiliar);
                pm.AuxPossibleMoves(combinacoesDados[i][0], combinacoesDados[i][1]);
                List<BoardSetup> moveList = pm.getPossbibleNextSetups();
                for (BoardSetup child : moveList) {
                    double AX = Math.max(A, L);
                    double BX = Math.min(B, U);
                    double eval = minimax(child, depth - 1, AX, BX, maximizingPlayer[1] == 0 ? new int[]{1, 2} : new int[]{0, 2});
                    if (combinacoesDados[i][0] == combinacoesDados[i][1]) {
                        eval *= 1.0 / 36;
                    } else {
                        eval *= 1.0 / 18;
                    }
                    sum += eval;
                    if (sum / N <= A) {
                        return alpha;
                    }
                    if (sum / N >= B) {
                        return beta;
                    }
                    A += U - eval;
                    B += L - eval;
                }
            }
            return sum / N;
        }
        
    }
    

    public int rollOrDouble(BoardSetup boardSetup) throws CannotDecideException {
        return ROLL;
    }

    public int takeOrDrop(BoardSetup boardSetup) throws CannotDecideException {
        return TAKE;
    }
}
