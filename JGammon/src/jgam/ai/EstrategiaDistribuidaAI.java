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

/**
 *
 * @author rafael
 */
public class EstrategiaDistribuidaAI implements AI{
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
        return "IA de estratégia distribuída";
    }

    /**
     * get a short description of this method.
     *
     * @return String
     */
    @Override
    public String getDescription() {
        return "IA implementada com expectminimax de profundidade 3 e estratégia distribuída";
    }

    double heuristica(BoardSetup boardSetup, int player)
    {
        double eval = 0.0;
        int oponente = 3 - player;        

        for (int i = 1; i <= 24; i++) {
            int p = boardSetup.getPoint(player, i);
            
            if (p == 1) {
                eval -= 50.0;
            }
            else if (p == 2 || p == 3) {
                eval += 50.0;
            }
            else {
                eval -= 20.0;
            }
        }
        
        if(boardSetup.getPoint(oponente, 0) == 1)
        {
            eval += 20.0;
        }
        
        if(boardSetup.getPoint(player, 0) == 1)
        {
            eval -= 50.0;
        }
        
        int pontos = boardSetup.getPoint(player, 25);
        eval += pontos * 50.0;
        
        pontos = boardSetup.getPoint(oponente, 25);
        eval -= pontos * 50.0;
                
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