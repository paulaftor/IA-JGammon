package jgam.ai;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jgam.game.BoardSetup;
import jgam.game.PossibleMoves;
import jgam.game.SingleMove;

public class ExpectiminimaxPodaAI implements AI {
    @Override
    public void init() throws Exception {
    }

    @Override
    public void dispose() {
    }

    @Override
    public String getName() {
        return "IA da equipe - poda";
    }

    @Override
    public String getDescription() {
        return "IA da equipe - expectiminimax com poda";
    }

    private double heuristica(BoardSetup bs, int player) {
        double eval = 0.0;
        int opponent = 3 - player;

        int checkersProximasOff = 0;
        int checkersDistantesOff = 0;
        int opponentCheckersProximasOff = 0;
        int opponentCheckersDistantesOff = 0;

        for (int i = 1; i < 25; i++) {
            int playerCheckers = bs.getPoint(player, i);
            int opponentCheckers = bs.getPoint(opponent, i);

            if (playerCheckers >= 4)
                eval -= 50.0 * playerCheckers;
            else if (playerCheckers >= 2)
                eval += 200.0;
            else if (playerCheckers == 1) 
                eval -= 250.0;

            if (i <= 12){
                checkersProximasOff += playerCheckers;
                opponentCheckersProximasOff += opponentCheckers;
            }

            if (i >= 19){
                checkersDistantesOff += playerCheckers;
                opponentCheckersDistantesOff += opponentCheckers;
            }

            if(i == 18){
                eval -= 500 * playerCheckers;
            }

            if (i >= 13)
                eval -= 50.0 * playerCheckers;
            else
                eval += 20.0 * playerCheckers;
        }

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

        if (checkersDistantesOff == 1)
            eval -= 500.0;
        else if (checkersDistantesOff >= 2)
            eval -= 1000.0;

        if (player == 1)
            eval -= 1000.0 * bs.getPoint(1, 24);
        else
            eval -= 1000.0 * bs.getPoint(2, 24);

        int bearedOffCheckers = bs.getOff(player);
        int opponentCheckersAtBar = bs.getBar(opponent);

        eval += 1000 * bearedOffCheckers + 200.0 * opponentCheckersAtBar;
        eval += vantagemClara(bs, player) ? 5000.0 : 0.0;
        eval -= vantagemClara(bs, opponent) ? 5000.0 : 0.0;

        return eval;
    }

    double acaso(BoardSetup bs, int depth, double alfa, double beta, int player, boolean max) {
        if (depth == 0 || bs.getOff(1) == 15 || bs.getOff(2) == 15)
            return heuristica(bs, player);

        double val = 0.0;
        for (int dado1 = 1; dado1 <= 6; ++dado1) {
            for (int dado2 = dado1; dado2 <= 6; ++dado2) {
                bs.setDice(dado1, dado2);
                PossibleMoves pm = new PossibleMoves(bs);
                List<BoardSetup> list = pm.getPossbibleNextSetups();
                double prob = dado1 == dado2 ? 1/36 : 1/18;

                for (BoardSetup setup : list) {
                    if(max)
                        val += max(setup, depth - 1, alfa, beta, player) * prob;
                    else
                        val -= min(setup, depth - 1, alfa, beta, player) * prob;
                }        
            }
        }
        return val;
    }

    double min(BoardSetup bs, int depth, double alfa, double beta, int player) {
        if (depth == 0 || bs.getOff(1) == 15 || bs.getOff(2) == 15)
            return heuristica(bs, player);

        double val = Double.POSITIVE_INFINITY;
        double vl = acaso(bs, depth - 1, alfa, beta, player, true);
        if (vl < val)
            val = vl;

        if (val <= alfa)
            return val;

        beta = Math.min(beta, val);
        return val;
    }

    double max(BoardSetup bs, int depth, double alfa, double beta, int player) {
        if (depth == 0 || bs.getOff(1) == 15 || bs.getOff(2) == 15)
            return heuristica(bs, player);

        double val = Double.NEGATIVE_INFINITY;
        double vl = acaso(bs, depth - 1, alfa, beta, player, false);
        if (vl > val)
            val = vl;

        if (val >= beta)
            return val;

        alfa = Math.max(alfa, val);
        return val;
    }

    @Override
    public SingleMove[] makeMoves(BoardSetup bs) throws CannotDecideException {
        double eval = Double.NEGATIVE_INFINITY;
        int move = -1;

        PossibleMoves pm = new PossibleMoves(bs);
        List<BoardSetup> moveList = pm.getPossbibleNextSetups();

        int i = 0;
        for (BoardSetup boardSetup : moveList) {
            double value = heuristica(boardSetup, bs.getPlayerAtMove());
            value -= acaso(boardSetup, 2, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, bs.getPlayerAtMove(), false);
            if (value > eval) {
                eval = value;
                move = i;
            }
            i++;
        }

        if (move == -1)
            return new SingleMove[0];
        else
            return pm.getMoveChain(move);
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

    @Override
    public int rollOrDouble(BoardSetup boardSetup) throws CannotDecideException {
        int player = boardSetup.getPlayerAtMove();
        if (vantagemClara(boardSetup, player))
            return DOUBLE;
        else
            return ROLL;
    }

    @Override
    public int takeOrDrop(BoardSetup boardSetup) throws CannotDecideException {
        int opponent = boardSetup.getPlayerAtMove();
        if (vantagemClara(boardSetup, opponent))
            return DROP;
        else
            return TAKE;
    }
}
