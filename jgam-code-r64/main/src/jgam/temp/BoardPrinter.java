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



package jgam.temp;

import jgam.board.*;
import jgam.game.*;

/**
 * <p>Title: JGam - Java Backgammon</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
class BoardPrinter {
    public BoardPrinter() {
        super();
    }

    public static void print(BoardSetup bs) {

        System.out.println("BoardSetup "+bs);

        if(bs == null)
            return;

        for (int i = 13; i <= 24 ; i++) {
            if(bs.getPoint(1, i) > 0)
                System.out.print(" "+bs.getPoint(1, i)+"o");
            else if(bs.getPoint(2,25-i) > 0)
                System.out.print(" "+bs.getPoint(2, 25-i)+"x");
            else
                System.out.print(" __");

            if(i == 18)
                System.out.print(" | |");
        }

        System.out.println();

        for (int i = 12; i >= 1 ; i--) {
            if(bs.getPoint(1, i) > 0)
                System.out.print(" "+bs.getPoint(1, i)+"o");
            else if(bs.getPoint(2,25-i) > 0)
                System.out.print(" "+bs.getPoint(2, 25-i)+"x");
            else
                System.out.print(" __");

            if(i == 7)
                System.out.print(" | |");
        }

        System.out.println();

        System.out.println("Off:  o: "+bs.getOff(1)+ "   x:"+bs.getOff(2));
        System.out.println("Bar:  o: "+bs.getBar(1)+ "   x:"+bs.getBar(2));
        System.out.println("DoubleCube: "+bs.getDoubleCube()+ "  o may double: "+bs.mayDouble(1));
        System.out.println("Player at move: "+bs.getActivePlayer());
        if(bs.getDice() == null) {
            System.out.println("No Dice");
        } else {
            System.out.println("Dice:"+bs.getDice()[0]+", "+bs.getDice()[1]);
        }
    }
}
