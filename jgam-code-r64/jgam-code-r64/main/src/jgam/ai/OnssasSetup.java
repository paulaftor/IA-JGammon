package jgam.ai;

import jgam.game.ArrayBoardSetup;
import jgam.game.MoveChain;
import jgam.game.SingleMove;
import java.util.Random;

public class OnssasSetup extends ArrayBoardSetup{

    private AI ai;
    Random random = new Random();

    public OnssasSetup(AI ai) {
        super();
        this.ai = ai;
    }

    void assignFrom(ArrayBoardSetup bs) {
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

    public int evaluatePossibleMoves() throws CannotDecideException{
            
            int initialplayer = getPlayerAtMove();
    
            if (initialplayer == 0) {
                throw new IllegalStateException(
                        "cannot evaluate position w/o player at move");
            }
    
            while (getMaxPoint(1) > 0 && getMaxPoint(2) > 0) {
                
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
