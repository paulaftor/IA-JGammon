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

public class EquipeAI implements AI{
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
        return "IA implementada com expectiminimax";
    }

    public double heuristica(BoardSetup bs, int player) {
        double eval = 0.0;

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

    /**
     * given a board make decide which moves to make.
     * There may not be any dice values left after call to this function.
     *
     * @param boardSetup BoardSetup to evaluate
     * @return SingleMove[] a complete set of moves.
     * @throws CannotDecideException if the AI cannot decide which moves to make
     */

    double heuristicaAlternativa(BoardSetup boardSetup) {
      double returnValue = 0.0;
      int player = boardSetup.getPlayerAtMove();
      int opponent = 3 - player;

      for (int i = 1; i <= 24; i++) {
        int checkersPoint = boardSetup.getPoint(player, i);

        if (checkersPoint >= 3) {
          returnValue -= 75.0;
        }

        else if (checkersPoint == 1){
          returnValue -= 25.0;
        }

        else
            returnValue += 25.0;
      }

      if (boardSetup.getPoint(opponent, 0) >= 1)
        returnValue += 100.0;

      if (boardSetup.getPoint(player, 0) >= 1) 
        returnValue -= 50.0;

      int point = boardSetup.getPoint(player, 25);
      returnValue += point * 50.0; 

      point = boardSetup.getPoint(opponent, 25);
      returnValue -= point * 50.0;

      return returnValue;
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

                    if (dado1 == dado2)
                        value /= 36;
                    else 
                        value /= 18;

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

    public boolean vantagemClara(BoardSetup boardSetup) {
        int player = boardSetup.getPlayerAtMove();
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

    /**
     * given a board make decide whether to roll or to double.
     *
     * @param boardSetup BoardSetup
     * @return either DOUBLE or ROLL
     * @throws CannotDecideException if the AI cannot decide which moves to make
     */
    @Override
    public int rollOrDouble(BoardSetup boardSetup) throws CannotDecideException {
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
        return TAKE;
    }
}