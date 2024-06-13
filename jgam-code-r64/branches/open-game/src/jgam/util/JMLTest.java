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



package jgam.util;

/**
 * <p>Title: JGam - Java Backgammon</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author Mattias Ulbrich
 * @version 1.0
 */
public class JMLTest {

    /*@ ensures getSeconds() == 0;
      @ ensures getMinutes() == 0;
      @ ensures getHours() == 0;
      @*/
    public JMLTest() {
    }

    public final static int SECONDS_PER_DAY = 86400;

    //@ private invariant secondsSinceMidnight >= 0;
    //@ private invariant secondsSinceMidnight < 86400;

    private int secondsSinceMidnight = 0;

    //@ pure
    public int getSeconds() {
        return secondsSinceMidnight % 60;
    }

    //@ pure
    public int getMinutes() {
        return (secondsSinceMidnight / 60) % 60;
    }

    //@ pure
    public int getHours() {
        return secondsSinceMidnight / 3600;
    }

    /*@ requires seconds >= 0 && seconds < 60;
      @ ensures getSeconds() == seconds;
      @*/
    public void setSeconds(int seconds) {
        secondsSinceMidnight = secondsSinceMidnight
                               - (secondsSinceMidnight %60)
                               + seconds;
    }

    public static void main(String[] args) {
        JMLTest t = new JMLTest();
        t.setSeconds(333);
    }

}
