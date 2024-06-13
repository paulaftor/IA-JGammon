package jgam;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import jgam.util.Util;
import junit.framework.TestCase;

public class TestUtil extends TestCase {

    public void testBound() {
        assertEquals(20, Util.bound(20, 10, 30));
        assertEquals(25, Util.bound(20, 25, 30));
        assertEquals(15, Util.bound(20, 10, 15));
        assertEquals(30, Util.bound(20, 30, 30));
        try {
            assertEquals(20, Util.bound(20, 30, 10));
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }
    
    public void testReverse() {
        Collection<Integer> c = Arrays.asList(4,1,2,3,4);
        Collection<Integer> c_rev = Arrays.asList(4,3,2,1,4);
        
        Collection<Integer> d = new ArrayList<Integer>();
        for (Integer integer : Util.reverse(c)) {
            d.add(integer);
        }
        assertEquals(c_rev, d);
        
        d.clear();
        for (Integer integer : Util.reverse(Util.reverse(c))) {
            d.add(integer);
        }
        assertEquals(c, d);
    }

}
