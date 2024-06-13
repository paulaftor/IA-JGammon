package jgam.game;

import java.io.File;
import java.io.IOException;

import jgam.FileBoardSetup;
import jgam.util.FormatException;
import junit.framework.TestCase;

public class TestPossibleMoves extends TestCase {
    
    BoardSetup board;
    int[] testdice;
    
    // This is a board where I have blobs on 23 and 24
    // and opponent has nothing on the board
    static BoardSetup INIT;
    {
        try {
            INIT = new FileBoardSetup(getClass().getResource("testPossibleMoves.txt"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }
    
    @Override
    protected void setUp() throws Exception {
        board = new ArrayBoardSetup(INIT) {{
           testdice = dice;  
        }};
    }

    public void testGetPossibleMoveChains() {
        fail("Not yet implemented");
    }

    public void testGetMoveChain() {
        fail("Not yet implemented");
    }

    public void testGetPossibleMovesFrom() {
        fail("Not yet implemented");
    }

    public void testGetPossbibleNextSetups() {
        fail("Not yet implemented");
    }

    public void testMayMove() {
        testdice[0] = 3;
        testdice[1] = 2;
        PossibleMoves pm = new PossibleMoves(board);
        
        assertTrue(pm.mayMove(new SingleMove(1, "25/23")));
        assertFalse(pm.mayMove(new SingleMove(1, "25/20")));
    }

}
