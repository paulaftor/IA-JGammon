package jgam.ai;

import jgam.game.ArrayBoardSetup;
import jgam.game.BoardSetup;
import jgam.game.MoveChain;
import jgam.game.PossibleMoves;

import java.util.Iterator;
import java.util.List;

/**
 * Class with static methods.
 *
 * To compute the chance to have blot(s) hit.
 */
public class Hits {

    // todo jsut to make the constructo visible
    static class Setup extends ArrayBoardSetup {
        Setup(BoardSetup bs) {
            super(bs);
            // make sure player is set ...
            activePlayer = getPlayerAtMove();
        }

        void switchPlayers() {
            activePlayer = 3 - activePlayer;
        }

        void setDice(int a, int b) {
            dice[0] = a;
            dice[1] = b;
        }
    }

    /**
     * Assuming the current player has finished their move. It is up to the other player
     * to hit as many blots as possible
     * @param bs
     * @return
     */
    public static double computeExpectedBarIncrease(BoardSetup bs) {

        if (bs.isSeparated()) {
            return 0.;
        }

        Setup s = new Setup(bs);
        s.switchPlayers();

        double expect = 0.;
        for (int d1 = 1; d1 <= 5; d1++) {
            for (int d2 = d1; d2 <= 6; d2++) {
                s.setDice(d1, d2);
                double factor = (d1 == d2) ? 1 / 36. : 1 / 18.;
                expect += factor * worstHit(s);
            }
        }

        return expect;
    }


    private static int worstHit(Setup s) {
        s.debugOut();
        int worstHit = 0;
        int barBefore = s.getBar(3 - s.getActivePlayer());
        PossibleMoves pm = new PossibleMoves(s);
        for (BoardSetup s2 : pm.getPossbibleNextSetups()) {
            s2.debugOut();
            int newbar = s2.getBar(3 - s.getActivePlayer());
            worstHit = Math.max(newbar - barBefore, worstHit);
        }
        return worstHit;
    }


}