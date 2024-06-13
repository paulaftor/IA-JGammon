package jgam.ai;

import java.util.Random;

import jgam.game.ArrayBoardSetup;
import jgam.game.BoardSetup;
import jgam.game.MoveChain;
import jgam.game.SingleMove;

class RolloutSetup extends ArrayBoardSetup {

    private AI ai;

    private static Random random = new Random();

    /**
     * 
     * @param ai AI to be used during rollout
     */
    public RolloutSetup(AI ai) {
        super();
        this.ai = ai;
    }

    void assignFrom(BoardSetup bs) {
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

    public int rollout() throws CannotDecideException {

        int initialplayer = getPlayerAtMove();

        // starting position without dice!
        while (initialplayer == 0) {
            throw new IllegalStateException(
                    "cannot rollout position w/o player at move");
        }

        while (getMaxPoint(1) > 0 && getMaxPoint(2) > 0) {
            rolldice();
            MoveChain moves = ai.makeMoves(this);
            for (int i = 0; i < moves.size(); i++) {
                performMove(moves.get(i));
            }
            activePlayer = 3 - activePlayer;
        }

        if (getMaxPoint(initialplayer) == 0)
            return 1;
        else
            return 0;
    }

    /**
     * make a move persistently. the values are not checked
     * 
     * @param m SingleMove
     */
    private void performMove(SingleMove m) {
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

    private void rolldice() {
        dice[0] = random.nextInt(6) + 1;
        dice[1] = random.nextInt(6) + 1;
    }

}
