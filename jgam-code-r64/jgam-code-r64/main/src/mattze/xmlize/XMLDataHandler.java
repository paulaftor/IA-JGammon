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

interface XMLDataHandler {
    public String getTag();
    public boolean doesHandle(Object object);
    public void writeObject(Object object, XMLObjectWriter writer, int sernum) throws IOException, IllegalAccessException;
    public Object readObject(Reader reader) throws IOException, IllegalAccessException;
}


class CollectionHandler implements XMLDataHandler {
    public String getTag() {
        return "collection";
    }

    public boolean doesHandle(Object object) {
        return (object instanceof List) || (object instanceof Set);
    }

    public void writeObject(Object object, XMLObjectWriter writer, int sernum) throws
            IOException, IllegalAccessException {

        Collection collection = (Collection) object;

        writer.write("<collection class=\""+object.getClass().getName()+
                     "\" id=\""+sernum+"\">");
        writer.writeObject(collection.toArray());
        writer.write("</collection>");
    }

    public Object readObject(Reader reader) {
        return null;
    }
}

class DateHandler implements XMLDataHandler {
    public String getTag() {
      return "date";
  }

  public boolean doesHandle(Object object) {
      return object instanceof Date;
  }

  public void writeObject(Object object, XMLObjectWriter writer, int sernum) throws
          IOException, IllegalAccessException {

      Date date = (Date) object;

      writer.write("<date id=\""+sernum+"\">");
      writer.writeLong(date.getTime());
      writer.write("</date>");
  }

  public Object readObject(Reader reader) {
      return null;
  }

}
