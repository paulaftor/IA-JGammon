/**
 * <p>
 * Title: JGam - Java Backgammon
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2005
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author Mattias Ulbrich
 * @version 1.0
 */

import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import jgam.ai.BearOffAI;
import jgam.game.BoardSetup;

public class MakeTableRealPipExpectedThrows {

    public static void main(String[] args) throws Exception {
        
        BearOffAI ai = new BearOffAI();
        ai.init();
        Map<Integer, Float> table = ai.getTable();
        
        for (Entry<Integer, Float> entry : table.entrySet()) {
            BoardSetup bs = makeSetup(entry.getKey());
            System.out.printf(Locale.UK, "%d %f\n", bs.calcPip(1), entry.getValue());
        }
        
    }
    
    private static BoardSetup makeSetup(int print) {
        TrainingSetup bs = new TrainingSetup(true);
        bs.setPlayerAtMove(1);
        for (int i = 0; i < 6; i++) {
            int v = print & 0xf;
            bs.setPoint(1, i+1, v);
            print >>= 4;
        }
        return bs;
    }


}
