/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jgam.ai;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jgam.game.BoardSetup;
import jgam.game.PossibleMoves;
import jgam.game.SingleMove;

public class EquipeMinimaxAI implements AI{
        /**
     * initialize this instance. Is called before it is used to
     * make decisions.
     *
     * @throws Exception if sth goes wrong during init.
     */
    @Override
    public void init() throws Exception {

    }

    /**
     * free all used resources.
     */
    @Override
    public void dispose() {

    }

    /**
     * get the name of this AI Method.
     *
     * @return String
     */
    @Override
    public String getName() {
        return "IA implementada pela equipe";
    }

    /**
     * get a short description of this method.
     *
     * @return String
     */
    @Override
    public String getDescription() {
        return "IA da equipe - expectiminimax";
    }

    private double heuristica(BoardSetup bs, int player) {
        double eval = 0.0;

        int opponent = 3 - player;

        int checkersInLastHalf = 0;
        int checkersInFirstSix = 0;

        for (int i = 1; i < 25; i++) {
            int playerCheckers = bs.getPoint(player, i);
            int opponentCheckers = bs.getPoint(opponent, i);

            // Caso tenha uma peça do oponente na casa, soma 80 pontos
            // porque é uma peça que pode ser capturada
            // Caso não tenha peça do oponente, soma 40 pontos
            //if (opponentCheckers == 1)
            //    eval += 80.0;
            //else if (opponentCheckers == 0)
            //    eval += 40.0;
            //else
            //    eval -= 150.0;

            // Caso tenha 4 ou mais peças na casa, subtrai 50 pontos por peça
            // Caso tenha 2 ou 3 peças, soma 200 pontos
            // Caso tenha 1 peça, subtrai 250 pontos
            if (playerCheckers >= 4)
                eval -= 50.0 * playerCheckers;
            else if (playerCheckers >= 2)
                eval += 200.0;
            else if (playerCheckers == 1) //&& !checkersCanBeAlone)
                eval -= 250.0;

            if ((player == 1 && i >= 13) || (player == 2 && i <= 12))
                checkersInLastHalf += playerCheckers;

            if ((player == 1 && i <= 6) || (player == 2 && i >= 19))
                checkersInFirstSix += playerCheckers;

            if ((player == 1 && i <= 13) || (player == 2 && i >= 12))
                eval -= 20.0 * playerCheckers;
            else
                eval += 20.0 * playerCheckers;
        }

        // Caso tenha 15 peças na última metade do tabuleiro, soma 1000 pontos
        // Caso tenha 12 ou mais peças, soma 400 pontos
        // Caso tenha 9 ou mais peças, soma 250 pontos
        // Caso tenha 6 ou mais peças, soma 40 pontos
        // Caso tenha 3 ou mais peças, soma 15 pontos
        // Ajuda a manter as peças na última metade do tabuleiro
        // e a mandar uma peça comida para a última metade de forma
        // mais rápida.
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

        // Caso tenha 0 peças nas primeiras 6 casas, soma 1000 pontos
        // Caso tenha 1 peça, subtrai 500 pontos
        // Caso tenha 2 ou mais peças, subtrai 1000 pontos
        // if (checkersInFirstSix == 0)
        //     eval += 1000.0;
        // else 
        if (checkersInFirstSix == 1)
            eval -= 500.0;
        else if (checkersInFirstSix >= 2)
            eval -= 1000.0;

        // Faz de tudo pra tirar as duas ultimas pecas
        // da primeira posicao do tabuleiro para nao atrasa-las
        if (player == 1)
            eval -= 1000.0 * bs.getPoint(1, 24);
        else
            eval -= 1000.0 * bs.getPoint(2, 1);

        int checkersAtBar = bs.getBar(player);
        int bearedOffCheckers = bs.getOff(player);
        int opponentCheckersAtBar = bs.getBar(opponent);

        // Para o late game, utilidade máxima por peças fora do tabuleiro,
        // afinal, é o objetivo do jogo
        eval += 1000 * bearedOffCheckers + 200.0 * opponentCheckersAtBar;

        return eval;
    }
 

    //Etapa de min
    double min(BoardSetup boardSetup, int player) {
        double bestValue = Double.NEGATIVE_INFINITY;
        for(int dado1 = 1; dado1 <= 6; ++dado1) {
            for(int dado2 = dado1; dado2 <= 6; ++dado2) {
                boardSetup.getDice()[0] = dado1;
                boardSetup.getDice()[1] = dado2;

                PossibleMoves pm = new PossibleMoves(boardSetup);
                List list = pm.getPossbibleNextSetups();
                int index = 0;

                for (Iterator iter = list.iterator(); iter.hasNext(); index++) {
                    BoardSetup setup = (BoardSetup) iter.next();
                    double value = heuristica(setup, player);
                    value = dado1 == dado2 ? value / 36 : value / 18;

                    if (value > bestValue)
                        bestValue = value;

                }
            }
        }
        return bestValue;
    }


    @Override
    public SingleMove[] makeMoves(BoardSetup boardSetup) throws CannotDecideException {
        int player = boardSetup.getPlayerAtMove();
        PossibleMoves pm = new PossibleMoves(boardSetup);
        int bestIndex = expectiminimax(boardSetup, player);

        if (bestIndex == -1)
            return new SingleMove[0];
        else
            return pm.getMoveChain(bestIndex);

    }

    public int expectiminimax(BoardSetup boardSetup, int player) {
        double bestValue = Double.NEGATIVE_INFINITY;
        int bestIndex = -1;
        int index = 0;
        PossibleMoves pm = new PossibleMoves(boardSetup);
        List list = pm.getPossbibleNextSetups();

        // Etapa de max 
        for (Iterator iter = list.iterator(); iter.hasNext(); index++) {
            BoardSetup setup = (BoardSetup) iter.next();
            double value = heuristica(setup, player);
            value -= min(setup, 3-player);

            if (value > bestValue) {
                bestValue = value;
                bestIndex = index;
            }
        }

        return bestIndex;
    }

    public boolean vantagemClara(BoardSetup boardSetup, int player) {
        int opponent = 3 - player;
        int checkersPertoOff = 0;
        int opponentCheckersPertoOff = 0;
        int playerCheckers = 0;
        int opponentCheckers = 0;
        int checkersLongeOff = 0;
        int opponentCheckersLongeOff = 0;

        for (int i = 0; i <= 25; i++) {
            playerCheckers = boardSetup.getPoint(player, i);
            opponentCheckers = boardSetup.getPoint(opponent, i);

            if ((player == 1 && (i <= 12 && i>0)) || (player == 2 && (i >= 13 && i<25)))
                checkersLongeOff += playerCheckers;
            
            if ((opponentCheckersLongeOff == 1 && (i <= 12 && i>0)) || (opponentCheckersLongeOff == 2 && (i >= 13 && i<25)))
                opponentCheckersLongeOff += playerCheckers;

            if ((player == 1 && i > 18 && i<25) || (player == 2 && i <= 6 && i>0))
                checkersPertoOff += playerCheckers;

            if ((opponent == 1 && i > 18 && i<25) || (opponent == 2 && i <= 6 && i>0))
                opponentCheckersPertoOff += opponentCheckers;
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
        //return ROLL;
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
        int opponent = 3-boardSetup.getPlayerAtMove();
        if(vantagemClara(boardSetup, opponent))
            return DROP;
        else
            return TAKE;
    }
}