
package jgam.ai;

import java.io.*;

import jgam.game.*;
import jgam.util.ExceptionDialog;

/**
 * <p style="font-weight:bold">This AI only knows to play Hypergammon, ie. with only 
 * three checkers for each player. It uses table look up and plays optimally.</p>
 * 
 * <h2>Size of table</h2>
 * <p>There are binomial(28, 3) possibilities to place 3 checkers on 
 * 26 points (bar+off included) resulting in 3276 possible position for one
 * player.</p>
 *  
 * <p>For both players this leads to a total number of less than 3276&sup2;=10732176 ~ 10<sup>7.03</sup>
 * possible positions.<br>
 * 
 * Some of these are not valid positions (e.g. white:all 3 on 
 * point 12, black:all 3 on point 13; which would be the same), 
 * but coding is significantly easier if these positions are 
 * not sorted out.</p>
 * 
 * <h2>Coding</h2>
 * <p>All positions (valid or not) from the paragraph above are coded 
 * as integers between 0 and 10732175 using multiple base coding.</p>
 * 
 * <p>The function <i>codeCheckers(board, player)</i> is used to code a half board, ie. the 
 * position of the 3 checkers of one player. The codings of the two
 * halfboards are combined via <i>codeCheckers(board, 1) + 3276*codeCheckers(board, 2)</i>.</p>
 * 
 * <p>To code a half board, the position (points) 
 * <tt>pos</tt> of the three checkers are sorted such that 
 * <tt>pos[0] < pos[1] < pos[2]</tt>. The values <tt>v</tt> are 
 * calculated as differences between them such that:
 * <pre>
 *   0 <= v[0]=pos[0] <= 25
 *   0 <= v[1]=pos[1]-pos[0] <= 25-pos[0]
 *   0 <= v[2]=pos[2]-pos[1] <= 25-pos[1]
 * </pre>
 * With these values the coding is done as
 * <pre>
 *   code(pos) = v[0] + 26*(v[1] + (26-pos[0])*v[2]))
 * </pre>
 * The decoding  is done via
 * <pre>
 *   decode(c) {
 *     v[0] = c % 26;
 *     v[1] = (c/26) % (26-v[0])
 *     v[2] = c / (26* (26-v[0]))
 *     pos = { v[0], v[0]+v[1], v[0]+v[1]+v[2] }
 *     return pos
 *   }
 * </pre>
 * 
 * <h2>Table creation</h2>
 * <p>The table is created if the createTable method is called. The main method does so.
 * The table is stored on disk in the file "hypergammontable.dat". For each position 
 * one byte is stored, modelling the winning probability between 0/255 and 255/255=1 in
 * a fixed point arithmetic.</p>
 * <p style="color:red;">The table file about 10.2 MB large. That's why it is
 * not shipped along.</p>
 *  
 * @author Mattias Ulbrich
 *
 */
public class HypergammonTableAI extends EvaluatingAI {

    public static final String FILENAME = "hypergammontable.dat";
    
    /** 
     * try to read the file 
     */
    public void init() throws Exception {
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILENAME));
        try {
            table = (byte[])ois.readObject();
        } catch(Exception ex) {
            if(ExceptionDialog.show(null, "The hypergammon database is not accessible. Create it?", ex, "Create table")) {
                HypergammonTableMaker htm = new HypergammonTableMaker();
                htm.makeTable();
                htm.saveTable();
                table = htm.getTable();
            }
        } finally {
            ois.close();
        }
    }

    /* (non-Javadoc)
     * @see jgam.ai.AI#dispose()
     */
    public void dispose() {
        table = null;
    }

    /* (non-Javadoc)
     * @see jgam.ai.AI#getName()
     */
    public String getName() {
        return "HypergammonOptimal";
    }

    /* (non-Javadoc)
     * @see jgam.ai.AI#getDescription()
     */
    public String getDescription() {
        return "Hypergammon AI that plays optimally";
    }

    public double probabilityToWin(BoardSetup setup) {
        return getTable(codeBoard(setup));
    }
    
    ///////////////////////
    // Table stuff
    ///////////////////////
    
    // initialized in init, forgotten by dispose.
    private byte[] table;
    
    private double getTable(int code) {
        byte tabvalue = table[code];
        assert tabvalue != 0;
        return (tabvalue)/255.;
    }
        
    ///////////////////////
    // Coding positions
    ///////////////////////
    
    public static int codePosition(int[] playerAtMove, int player2[]) {
        return codeCheckers(playerAtMove) + 3276*codeCheckers(player2);
    }
    
    public static int codeCheckers(int[] pos) {
        return pos[0] + 26*(pos[1]-pos[0] + (26-pos[0])*(pos[2]-pos[1])); 
    }
    
    public static int codeBoard(BoardSetup setup) {
        int pl = setup.getPlayerAtMove();
        return codeCheckers(setup, pl) + 3276 * codeCheckers(setup, 3-pl);
    }

    public static int codeCheckers(BoardSetup setup, int player) {
        int pos1, pos2, pos3;
        
        //
        // calc positions
        //
        for(pos1 = 0; setup.getPoint(player, pos1) == 0; pos1++);
        if(setup.getPoint(player, pos1) > 1) {
            pos2 = pos1;
        } else {
            for(pos2 = pos1+1; setup.getPoint(player, pos2) == 0; pos2++);
        }
        if(setup.getPoint(player, pos1) > 2 || (pos1 != pos2 && setup.getPoint(player, pos2) > 1)) {
            pos3 = pos2;
        } else {
            for(pos3 = pos2+1; setup.getPoint(player, pos3) == 0; pos3++);
        }
        
        //
        // calc v
        //
        int v1 = pos1;
        int v2 = pos2 - pos1;
        int v3 = pos3 - pos2;
        
        //
        // calc code
        //
        int code = v1 + 26*(v2 + (26-pos1)*v3);
        
        return code;
    }
    
}
