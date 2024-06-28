/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jgam.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import jgam.game.BoardSetup;
import jgam.game.PossibleMoves;
import jgam.game.SingleMove;

public class Minimax implements AI{
    private HashMap memo = new HashMap();

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
        return "IA Expectiminimax recursivo";
    }

    /**
     * get a short description of this method.
     *
     * @return String
     */
    @Override
    public String getDescription() {
        return "IA implementada com expectiminimax recursivo";
    }

    public double heuristica(BoardSetup bs) {
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
                eval -= 50.0 * numCheckers;
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

        // MUDAR ISSO AQUI, PODE NÃO FAZER SENTIDO EM ALGUNS CONTEXTOS
        // PRINCIPALMENTE EM ÍNICIO DE PARTIDA
        if (checkersInFirstSix == 0 && bs.getBar(player) == 0)
            eval += 1000.0;
        else if (checkersInFirstSix == 1 && bs.getBar(player) == 0)
            eval -= 500.0;
        else if (checkersInFirstSix >= 2 && bs.getBar(player) == 0)
            eval -= 1000.0; 

        int pecasSaidas = bs.getOff(player);
        eval += 500.0 * pecasSaidas;
        //System.out.println("HEURISTICA CALCULADA: " + eval);
        return eval;
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

    /**
     * given a board make decide which moves to make.
     * There may not be any dice values left after call to this function.
     *
     * @param boardSetup BoardSetup to evaluate
     * @return SingleMove[] a complete set of moves.
     * @throws CannotDecideException if the AI cannot decide which moves to make
     */


     @Override
     public SingleMove[] makeMoves(BoardSetup boardSetup) throws CannotDecideException {
        double bestValue = Double.NEGATIVE_INFINITY;
        int bestIndex = -1;
        PossibleMoves pm = new PossibleMoves(boardSetup);
        List<BoardSetup> list = pm.getPossbibleNextSetups(); 
        int depth = 1;
    
        for (int index = 0; index < list.size(); index++) {
            BoardSetup setup = list.get(index);
            double value = heuristica(setup);
            value -= expectiminimax(setup, false, depth, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
            
            if (value > bestValue) {
                bestValue = value;
                bestIndex = index;
            }
        }
    
        if (bestIndex == -1)
            return new SingleMove[0];
        else
            return pm.getMoveChain(bestIndex);
     }
     
     @SuppressWarnings("unchecked")
    public double expectiminimax(BoardSetup boardSetup, boolean max, int depth,  double alfa, double beta) {
        double key = boardSetup.hashCode() + depth + (max ? 1 : 0);
        if(memo.containsKey(key))
            return (double) memo.get(key);
        
        if (depth == 0 || boardSetup.getOff(1) == 15 || boardSetup.getOff(2) == 15){
            memo.put(key, heuristica(boardSetup));
            return heuristica(boardSetup);
        }

        double value = 0.0;
        for (int dado1 = 1; dado1 <= 6; dado1++) {
            for (int dado2 = dado1; dado2 <= 6; dado2++) {
                boardSetup.setDice(dado1, dado2); 
                PossibleMoves pm = new PossibleMoves(boardSetup);
                List<BoardSetup> list = pm.getPossbibleNextSetups(); 
                double maxValue = max ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
                double probabilidade = dado1 == dado2 ? 1.0 / 36.0 : 1.0 / 18.0;
                for (BoardSetup setup : list) {
                    value = expectiminimax(setup, !max, depth - 1, alfa, beta);
                    maxValue = max ? Math.max(maxValue, value) : Math.min(maxValue, value);
                    if(max)
                        alfa = maxValue;
                    else
                        beta = maxValue;
                    
                    if(beta <= alfa) 
                        break;
                }
                
                value += maxValue * probabilidade;
            }
        }
        return value;
    } 

     
    public boolean vantagemClara(BoardSetup boardSetup, int player) {
        int opponent = 3 - player;
        int checkersInLastHalf = 0;
        int opponentCheckersInLastHalf = 0;
        System.out.println("Player: " + player);

        for (int i = 1; i < 25; i++) {
            int numCheckers = boardSetup.getPoint(player, i);
            int opponentNumCheckers = boardSetup.getPoint(opponent, i);

            if ((player == 1 && i > 12) || (player == 2 && i <= 12))
                checkersInLastHalf += numCheckers;

            if ((opponent == 1 && i > 12) || (opponent == 2 && i <= 12))
                opponentCheckersInLastHalf += opponentNumCheckers;
        }

        if (checkersInLastHalf == 15 && opponentCheckersInLastHalf < 12)
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
        // int player = boardSetup.getPlayerAtMove();
        // if(vantagemClara(boardSetup, player))
        //     return DOUBLE;
        // else
        //     return ROLL;
        return ROLL;
    }

    /**
     * given a board and a double offer, take or drop.
     * Evalutate the player whose turn it is NOT!
     *
     * @param boardSetup BoardSetup
     * @return either TAKE or DROP
     * @throws CannotDecideException if the AI cannot decide which moves to make
     */
    @Override
    public int takeOrDrop(BoardSetup boardSetup) throws CannotDecideException {
        // int opponent = 3-boardSetup.getPlayerAtMove();
        // if(vantagemClara(boardSetup, opponent))
        //     return DROP;
        // else
        //     return TAKE;
        return TAKE;
    }
}