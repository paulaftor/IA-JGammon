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


package jgam.history;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import jgam.game.SingleMove;

/**
 * A move history message contains:
 * - the dice values
 * - a set of moves.
 *
 * It can be displayed as a string or send to a LoggingBoardSetup.
 */
class MoveMessage implements HistoryMessage {
    
    int player;
    int dice[];
    private List<SingleMove> moves = new ArrayList<SingleMove>();
    private String cachedShortString;
    private String cachedLongString;

    public MoveMessage(int player, int dice[]) {
        assert dice.length == 2;
        this.dice = (int[]) dice.clone();
        this.player = player;
    }

    /**
     * get the owner of this.
     *
     * @return 1 or 2
     */
    public int getPlayer() {
        return player;
    }

    public List<SingleMove> getMoves() {
        return moves;
    }

    /**
     * return the dice for these moves
     * @return the dice
     */
    public int[] getDice() {
        return dice;
    }

    /**
     * return a compact description of the move.
     *
     * This description can be shorter than the value returned by toLongString()
     * It is more human readable and more compact.
     *
     * @return a short string describing the moves made in here
     */
    public String toShortString() {
        if(cachedShortString == null) {
            String compactmoves = compactMoveList(moves);
            cachedShortString = dice[0] + dice[1] + ":" + compactmoves;
        }
        return cachedShortString;
    }

    /**
     * return:
     *   <dice1><dice2>: <move1.from>/<move2.to>{*} ... <moveN.from>/<moveN.to>{*}
     *
     * All move are single hops.
     *
     */
    public String toLongString() {
        
//        if(cachedLongString == null) {
//            String compactmoves = compactMoveList(moves);
//            cachedLongString = dice[0] + dice[1] + ":" + compactmoves;
//        }
//        return cachedShortString;
//   
        
        String ret = "" + dice[0] + dice[1] + ":";
        for(SingleMove singleMove : moves) {
            ret += " " + singleMove.toString();
        }
        return ret;
    }

    public void applyTo(jgam.history.HistoryMessage.HistoryMessageReceiver hmr) {
        hmr.receiveMoveMessage(this);
    }

    /**
     * add a move to the list for this round.
     * @param m Move to add
     */
    public void add(SingleMove m) {
        moves.add(m);
        cachedShortString = null;
    }

    /**
     * clear the moves in this message.
     *
     * This is done when an undo is issued.
     */
    public void clear() {
        moves.clear();
        cachedShortString = null;
    }

    private static class MoveDescription implements Comparable<MoveDescription> {
        int from;
        int to;
        int count;
        boolean valid = true;
        Set<Integer> hit = new TreeSet<Integer>(Collections.reverseOrder());
        
        public MoveDescription(SingleMove sm) {
            this.from = sm.from();
            this.to = sm.to();
            this.count = 1;
            if(sm.isHit()) {
                hit.add(this.to);
            }
        }
        
        @Override
        public int compareTo(MoveDescription o) {
            if(o.from == this.from) {
                return o.to - this.to;
            } else {
                return o.from - this.from;
            }
        }
    }

    /**
     * compact a List of Moves.
     *
     * SingleMoves can be merged to DoubleMoves or MultiMoves.
     * These are stored in {@link MoveDescription}s.
     *
     * First DoubleMoves are melted then it is checked whether there are
     * MultiMoves.
     *
     */
    public static String compactMoveList(List<SingleMove> moves) {
    
        List<MoveDescription> result = new LinkedList<MoveMessage.MoveDescription>();
        for (SingleMove sm : moves) {
            result.add(new MoveDescription(sm));
        }
        
        // make consecutive moves into one:
        // 5/4 4/3 --> 5/3
        int index = 0;
        for (MoveDescription md1 : result) {
            for (MoveDescription md2 : result.subList(index + 1, result.size())) {
                if(!md2.valid)
                    continue;
                if(md1.to == md2.from) {
                    md1.to = md2.to;
                    md1.hit.addAll(md2.hit);
                    md2.valid = false;
                }
            }
        }
    
        // collect same moves into one: 2/1 2/1 --> 2/1 (2)
        // moves are assumed to be sorted: same moves must be directly behind
        // each other
        MoveDescription last = null;
        for(MoveDescription md : result) {
            if(!md.valid)
                continue;
            if(last != null && last.from == md.from && last.to == md.to) {
                last.count ++;
                last.hit.addAll(md.hit);
                md.valid = false;
            } else {
                last = md;
            }
        }
        
        Collections.sort(result);
        StringBuilder sb = new StringBuilder();
        for (MoveDescription md : result) {
            if(!md.valid)
                continue;
            sb.append(" ");
            sb.append(md.from);
            int lasth = -1;
            for (Integer h : md.hit) {
                sb.append("/" + h + "*");
                lasth = h;
            }
            if(lasth != md.to) {
                sb.append("/").append(md.to);
            }
            if(md.count > 1) {
                sb.append(" (" + md.count + ")");
            }
        }
        
        return sb.toString();
        
    }
}

