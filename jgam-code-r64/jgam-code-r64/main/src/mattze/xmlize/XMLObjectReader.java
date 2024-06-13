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
public class XMLObjectReader extends FilterReader {

    private TagReader in;
    private List objectTable = new ArrayList();

    public XMLObjectReader(Reader r) throws IOException {
        super(new TagReader(r));
        this.in = (TagReader)super.in;
    }


    public Object readObject() throws IOException {
        Tag t = in.readTag();
        Class directClass;

        if (t.name.equals("objectref")) {
            return readObjectRef(t);
        } else

        if (t.name.equals("array")) {
            return readArray(t);
        } else

        if (t.name.equals("serializable") || t.name.equals("object")) {
            return readObject(t);
        } else

        if(t.name.equals("null"))
            return null;
        else

        if((directClass = getDirectClass(t.name)) != null) {
            t = in.readTag();  // read the end-tag.
            Object o =  mkObjectFromString(directClass, t.plaintextBefore.toString());
            objectTable.add(o);
            return o;
        } else

        throw new IOException("unexpected: "+t);
    }

    /**
     * makeDirectObject
     *
     * @param directClass Class
     * @param string String
     * @return Object
     */
    private Object mkObjectFromString(Class directClass, String string) throws
            IOException {
        try {
            Constructor cstr = directClass.getConstructor(new Class[] {String.class});
            return cstr.newInstance(new String[] { string });
        } catch (Exception ex) {
            throw (IOException)new IOException("instantiation doesn't work: "+ex).initCause(ex);
        }
    }

    /**
     * getDirectClass
     *
     * @param string String
     * @return Class
     */
    private Class getDirectClass(String string) {
        String[] table = XMLObjectWriter.DIRECT_CLASSES_TABLE;
        for (int i = 0; i < table.length; i+=2) {
            if(table[i+1].equals(string))
                try {
                    return Class.forName(table[i]);
                } catch (ClassNotFoundException ex) {
                    throw new Error(ex); // won't happen
                }
        }

        return null;
    }

    /**
     * readSerializable
     *
     * @param t Tag
     * @return Object
     */
    private Object readObject(Tag t) throws IOException {

        Class cls;
        try {
            cls = Class.forName(t.get("class"));
        } catch (ClassNotFoundException ex) {
            throw (IOException)new IOException("Class not found: "+t.get("class")).initCause(ex);
        }

        Object object = null;
        try {
            object = cls.newInstance();
        } catch (Exception ex) {
            throw (IOException)new IOException("cannot instantiate: "+t.get("class")).initCause(ex);
        }

        objectTable.add(object);

        if(t.name.equals("object"))
            return object;

        // serializable goes on.

        t = in.readTag();
        while(t.name.equals("field")) {
//            System.out.println(t);
            try {
                cls = Class.forName(t.get("class"));
                Field field = cls.getDeclaredField(t.get("name"));
                field.setAccessible(true);
                Object obj = readObject();
                field.set(object, obj);
            } catch (IOException ex) {
                throw ex;
            } catch(Exception ex) {
                throw (IOException)new IOException("Access error: "+ex).initCause(ex);
            }
            in.readTag(); // end-field
            t = in.readTag();
        }

        return object;
    }

    /**
     * readArray
     *
     * @param t Tag
     */
    private Object readArray(Tag t) throws IOException {

        Class c = null;
        try {
            c = getClassForName(t.get("class"));
        } catch (ClassNotFoundException ex) {
            throw (IOException)new IOException("Unknown class: "+t.get("class")).initCause(ex);
        }
        int len = Integer.parseInt(t.get("length"));

        Object array = Array.newInstance(c, len);
        objectTable.add(array);

        if(c.isPrimitive()) {
            t = in.readTag();
            Class direct = getDirectClass(c.getName());
            String[] items = t.plaintextBefore.toString().split(", ");
            for (int i = 0; i < len; i++) {
                Array.set(array, i, mkObjectFromString(direct, items[i]));
            }
        } else {
            for (int i = 0; i < len; i++) {
                Array.set(array, i, readObject());
            }
            in.readTag();
        }


        return array;
    }

    /**
     * getClassForName
     *
     * @param string String
     * @return Class
     */
    private Class getClassForName(String string) throws ClassNotFoundException {
        if(string.equals("double"))
            return Double.TYPE;

        return Class.forName(string);
    }

    /**
     * readObjectRef
     *
     * @param t Tag
     */
    private Object readObjectRef(Tag t) {
        return objectTable.get(Integer.parseInt(t.get("ref")));
    }
}
