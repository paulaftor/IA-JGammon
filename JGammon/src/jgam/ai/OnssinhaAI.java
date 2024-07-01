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

    private double heuristica(BoardSetup bs, int player) {
        double eval = 0.0;
        int opponent = 3 - player;

        int checkersProximasOff = 0;
        int checkersDistantesOff = 0;

        for (int i = 1; i < 25; i++) {
            int playerCheckers = bs.getPoint(player, i);
            //int opponentCheckers = bs.getPoint(opponent, i);

            // Caso tenha 4 ou mais peças na casa, subtrai 50 pontos por peça
            // Caso tenha 2 ou 3 peças, soma 200 pontos
            // Caso tenha 1 peça, subtrai 250 pontos
            if (playerCheckers >= 4)
                eval -= 50.0 * playerCheckers;
            else if (playerCheckers >= 2)
                eval += 200.0;
            else if (playerCheckers == 1) 
                eval -= 250.0;

            if (i <= 12)
                checkersProximasOff += playerCheckers;

            if (i >= 19)
                checkersDistantesOff += playerCheckers;

            if (i >= 12)
                eval -= 20.0 * playerCheckers;
            else
                eval += 20.0 * playerCheckers;
        }

        // Ajuda a manter as peças na última metade do tabuleiro
        // e a mandar uma peça comida para a última metade de forma
        // mais rápida.
        if (checkersProximasOff == 15)
            eval += 1000.0;
        else if (checkersProximasOff >= 12)
            eval += 400.0;
        else if (checkersProximasOff >= 9)
            eval += 250.0;
        else if (checkersProximasOff >= 6)
            eval += 40.0;
        else if (checkersProximasOff >= 3)
            eval += 15.0;

        // Caso tenha 1 peça, subtrai 500 pontos
        // Caso tenha 2 ou mais peças, subtrai 1000 pontos
        if (checkersDistantesOff == 1)
            eval -= 500.0;
        else if (checkersDistantesOff >= 2)
            eval -= 1000.0;

        // Faz de tudo pra tirar as duas ultimas pecas
        // da primeira posicao do tabuleiro para nao atrasa-las
        if (player == 1)
            eval -= 1000.0 * bs.getPoint(1, 24);
        else
            eval -= 1000.0 * bs.getPoint(2, 24);

        int bearedOffCheckers = bs.getOff(player);
        int opponentCheckersAtBar = bs.getBar(opponent);

        // Para o late game, utilidade máxima por peças fora do tabuleiro,
        // afinal, é o objetivo do jogo
        eval += 1000 * bearedOffCheckers + 200.0 * opponentCheckersAtBar;

        return eval;
    }

    public SingleMove[] makeMoves(BoardSetup bs) throws CannotDecideException {
        int player = bs.getPlayerAtMove();

        // Definir profundidade máxima da busca
        int depth = 1;

        // Chamar o algoritmo Minimax com alpha-beta pruning
        double bestEval = Double.NEGATIVE_INFINITY;
        int bestMoveIndex = -1;

        PossibleMoves pm = new PossibleMoves(bs);
        List<BoardSetup> moveList = pm.getPossbibleNextSetups();

        for (int i = 0; i < moveList.size(); i++) {
            BoardSetup boardSetup = moveList.get(i);
            double evaluation = heuristica(bs, player);
            evaluation -= minimax(boardSetup, depth, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, false);
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

        // Verificar se é um nó terminal ou atingiu a profundidade máxima
        if (depth == 0 || bs.getOff(1)==15 || bs.getOff(2)==15) {
            return heuristica(bs, 3-bs.getPlayerAtMove());
        }

        double minEval = Double.POSITIVE_INFINITY;
        double maxEval = Double.NEGATIVE_INFINITY;

        for(int dado1=1; dado1<=6; ++dado1) {
            for(int dado2=dado1; dado2<=6; ++dado2) {
                bs.setDice(dado1, dado2);

                if(maximizingPlayer){
                    PossibleMoves pm = new PossibleMoves(bs);
                    List<BoardSetup> moveList = pm.getPossbibleNextSetups();

                    for (BoardSetup child : moveList) {
                        double eval = minimax(child, depth - 1, alpha, beta, false);
                        maxEval = Math.max(maxEval, eval);
                        alpha = Math.max(alpha, eval);
                        System.out.println("Alpha: " + alpha + " Beta: " + beta);
                        if (beta <= alpha) {
                            break; // Poda beta
                        }
                    }
                } else {
                    PossibleMoves pm = new PossibleMoves(bs);
                    List<BoardSetup> moveList = pm.getPossbibleNextSetups();

                    for (BoardSetup child : moveList) {
                        double eval = minimax(child, depth - 1, alpha, beta, true);
                        minEval = Math.min(minEval, eval);
                        beta = Math.min(beta, eval);
                        System.out.println("Alpha 2: " + alpha + " Beta 2: " + beta);
                        if (beta <= alpha) {
                            break; // Poda alpha
                        }
                    }
                }

            }
        }

        return maximizingPlayer ? maxEval : minEval;
    }

    
    public boolean vantagemClara(BoardSetup boardSetup, int player) {
        int opponent = 3 - player;
        int checkersPertoOff = 0;
        int opponentCheckersPertoOff = 0;
        int playerCheckers = 0;
        int opponentCheckers = 0;
        int checkersLongeOff = 0;
        int opponentCheckersLongeOff = 0;

        for (int i = 1; i < 25; i++) {
            playerCheckers = boardSetup.getPoint(player, i);
            opponentCheckers = boardSetup.getPoint(opponent, i);

            if (i >= 13){
               checkersLongeOff += playerCheckers;
                opponentCheckersLongeOff += opponentCheckers;
            }
            if (i <= 6){
                checkersPertoOff += playerCheckers;
                opponentCheckersPertoOff += opponentCheckers;
            }
        }

        checkersLongeOff += boardSetup.getBar(player);
        opponentCheckersLongeOff += boardSetup.getBar(opponent);
        checkersPertoOff += boardSetup.getOff(player);
        opponentCheckersPertoOff += boardSetup.getOff(opponent);
        int diferencaOff = boardSetup.getOff(player) - boardSetup.getOff(opponent);
        int diferencaPertoOff = checkersPertoOff - opponentCheckersPertoOff;
        int diferencaLongeOff = opponentCheckersLongeOff - checkersLongeOff;
        if (diferencaOff > 4 || (diferencaPertoOff > 4 && checkersPertoOff > 10) || diferencaLongeOff > 3)
            return true;
        else
            return false;
    }

    /**
     * given a board make decide whether to roll or to double.
     *
     * @param boardSetup BoardSetup
     * @return either DOUBLE or ROLL
     * @throws CannotDecideException if the AI cannot decide which moves to make
     */
    @Override
    public int rollOrDouble(BoardSetup boardSetup) throws CannotDecideException {
        int player = boardSetup.getPlayerAtMove();
        if(vantagemClara(boardSetup, player))
            return DOUBLE;
        else
            return ROLL;
    }

    /**
     * given a board and a double offer, take or drop.
     * Evaluate the player whose turn it is NOT!
     *
     * @param boardSetup BoardSetup
     * @return either TAKE or DROP
     * @throws CannotDecideException if the AI cannot decide which moves to make
     */
    @Override
    public int takeOrDrop(BoardSetup boardSetup) throws CannotDecideException {
        int opponent = boardSetup.getPlayerAtMove();
        if(vantagemClara(boardSetup, opponent))
            return DROP;
        else
            return TAKE;
    }
}