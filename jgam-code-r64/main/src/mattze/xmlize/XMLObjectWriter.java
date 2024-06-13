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
import java.lang.reflect.*;
import java.util.*;

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
public class XMLObjectWriter extends FilterWriter {

    // the list of all objects encountered so far (for use with ref)
    private List objectTable = new ArrayList();

    // these classes are translated into their String equivalent
    static final String DIRECT_CLASSES_TABLE[] = {
                                                 "java.lang.String", "string",
                                                 "java.lang.Integer", "integer",
                                                 "java.lang.Float", "float",
                                                 "java.lang.Long", "long",
                                                 "java.lang.Character", "char",
                                                 "java.lang.Boolean", "boolean",
                                                 "java.lang.Double", "double",
                                                 "java.lang.Byte", "byte",
    };

    static final Hashtable DIRECT_CLASSES = new Hashtable();
    private static final List XML_HANDLERS = new ArrayList();

    static {
        for (int i = 0; i < DIRECT_CLASSES_TABLE.length; i += 2) {
            DIRECT_CLASSES.put(DIRECT_CLASSES_TABLE[i],
                               DIRECT_CLASSES_TABLE[i + 1]);
        }
        //addXMLDataHandler(new CollectionHandler());
        //addXMLDataHandler(new DateHandler());
    }

    public XMLObjectWriter(Writer w) {
        super(w);
    }

    public void writeObject(Object o) throws IOException {

        try {
            if (o == null) {
                out.write("<null />");
                return;
            }

            XMLDataHandler handler;
            int sernum = objectTable.indexOf(o);

            if (sernum != -1) {
                out.write("<objectref ref=\"" + sernum + "\" />");
            } else {
                sernum = objectTable.size();
                objectTable.add(o);

                String direct = (String) DIRECT_CLASSES.get(o.getClass().
                        getName());

                //
                // direct == primitive types
                if (direct != null) {
                    out.write("<" + direct + " id=\"" + sernum+"\">" +
                              encode(o.toString()) +
                              "</" + direct + ">");
                } else

                //
                // arrays
                if (o.getClass().isArray()) {
                    writeArray(o, sernum);
                } else

                //
                // XMLable
                if (o instanceof XMLable) {
                    writeXMLable((XMLable) o, sernum);
                } else

                //
                // registered handler for this class
                if ((handler = getXMLDataHandler(o)) != null) {
                    handler.writeObject(o, this, sernum);
                } else

                //
                // serializable
                if (o instanceof Serializable) {
                    writeSerializable((Serializable) o, sernum);
                }

                //
                // unknown object
                else {
                    out.write("<object class=\"" + o.getClass().getName() +
                              "\" id=\"" + sernum + "\"/>");
                }
            }
        } catch (IllegalAccessException ex) {
            throw (IOException)new IOException(
                    "Cannot write due to illegal access").initCause(ex);
        }
    }

    /**
     * writeArray
     *
     * @param o Object
     * @param sernum int
     */
    private void writeArray(Object o, int sernum) throws IOException {
        Class componentType = o.getClass().getComponentType();
        int length = Array.getLength(o);
        out.write("<array length=\"" + length +
                  "\" class=\"" +
                  componentType.getName() +
                  "\" id=\"" + sernum +
                  "\">");

        if (componentType.isPrimitive()) {
            for (int i = 0; i < length - 1; i++) {
                write(Array.get(o, i).toString() + ", ");
            }
            if (length != 0) {
                write(Array.get(o, length - 1).toString());
            }
        } else {
            for (int i = 0; i < length; i++) {
                writeObject(Array.get(o, i));
            }
        }

        out.write("</array>");
    }


    /**
     * writeXMLable
     *
     * @param o XMLable
     */
    private void writeXMLable(XMLable xmlable, int sernum) throws
            IOException, IllegalAccessException {
        XMLMap map = xmlable.getXMLData();
        write("<xmlable class=\"" + xmlable.getClass().getName() + "\" id=\"" +
              sernum + "\">");
        for (Iterator iter = map.iterateKeys(); iter.hasNext(); ) {
            String key = (String) iter.next();
            out.write("<" + key + ">");
            writeObject(map.get(key));
            out.write("</" + key + ">");
        }
        write("</xmlable>");
    }


    /**
     * writeSerializable
     *
     * @param o Serializable
     */
    private void writeSerializable(Serializable serializable, int sernum) throws
            IOException, IllegalArgumentException, IllegalAccessException {

        out.write("<serializable class=\"" +
                  serializable.getClass().getName() + "\" id=\"" +
                  sernum + "\">");
        Class cls = serializable.getClass();
        while (cls != null) {
            writeFieldsForClass(cls, serializable);
            cls = cls.getSuperclass();
        }
        out.write("</serializable>");

    }

    private void writeFieldsForClass(Class cls, Object object) throws
            IllegalAccessException, IllegalArgumentException, IOException {

        if (!doesImplementSerialzable(cls)) {
            return;
        }

        Field[] fields = cls.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            if (!Modifier.isStatic(fields[i].getModifiers())) {
                fields[i].setAccessible(true);
                out.write("<field name=\"" + fields[i].getName() +
                          "\" class=\"" + cls.getName() + "\">");
                writeObject(fields[i].get(object));
                out.write("</field>");
            }
        }

    }


    /**
     * return true iff clss does implement the Serializable interface.
     *
     * Not if a super/sub class does so!
     */
    private boolean doesImplementSerialzable(Class clss) {
        Class interfaces[] = clss.getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            if (interfaces[i] == Serializable.class) {
                return true;
            }
        }
        return false;
    }

    private String encode(String s) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
            case '<':
                buf.append("&lt;");
                break;
            case '>':
                buf.append("&gt;");
                break;
            case '"':
                buf.append("&quot;");
                break;
            case '&':
                buf.append("&amp;");
                break;
            default:
                buf.append(c);
                break;
            }
        }
        return buf.toString();
    }

    public static void addXMLDataHandler(XMLDataHandler handler) {
        XML_HANDLERS.add(handler);
    }

    public static XMLDataHandler getXMLDataHandler(Object object) {
        for (Iterator iter = XML_HANDLERS.iterator(); iter.hasNext(); ) {
            XMLDataHandler handler = (XMLDataHandler) iter.next();
            if (handler.doesHandle(object)) {
                return handler;
            }
        }
        return null;
    }

    // these are some convenience methods!

    public void writeDouble(double v) throws IOException {
        writeObject(new Double(v));
    }

    public void writeFloat(float v) throws IOException {
        writeObject(new Float(v));
    }

    public void writeByte(int v) throws IOException {
        writeObject(new Byte((byte) v));
    }

    public void writeChar(int v) throws IOException {
        writeObject(new Character((char) v));
    }

    public void writeInt(int v) throws IOException {
        writeObject(new Integer(v));
    }

    public void writeShort(int v) throws IOException {
        writeObject(new Short((short) v));
    }

    public void writeLong(long v) throws IOException {
        writeObject(new Long(v));
    }

    public void writeBoolean(boolean v) throws IOException {
        writeObject(new Boolean(v));
    }
}
