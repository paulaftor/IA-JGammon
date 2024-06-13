package jgam.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * This class is a collection of static helper functions of varying nature.
 * 
 * The class cannot be instantiated.
 */
public class Util {
    
    private static Random r = new Random();

    private Util() {
        throw new Error("not to be called");
    }

    /**
     * Get an integer value which is within an interval.
     * 
     * The result is the value closest to <code>value</code> within the closed
     * interval <code>[min, max]</code>.
     * 
     * This method can be used to bound an integer value by an upper and lower
     * bound.
     * 
     * The argument <code>min</code> must not be larger than <code>max</code>.
     * 
     * @param value
     *            the value to bound
     * @param min
     *            the minimum value to take
     * @param max
     *            the maximum value to take
     * @return a value <code>r</code> such that {@code min <= r <= max} and
     *         {@code Math.abs(r-value)} is minimal
     */
    public static int bound(int value, int min, int max) {
        if(max < min) {
            throw new IllegalArgumentException("Second argument must not be larger than third");
        }
        return Math.min(Math.max(value, min), max);
    }
    
    /**
     * Get an double value which is within an interval.
     * 
     * The result is the value closest to <code>value</code> within the closed
     * interval <code>[min, max]</code>.
     * 
     * This method can be used to bound a double value by an upper and lower
     * bound.
     * 
     * The argument <code>min</code> must not be larger than <code>max</code>.
     * 
     * @param value
     *            the value to bound
     * @param min
     *            the minimum value to take
     * @param max
     *            the maximum value to take
     * @return a value <code>r</code> such that {@code min <= r <= max} and
     *         {@code Math.abs(r-value)} is minimal
     */
    public static double bound(double value, double min, double max) {
        if(max < min) {
            throw new IllegalArgumentException("Second argument must not be larger than third");
        }
        return Math.min(Math.max(value, min), max);
    }
    
    // TODO DOC
    public static <E> Iterable<E> reverse(final Iterable<E> iterable) {
        return new Iterable<E>() {
            public Iterator<E> iterator() {
                return new RevItr<E>(iterable.iterator());
            }
        };
    }
    
    private static class RevItr<E> implements Iterator<E> {

        private LinkedList<E> linkedList;

        public RevItr(Iterator<E> iterator) {
            linkedList = new LinkedList<E>();
            while(iterator.hasNext()) {
                linkedList.addFirst(iterator.next());
            }
        }

        @Override
        public boolean hasNext() {
            return !linkedList.isEmpty();
        }

        @Override
        public E next() {
            return linkedList.removeFirst();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("This is an immedate iterator, we cannot remove");
        }
        
    }

    // TODO DOC
    public static <E> E chooseRandom(List<E> list) {
        int index = r.nextInt(list.size());
        return list.get(index);
    }

    public static <E> E getInCollection(Collection<E> collection, int index) {
        for (E e : collection) {
            if(index == 0)
                return e;
            index --;
        }
        throw new IndexOutOfBoundsException("Index beyond the collection's size: " + index);
    }

    // TODO DOC
    public static String toString(Object o) {
        
        if(o == null) {
            return "(null)";
        }
        
        if (o instanceof Object[]) {
            Object[] arr = (Object[]) o;
            return Arrays.toString(arr);
        }
        
        if (o instanceof int[]) {
            int[] arr = (int[]) o;
            return Arrays.toString(arr);
        }
        
        return o.toString();
    }
    
}
