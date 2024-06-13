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
class TagReader extends FilterReader {

    public TagReader(Reader in) throws IOException {
        super(in);
    }

    /**
     *
     * @return Tag
     * @throws IOException
     */
    public Tag readTag() throws IOException {
        Tag t = new Tag();

        // pre plaintext
        char c = readChar();

        while (c != '<') {
            t.plaintextBefore.append(c);
            c = readChar();
        }

        // the tag-name may begin with '/'
        c = readChar();
        if(c == '/') {
            t.closing = true;
            t.name = "/";
            c = readChar();
        }

        while (!Character.isSpaceChar(c) && c != '>') {
            t.name += c;
            c = readChar();
        }

        if (Character.isSpaceChar(c)) {
            c = readCharSkipSpaces();
        }

        // attributes
        while (c != '>' && c != '/') {

            StringBuffer attrName = new StringBuffer();
            StringBuffer attrValue = new StringBuffer();

            // attr name
            while (!Character.isSpaceChar(c) && c != '=') {
                attrName.append(c);
                c = readChar();
            }

            if (c != '=') {
                c = readCharSkipSpaces();
            }

            if (c != '=') {
                throw new IOException("= expected, found: " + c);
            }

            c = readCharSkipSpaces();
            if (c != '"') {
                throw new IOException("\" expected, found: " + c);
            }

            c = readChar();
            while (c != '"') {
                attrValue.append(c);
                c = readChar();
            }

            c = readCharSkipSpaces();
            t.attrs.put(attrName.toString(), attrValue.toString());
        }

        if(c == '/') {
            t.closing = true;
            if (readChar() != '>')
                throw new IOException("/> expected!");
        }

        return t;
    }

    private char readChar() throws IOException {
        int c = in.read();
        if (c == -1) {
            throw new EOFException();
        } else {
            return (char) c;
        }
    }

    private char readCharSkipSpaces() throws IOException {
        char c;
        do {
            c = readChar();
        } while (Character.isSpaceChar(c));
        return c;
    }

    public static void main(String[] args) throws IOException {
        TagReader r = new TagReader(new FileReader("network.xml"));
        for (; ; ) {
            System.out.println("tag: " + r.readTag());
        }
    }

}


class Tag {
    StringBuffer plaintextBefore = new StringBuffer();
    String name = "";
    Hashtable attrs = new Hashtable();
    boolean closing = false;

    public String toString() {
        return "<" + name + " " + attrs + "> [" + plaintextBefore + "]";
    }

    boolean isEndFor(Tag t) {
        return name.equals("/" + t.name);
    }

    String get(String key) {
        return (String)attrs.get(key);
    }
}
