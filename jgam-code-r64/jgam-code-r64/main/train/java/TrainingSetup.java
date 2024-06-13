import java.util.*;

import jgam.game.*;

/**
 * Training setups know about setting dice and checkers and performing single
 * moves
 * 
 * @author Mattias Ulbrich
 */
public class TrainingSetup extends ArrayBoardSetup {

    static final Random random = new Random(2000);

    TrainingSetup() {
        super(BoardSnapshot.INITIAL_SETUP);
    }

    /**
     * create an empty TrainingSetup.
     * 
     * If empty is set to true the data fields are not set to the INITIAL_SETUP
     * If empty is false it behaves like the standard constructor
     * 
     * @param empty true if an empty setup is to be created
     */
    TrainingSetup(boolean empty) {
        super();
        if(!empty)
            copyFrom(BoardSnapshot.INITIAL_SETUP);
    }

    TrainingSetup(BoardSetup setup) {
        super(setup);
    }

    /**
     * make a move persistently.
     * the values are not checked
     * @param m SingleMove
     */
    void performMove(SingleMove m) {
        if (m.player() == 1) {
            checkers1[m.from()]--;
            checkers1[m.to()]++;
            if (m.to() != 0 && checkers2[25 - m.to()] == 1) {
                checkers2[25 - m.to()]--;
                checkers2[25]++;
            }
        } else {
            checkers2[m.from()]--;
            checkers2[m.to()]++;
            if (m.to() != 0 && checkers1[25 - m.to()] == 1) {
                checkers1[25 - m.to()]--;
                checkers1[25]++;
            }
        }
    }

    void performMoves(MoveChain moveChain) {
        for (int i = 0; i < moveChain.size(); i++) {
            performMove(moveChain.get(i));
        }
    }
    
    void roll(int d1, int d2) {
        if (dice == null) {
            dice = new int[2];
        }

        dice[0] = d1;
        dice[1] = d2;
    }

    void roll() {
        roll(random.nextInt(6) + 1, random.nextInt(6) + 1);
    }

    /**
     * @return 1 for player 1, 2 for player 2 aso, 0 for not yet ended
     */
    int winstatus() {
        if (getMaxPoint(1) == 0) {
            return 1;
        } else if (getMaxPoint(2) == 0) {
            return 2;
        } else {
            return 0;
        }
    }

    /**
     * ignore BACKGAMMON.
     *
     * call only on boards with winstatus() != 0
     *
     * @return true if won GAMMON or BACKGAMMON
     */
    boolean isGammon() {
        int winner = winstatus();
        assert winner != 0;

        return getPoint(3-winner, 0) == 0;
    }

    void switchPlayers() {
        activePlayer = 3 - getPlayerAtMove();
    }

    boolean undecidedPlayer() {
        return dice[0] == dice[1] && activePlayer == 0;
    }

    void copyFrom(BoardSetup bs) {
        synchronized (bs) {
            for (int i = 0; i < 26; i++) {
                checkers1[i] = (byte) bs.getPoint(1, i);
                checkers2[i] = (byte) bs.getPoint(2, i);
            }
            doubleCube = bs.getDoubleCube();
            dice = bs.getDiceCopy();

            activePlayer = bs.getActivePlayer();
            doublePlayer = bs.mayDouble(1) ? 1 : 2;
        }
    }
    
    void setPoint(int player, int point, int val) {
        if(player == 1)
            checkers1[point] = (byte)val;
        else
            checkers2[point] = (byte)val;
    }

    /**
     * setPlayerAtMove
     *
     * @param player int
     */
    public void setPlayerAtMove(int player) {
        activePlayer = player;
    }

}
