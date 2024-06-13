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

import java.io.*;
import java.util.*;

import javax.xml.parsers.*;

import org.xml.sax.SAXException;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: </p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 */
public class Test {


    public static void main(String[] args) throws IllegalArgumentException,
            IllegalAccessException, IOException, ParserConfigurationException,
            SAXException, FactoryConfigurationError {


        XMLObjectWriter w = new XMLObjectWriter(new FileWriter("test.xml"));

        List list = new ArrayList();
        list.add(new Object());
        list.add(new Integer(33));

        w.writeObject(list);
        //w.writeObject(new JFrame("title"));
       // w.writeObject(new XMLtest());
        w.writeObject(list);
        w.writeObject("hello world");

        w.close();

        XMLObjectReader r = new XMLObjectReader(new FileReader("network.xml"));

        double d[] = new double[0];
        Class c = d.getClass().getComponentType();

        Object o = r.readObject();
    }
}

class XMLtest implements XMLable {
    public XMLMap getXMLData() {
        XMLMap ret = new XMLMap();
        ret.put("date", new Date());
        ret.put("title", "This is the \"title\"!, no gross <idea> ");
        return ret;

    }

    public void setXMLData(XMLMap xmlmap) {

    }
}
