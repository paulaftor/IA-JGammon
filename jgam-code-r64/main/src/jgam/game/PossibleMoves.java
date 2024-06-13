/*
 * JGammon: A backgammon client written in Java
 * Copyright (C) 2005/06 Mattias Ulbrich
 *
 * JGammon includes: - playing over network
 *                   - plugin mechanism for graphical board implementations
 *                   - artificial intelligence player
 *                   - plugin mechanism for AI players
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package jgam.game;

import java.io.*;
import java.util.*;

import jgam.*;
import jgam.util.*;

// source 1.5

/**
 * use an object of this class to keep book about the possible moves that
 * can still be made.
 * @author Mattias Ulbrich
 * @version 1.0
 */
public class PossibleMoves {

    /**
     * the board to calculate the moves from.
     * It is an immutable snapshot
     */
    private BoardSetup setup;
    
    /**
     * the set of possible movechains 
     */
    private Set<MoveChain> moveChains;
    
    /**
     * remaining hops during calculation
     */
    private IntList hops = new IntList();

    public PossibleMoves(BoardSetup setup) {
        this.setup = new BoardSnapshot(setup);
        int dices[] = setup.getDice();

        if (dices[0] == dices[1]) {
            hops.add(dices[0], 4);
        } else {
            hops.addAll(dices);
        }

        moveChains = allMoves(setup);
    }

    public PossibleMoves(BoardSetup setup, IntList hops) {
        this.hops = hops;
        this.setup = new BoardSnapshot(setup);

        // System.out.println("hops: "+hops);
        moveChains = allMoves(setup);
    }


    /**
     * in this situation, is there still a move possible
     * @return boolean
     */
    public boolean canMove() {
        return !moveChains.isEmpty();
    }

    /**
     * get all combinations of possible moves.
     *
     * The resulting lists consists of Lists of SingleMoves.
     * (List<List<SingleMove>>).
     * The order in the inner list is of importance.
     *
     * @return a list of lists of SingleMoves
     */
    public Collection<MoveChain> getPossibleMoveChains() {
        return this.moveChains;
    }

    /**
     * get a possible chain of SingleMoves.
     * @param index index of the chain in the list returned by getPossibleMoveChains
     * @return SingleMove[] the resulting moves in correct order.
     */
    public MoveChain getMoveChain(int index) {
        MoveChain chain = Util.getInCollection(moveChains, index); 
        return chain;
    }

    /**
     *
     * @param point int
     * @return List
     */
    public Set<MoveChain> getPossibleMoveChainsFrom(int point) {
        Set<MoveChain> ret = new HashSet<MoveChain>();

        for(MoveChain chain : moveChains) {
            ret.addAll(chain.findConsecutiveChainsFrom(point));
        }

        return ret;
    }

    /**
     * corresponding to the elements in getPossibleMoveChains()
     * return a list of setups.
     *
     * @return List of BoardSetups
     */
    public List<BoardSetup> getPossbibleNextSetups() {
        ArrayList<BoardSetup> ret = new ArrayList<BoardSetup>();
        for (MoveChain list : moveChains) {
            ret.add(setupAfterMoves(setup, list));
        }
        return ret;
    }

    private BoardSetup setupAfterMoves(BoardSetup setup, MoveChain chain) {
        BoardSetup ret = setup;
        for(SingleMove move : chain) {
            ret = new SetupAfterMove(ret, move);
        }
        return ret;
    }

    /**
     * may a move be done.
     * - Is the player valid
     * - Are the points valid.
     *
     * @param move Move to check
     */
    public boolean mayMove(SingleMove move) {
        for (MoveChain chain : moveChains) {
            if (chain.contains(move)) {
                return true;
            }
        }
        return false;
    }


    ///////////////////////////////////////////
    // Calclulation functions
    ///////////////////////////////////////////

