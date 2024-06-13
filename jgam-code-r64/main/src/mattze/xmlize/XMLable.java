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



package mattze.xmlize;


/**
 *
 * If a class allows writing/reading via XMLDataWriter/Reader implement
 * this interface.
 *
 * @author Mattias Ulbrich
 */

public interface XMLable {

    /**
     * return the data to be stored in a map.
     * @return a map containing key/value - pairs.
     */
    public XMLMap getXMLData();

    /**
     * restore the state of an object retrieving the information from
     * a XMLmap.
     * @param xmlMap XMLMap to retrieve the original state from.
     */
    public void setXMLData(XMLMap xmlMap);

}
