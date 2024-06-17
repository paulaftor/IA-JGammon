package jgam.ai;

import java.util.*;

import jgam.game.*;

public class OnssasAI implements AI{
    public OnssasAI() {
    }

    public String getDescription() {
        return "Onssas' first AI";
    }

    public String getName() {
        return "OnssasAI";
    }

    public void init() {
    }

    public void dispose() {
    }

    /**
    * @param boardSetup BoardSetup
    * 
    */

    public int takeOrDrop(BoardSetup boardSetup) {
        Random r = new Random();
        if (r.nextInt(2) == 0)
            return DROP;
        else
            return TAKE;
    }

    public MoveChain makeMoves(BoardSetup boardSetup) {

        double bestValue = Double.NEGATIVE_INFINITY;
        int bestIndex = -1;

        PossibleMoves pm = new PossibleMoves(boardSetup);
        List list = pm.getPossbibleNextSetups();
        int index = 0;
        for (Iterator iter = list.iterator(); iter.hasNext(); index++) {
            BoardSetup setup = (BoardSetup) iter.next();
            double value = eval(setup);
            if (value > bestValue) {
                bestValue = value;
                bestIndex = index;
            }
        }

        if (bestIndex == -1)
            return MoveChain.EMPTY;
        else {
            // Sy stem.out.println("Evaluation for this move: "+bestValue);
            return pm.getMoveChain(bestIndex);
        }
    }
    private double eval(BoardSetup setup) {
        int blots = 0;
        int holding = 0;
        int block = 0;
        int player = setup.getPlayerAtMove();

        // xx block --> 1 point
        // xxx block --> 3 points
        // xxxx --> 6
        // xxxxx --> 10
        // xxxxxx --> 15
        // xxxxxxxxx... --> 15

        int curBlock = 0;
        for (int i = 1; i <= 24; i++) {

            int p = setup.getPoint(player, i);
            if (p == 1) {
                blots++;
                curBlock = 0;
            } else if (p >= 2) {
                holding++;
                block += curBlock;
                curBlock ++;
            } else {
                curBlock = 0;
            }
        }

        int hisbar = setup.getBar(3 - player);

        double v = setup.calcPip(3 - player) / 100.;
        v += -blots * .5;
        v -= Hits.computeExpectedBarIncrease(setup) * 2;
        v += holding * .2;
        v += block * .2;
        v += hisbar * .5;


        return v;

    }

    public int rollOrDouble(BoardSetup boardSetup) throws CannotDecideException {
        // TODO Auto-generated method stub
        return ROLL;
    }
}
