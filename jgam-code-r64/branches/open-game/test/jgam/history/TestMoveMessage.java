package jgam.history;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jgam.game.SingleMove;
import junit.framework.TestCase;

public class TestMoveMessage extends TestCase {

    public void testCompactMoveList() {
        check("3/2", "3/2");
        check("3/2*", "3/2*");
        check("6/5 (2)", "6/5", "6/5");
        check("6/2", "3/2", "6/3");
        check("6/3 5/2", "5/2", "6/3");
        check("6/5*/4", "5/4", "6/5*");
        check("6/3* (2) 6/0", "6/3", "6/3*", "6/3", "3/0");
    }
    
    // old test case
    // reveiled another bug
    public void testCompactMoveList2() throws Exception {
        List<SingleMove> list = new ArrayList<SingleMove>();
        list.add(new SingleMove(1, 24, 20, true));
        list.add(new SingleMove(1,"24/20"));
        list.add(new SingleMove(1,"20/16*"));
        list.add(new SingleMove(1, 20, 16, false));
        
        assertEquals(" 24/20*/16* (2)", MoveMessage.compactMoveList(list));
    }

    private void check(String expected, String ... moves) {
        ArrayList<SingleMove> l = new ArrayList<SingleMove>();
        for (String s : moves) {
            l.add(new SingleMove(1, s));
        }
        Collections.sort(l);
        String res = MoveMessage.compactMoveList(l);
        assertEquals(" " + expected, res);
    }

}