    /*
     * get all moves from a board given a number of hops left
     */
    private Set<MoveChain> allMoves(BoardSetup setup) {
        IntList distVal = hops.distinctValues();
        int player = setup.getPlayerAtMove();
        Set<MoveChain> ret = new HashSet<MoveChain>();
        for (int i = 0; i < distVal.length(); i++) {
            int diceval = distVal.get(i);
            for (int from = diceval; from <= 25; from++) {
                if (movePossible(setup, from, from - diceval)) {
                    ret.addAll(allMovesStartingWith(setup,
                            makeMove(setup, from, from - diceval), diceval));
                }
            }
            int maxPoint = setup.getMaxPoint(player);
            if (diceval == distVal.max() && maxPoint < diceval && maxPoint > 0) {
                ret.addAll(allMovesStartingWith(setup,
                        makeMove(setup, maxPoint, 0), diceval));
            }
        }
        return ret;
    }

    /*
     * construct a move with the desired data. eventually tag it as hitting
     */
    private SingleMove makeMove(BoardSetup setup, int from, int to) {
        boolean hit = setup.getPoint(3 - setup.getPlayerAtMove(), 25 - to) == 1; 
        SingleMove sm = new SingleMove(setup.getPlayerAtMove(), from, to, hit);
        return sm;
    }

    /*
     * return true if there is a normal (no max-playout) move from <from> to <to>
     */
    private boolean movePossible(BoardSetup setup, int from, int to) {

        int player = setup.getPlayerAtMove();
        assert player == 1 || player == 2;

        // the length must be available in the hops
        if (!hops.contains(from - to)) {
            return false;
        }

        // there must be a checker on the the from point
        if (setup.getPoint(player, from) == 0) {
            return false;
        }

        // there may be at most one checker on the goal
        if (to > 0 && setup.getPoint(3 - player, 25 - to) > 1) {
            return false;
        }

        // if playing out: maxpoint must be <= 6
        if (to == 0 && setup.getMaxPoint(player) > 6) {
            return false;
        }

        // if there is one on the bar play it first
        if (setup.getPoint(player, 25) > 0 && from != 25) {
            return false;
        }

        return true;
    }


    private Collection<MoveChain> allMovesStartingWith(BoardSetup setup, SingleMove m, int diceval) {
        hops.remove(diceval);
        setup = new SetupAfterMove(setup, m);
        Set<MoveChain> ret = allMoves(setup);
        for (MoveChain chain : ret) {
            chain.add(m);
        }
        hops.add(diceval);

        // make one entry if empty
        if (ret.isEmpty()) {
            return Collections.singleton(new MoveChain(m));
        }

        return ret;
    }



    public static void main(String[] args) throws IOException, FormatException {
        BoardSetup s = new FileBoardSetup(new File("deleteme.boad"));
        s.debugOut();
        PossibleMoves pm = new PossibleMoves(s);
        System.out.println("Resulting possible moves");
        System.out.println(pm.moveChains);
        System.out.println(pm.getPossibleMoveChainsFrom(13));
    }
}


/**
 *
 * A setup after a move has been performed. it wraps a setup and a move.
 *
 */
class SetupAfterMove extends WrappedBoardSetup {
    SingleMove move;

    SetupAfterMove(BoardSetup setup, SingleMove move) {
        super(setup);
        this.move = move;
    }

    public int getPoint(int player, int pointnumber) {
        if (player == move.player()) {
            if (pointnumber == move.from()) {
                return board.getPoint(player, pointnumber) - 1;
            } else if (pointnumber == move.to()) {
                return board.getPoint(player, pointnumber) + 1;
            } else {
                return board.getPoint(player, pointnumber);
            }
        } else {
            if (pointnumber == 25 && move.isHit()) {
                // asking for the bar in case of a hit
                return board.getPoint(player, pointnumber) + 1;
            } else if (move.to() != 0 && pointnumber == 25 - move.to()) {
                return 0;
            } else {
                return board.getPoint(player, pointnumber);
            }
        }
    }

}
