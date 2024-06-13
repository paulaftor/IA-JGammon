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
public class JassTest {
    public JassTest() {
        super();
    }

    public final static int SECONDS_PER_DAY = 86400;

    private int secondsSinceMidnight = 0;

    public int getSeconds() {
        return secondsSinceMidnight % 60;
    }

    public int getMinutes() {
        return (secondsSinceMidnight / 60) % 60;
    }

    public int getHours() {
        return secondsSinceMidnight / 3600;
    }

    public void setSeconds(int seconds) {
        /** require 0 <= seconds; seconds < 60; **/
        secondsSinceMidnight = secondsSinceMidnight
                               - (secondsSinceMidnight % 60)
                               + seconds;
        /** ensure getSeconds() == seconds;
                   getMinutes() == Old.getMinutes();
                   getHours() == Old.getHours();
         **/
        }

    /** invariant secondsSinceMidnight >= 0;
                  secondsSinceMidnight < 86400;
                  getSeconds() < 60 && getSeconds() >= 0;
                  getMintues() < 60 && getMinutes() >= 0;
                  getHours() < 24 && getHours() >= 0;
     **/
}
