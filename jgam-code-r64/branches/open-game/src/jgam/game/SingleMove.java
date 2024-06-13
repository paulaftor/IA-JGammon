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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An object of class SingleMove describes a single move in the backgammon game.
 *
 * This is moving one checker by the value of one dice.
 *
 * @author Mattias Ulbrich
 */
public final class SingleMove implements Comparable<SingleMove> {

    private final int from;
    private final int to;
    /** The hit information is purely informative and does not need to hold! */
    private final boolean hit;
    private final int player;

    public SingleMove(int player, int from, int to, boolean hit) {
        this.player = player;
        this.hit = hit;
        if(from <= to) {
            // throw new IllegalArgumentException("Move must go from higher to lower point");
        }
        this.from = from;
        this.to = to;
    }

    public SingleMove(int player, String desc) {
        try {
            Matcher m = Pattern.compile("([0-9]+)/([0-9]+)(\\*?)").matcher(desc);
            m.matches();
            this.player = player;
            this.from = Integer.parseInt(m.group(1));
            this.to = Integer.parseInt(m.group(2));
            this.hit = "*".equals(m.group(3));
        } catch (Exception ex) {
            throw new IllegalArgumentException(desc + " does not describe a move", ex);
        }
    }

    public String toString() {
        return "" + from + "/" + to;
    }

    public int from() {
        return from;
    }

    public int to() {
        return to;
    }
    
    public boolean isHit() {
        return hit;
    }

    public boolean isHit(BoardSetup setup) {
        return isHit(setup, setup.getPlayerAtMove());
    }
    
    public boolean isHit(BoardSetup setup, int player) {
        return setup.getPoint(3 - player, 25 - to()) == 1;
    }

    public int length() {
        return from - to;
    }

    public int compareTo(SingleMove m) {
        if(m.from() == this.from()) {
            return m.to() - this.to();
        } else {
            return m.from() - this.from();
        }
    }
    
    @Override
    public int hashCode() {
        return from + 27*to;
    }

    public boolean equals(Object o) {
        if (o instanceof SingleMove) {
            SingleMove m = (SingleMove) o;
            return m.from == from && m.to == to;
        } 
        return false;
    }

    public int player() {
        return player;
    }
}
