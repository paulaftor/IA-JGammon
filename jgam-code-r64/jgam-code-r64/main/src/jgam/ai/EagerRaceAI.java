package jgam.ai;

import jgam.game.BoardSetup;
import jgam.game.MoveChain;
import jgam.game.PossibleMoves;
import jgam.game.SingleMove;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class EagerRaceAI implements AI {
    @Override
    public void init() throws Exception {
    }

    @Override
    public void dispose() {
    }

    @Override
    public String getName() {
        return "Eager racing: Always move the furthest piece";
    }

    @Override
    public String getDescription() {
        return "Always move the checker that is furthest from home";
    }

    @Override
    public MoveChain makeMoves(BoardSetup boardSetup) throws CannotDecideException {

        PossibleMoves pm = new PossibleMoves(boardSetup);

        chains:
        for (MoveChain chain : pm.getPossibleMoveChains()) {
            List<Integer> srcs = new ArrayList<Integer>();
            for (SingleMove move : chain) {
                srcs.add(move.from());
            }
            // decreasing sort
            srcs.sort(Collections.reverseOrder());

            int point = boardSetup.getMaxPoint(boardSetup.getPlayerAtMove());
            int pieces = boardSetup.getPoint(boardSetup.getPlayerAtMove(), point);

            for (Integer src : srcs) {
                if(point != src) {
                    continue chains;
                }
                pieces --;
                while(pieces == 0) {
                    point --;
                    pieces = boardSetup.getPoint(boardSetup.getPlayerAtMove(), point);
                }
            }

            // found it!
            return chain;
        }

        throw new CannotDecideException();
    }

    @Override
    public int rollOrDouble(BoardSetup boardSetup) throws CannotDecideException {
        return ROLL;
    }

    @Override
    public int takeOrDrop(BoardSetup boardSetup) throws CannotDecideException {
        return TAKE;
    }
}
