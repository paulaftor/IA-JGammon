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

package jgam.debug;

import java.awt.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

/**
 * Open a new frame and inspect an arbitrary object.
 *
 * @author Mattias Ulbrich
 * @version 1.0
 */
public class ObjectInspector extends JFrame {
    private BorderLayout borderLayout1 = new BorderLayout();
    private JScrollPane jScrollPane = new JScrollPane();
    private JTree jTree = new JTree();
    private InspectorModel inspectorModel;

    private int a;
    Integer b = new Integer(17);

    public ObjectInspector(Object object) {
        super("Object Inspector");
        inspectorModel = new InspectorModel(object);
        try {
            jbInit();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void jbInit() throws Exception {
        getContentPane().setLayout(borderLayout1);
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.getContentPane().add(jScrollPane, java.awt.BorderLayout.CENTER);
        jScrollPane.getViewport().add(jTree);
        jTree.setModel(inspectorModel);
    }

    public static void main(String[] args) {
        Object r[] = new Number[5];
        r[0] = new Integer(4);
        Class c = r.getClass();
        Object o = new ObjectInspector(r);
        new ObjectInspector(new StringBuffer()).setVisible(true);
    }
}


class InspectorModel implements TreeModel {

    InspectorNode root;

    InspectorModel(Object object) {
        root = new InspectorNode(null, object, this, true);
    }

    /**
     * Returns the root of the tree.
     *
     * @return the root of the tree
     */
    public Object getRoot() {
        return root;
    }

    /**
     * Returns the child of <code>parent</code> at index <code>index</code>
     * in the parent's child array.
     *
     * @param parent a node in the tree, obtained from this data source
     * @param index int
     * @return the child of <code>parent</code> at index <code>index</code>
     */
    public Object getChild(Object parent, int index) {
        InspectorNode node = (InspectorNode) parent;
        return node.getChildAt(index);
    }

    /**
     * Returns the number of children of <code>parent</code>.
     *
     * @param parent a node in the tree, obtained from this data source
     * @return the number of children of the node <code>parent</code>
     */
    public int getChildCount(Object parent) {
        InspectorNode node = (InspectorNode) parent;
        return node.getChildCount();
    }

    /**
     * Returns <code>true</code> if <code>node</code> is a leaf.
     *
     * @param node a node in the tree, obtained from this data source
     * @return true if <code>node</code> is a leaf
     */
    public boolean isLeaf(Object parent) {
        InspectorNode node = (InspectorNode) parent;
        return node.isLeaf();
    }

    /**
     * Messaged when the user has altered the value for the item identified
     * by <code>path</code> to <code>newValue</code>.
     *
     * @param path path to the node that the user has altered
     * @param newValue the new value from the TreeCellEditor
     */
    public void valueForPathChanged(TreePath path, Object newValue) {
    }

    /**
     * Returns the index of child in parent.
     *
     * @param parent a note in the tree, obtained from this data source
     * @param child the node we are interested in
     * @return the index of the child in the parent, or -1 if either
     *   <code>child</code> or <code>parent</code> are <code>null</code>
     */
    public int getIndexOfChild(Object parent, Object child) {
        return ((InspectorNode) parent).getIndexFor(child);
    }

    /**
     * Adds a listener for the <code>TreeModelEvent</code> posted after the
     * tree changes.
     *
     * @param l the listener to add
     */
    public void addTreeModelListener(TreeModelListener l) {
    }

    /**
     * Removes a listener previously added with
     * <code>addTreeModelListener</code>.
     *
     * @param l the listener to remove
     */
    public void removeTreeModelListener(TreeModelListener l) {
    }

    /**
     * getSernum
     *
     * @param encapsulated Object
     * @return Integer
     */
    List sernumbers = new ArrayList();
    int getSernum(Object object) {
        EqualityWrapper w = new EqualityWrapper(object);
        int ser = sernumbers.indexOf(w);
        if (ser == -1) {
            ser = sernumbers.size() - 1;
            sernumbers.add(object);
        }
        return ser;
    }

    /**
     * isToStringClass
     *
     * @param classname String
     * @return boolean
     */

    private static final String TOSTRING_CLASSES[] = {
            "java.lang.String", "java.lang.Integer", "java.lang.Double", "java.lang.Float",
            "java.lang.Boolean", "java.lang.Character", "java.lang.Short", "java.lang.Long"
    };
    private static Set TO_STRING_LIST = new HashSet();
    static {
        for (int i = 0; i < TOSTRING_CLASSES.length; i++) {
            TO_STRING_LIST.add(TOSTRING_CLASSES[i]);
        }
    }


    boolean isToStringClass(String classname) {
        return TO_STRING_LIST.contains(classname);
    }

}


class EqualityWrapper {
    private Object object;
    EqualityWrapper(Object o) {
        object = o;
    }

    public boolean equals(Object o) {
        return o == object;
    }
}


class InspectorNode implements Comparable {

    Object encapsulated;
    String prefix;
    InspectorNode[] kids = null;
    boolean declaredHere;
    InspectorModel model;

    public InspectorNode(String f, Object o, InspectorModel model, boolean declaredHere) {
        this.declaredHere = declaredHere;
        this.model = model;
        prefix = f;
        encapsulated = o;
    }

    /**
     * Returns the child <code>TreeNode</code> at index
     * <code>childIndex</code>.
     *
     * @param childIndex int
     * @return TreeNode
     */
    public InspectorNode getChildAt(int childIndex) {
        if (kids == null) {
            makeKids();
        }

        return (InspectorNode) kids[childIndex];
    }

    public int getIndexFor(Object child) {
        if (kids == null) {
            makeKids();
        }

        for (int i = 0; i < kids.length; i++) {
            if (kids[i] == child) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns the number of children <code>TreeNode</code>s the receiver
     * contains.
     *
     * @return int
     */
    public int getChildCount() {
        if (kids == null) {
            makeKids();
        }

        return kids.length;
    }

    /**
     * Returns true if the receiver is a leaf.
     *
     * @return boolean
     */
    public boolean isLeaf() {
        if (prefix == null) {
            return false;
        }

        return getChildCount() == 0;
    }

    public int compareTo(Object o) {
        InspectorNode n = (InspectorNode) o;
        if (declaredHere != n.declaredHere) {
            return declaredHere ? -1 : 1;
        } else {
            return prefix.compareTo(n.prefix);
        }
    }

    private void makeKids() {
        if (encapsulated == null) {
            kids = new InspectorNode[0];
            return;
        }

        Class clss = encapsulated.getClass();
        if (clss.isArray()) {
            kids = new InspectorNode[Array.getLength(encapsulated)];
            for (int i = 0; i < kids.length; i++) {
                try {
                    kids[i] = new InspectorNode("[" + i + "]", Array.get(encapsulated, i), model, true);
                } catch (Exception ex) {
                    throw new Error(ex);
                }
            }
        } else {
            List fields = new ArrayList();
            addInheritedFields(clss, fields);

            kids = new InspectorNode[fields.size()];
            for (int i = 0; i < fields.size(); i++) {
                Field f = (Field) fields.get(i);
                f.setAccessible(true);
                try {
                    kids[i] = new InspectorNode(f.getName() + ": " + f.getType().getName(),
                              f.get(encapsulated), model,
                              f.getDeclaringClass() == clss);
                } catch (Exception ex) {
                    throw new Error(ex);
                }
            }
            Arrays.sort(kids);
        }
    }

    /**
     * getInheritedFields
     *
     * @param clss Class
     */
    private void addInheritedFields(Class clss, List list) {
        if (clss == null) {
            return;
        }

        list.addAll(Arrays.asList(clss.getDeclaredFields()));
        addInheritedFields(clss.getSuperclass(), list);
    }

    public String toString() {

        String ret = "";

        if (!declaredHere) {
            ret += "<html><font color=gray>";
        }

        if (prefix != null) {
            ret += prefix + " = ";
        }

        if (encapsulated == null) {
            ret += "null";
        } else {

            int sernum = model.getSernum(encapsulated);
            Class type = encapsulated.getClass();
            String classname = type.getName();

            if (model.isToStringClass(classname) || type.isPrimitive()) {
                ret += '"' + encapsulated.toString() + '"';
            } else if (type.isArray()) {
                ret += type.getComponentType() + "[" + Array.getLength(encapsulated) + "]";
            } else {
                ret += classname + "#" + sernum;
            }
        }

        if (!declaredHere) {
            ret += "</font></html>";
        }

        return ret;
    }

}
