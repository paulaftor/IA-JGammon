package jgam.ai;

import java.io.*;

/**
 * Create the table for the HypergammonTableAI by complete search.
 * 
 * Start at the beginning position and eval it. Thus all possible, accessible
 * positions will be evaluated.
 * 
 * Let succ(p,d) be the set of the positions that can follow position p with
 * dice d. The wining probability on roll va(p) for a position p then is
 * 
 * <pre>
 * va(p) = sum( Prob(d) * max {vp(p') : p' in succ(p,d) ; d : dices)    
 * </pre>
 * 
 * vp is the probability with the opponent on roll.<br> And as p' has the other
 * player as active player vp(p') can be calculated as vp(p') =
 * 1-va(swapboard(p'))
 * 
 * 
 * All results are stored in a fixed point array with 65535 different values. 0
 * means not yet initialised.
 * 
 * @author Mattias Ulbrich
 * 
 */
public class HypergammonTableMaker {

    public static final String FILENAME = "hypergammontable.dat";

    private float[] betterTable;

    private byte[] byteTable;

    private float maxError = 0f;

    private int generation;

    private int count;

    private int[] currentWork;

    private final static int CODES = 3276;

    private final static int TABLESIZE = CODES * CODES;
    
    public HypergammonTableMaker() {
    }

    /**
     * @param args
     * @throws Exception may be thrown during calculation or saving
     */
    public static void main(String[] args) throws Exception {
        HypergammonTableMaker htm = new HypergammonTableMaker();
        if (args.length > 0)
            htm.loadBetterTable(args[1]);
        else
            htm.initTable();
        
        htm.makeTable();
        htm.saveTable();
    }

    private void loadBetterTable(String file) {
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
                    file));
            count = ois.readInt();
            currentWork = (int[])ois.readObject();
            betterTable = (float[]) ois.readObject();
            ois.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    

    public void makeTable() {

        do {
            System.err.print("\nGeneration " + generation + ": " + CODES
                    + " - ");
            maxError = 0f;
            goOverTable();
            backupSave();
            generation++;
        } while (maxError > 1f / 65535.f);

    }

    private void backupSave() {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(
                    new FileOutputStream("hypergammon.bak"));
            oos.writeInt(count);
            oos.writeObject(currentWork);
            oos.writeObject(betterTable);
            oos.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void saveTable() throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(
                FILENAME));
        oos.writeObject(byteTable);
    }

    public byte[] getTable() {
        return byteTable;
    }

    private boolean isLegalPosition(int player1[], int player2[]) {
        for (int i = 0; i < player1.length; i++) {
            for (int j = 0; j < player2.length; j++) {
                if (player1[i] != 25 && player2[j] != 25 
                        && player1[i] == 25 - player2[j])
                    return false;
            }
        }
        return true;
    }

    private boolean isWinning(int player[]) {
        for (int i = 0; i < player.length; i++) {
            if (player[i] != 0)
                return false;
        }
        return true;
    }

    private void initTable() {
        
        betterTable = new float[TABLESIZE];

        System.err.print("Init table: " + CODES + " - ");

        int count = 0;
        for (int[] player1 = { 0, 0, 0 }; player1[0] != -1; next(player1)) {
            outandback(count++);
            for (int[] player2 = { 0, 0, 0 }; player2[0] != -1; next(player2)) {
                if (!isLegalPosition(player1, player2)) {
                    betterTable[codePosition(player1, player2)] = Float.NaN;
                } else {
                    if (isWinning(player2))
                        betterTable[codePosition(player1, player2)] = 0f;
                    else
                        betterTable[codePosition(player1, player2)] = 1f;
                }
            }
        }
    }

    private void goOverTable() {

        if(currentWork == null)
            currentWork = new int[] {0,0,0};
        
        for (int []pl1 = currentWork; pl1[0] != -1; next(pl1), count++) {
            if(count % 300 == 299)
                backupSave();
            outandback(count);
            for (int[] pl2 = { 0, 0, 0 }; pl2[0] != -1; next(pl2)) {
                if (isLegalPosition(pl1, pl2)) {
                    float v = calcValue(pl1, pl2);
                    float err = Math.abs(v
                            - betterTable[codePosition(pl1, pl2)]);
                    if (err > maxError)
                        maxError = err;
                }
            }
        }
        
        count = 0;
        currentWork = null;
    }

    /*
     * goto next setup in enumeration
     */
    private void next(int[] player) {
        if (++player[0] > player[1]) {
            player[0] = 0;
            if (++player[1] > player[2]) {
                player[1] = 0;
                if (++player[2] > 25) {
                    player[0] = -1;
                }
            }
        }
    }

    /*
     * average over all possible dice throws;
     */

    private int[] diceBuf = new int[4];

    private float calcValue(int[] player1, int[] player2) {
        float value = 0f;

        diceBuf[3] = diceBuf[2] = 0;
        for (diceBuf[0] = 1; diceBuf[0] <= 5; diceBuf[0]++) {
            for (diceBuf[1] = 1; diceBuf[1] <= 5; diceBuf[1]++) {
                value += 2 * bestSucc(player1, player2, diceBuf);
            }
        }

        for (diceBuf[0] = 1; diceBuf[0] <= 6; diceBuf[0]++) {
            diceBuf[1] = diceBuf[2] = diceBuf[3] = diceBuf[0];
            value += bestSucc(player1, player2, diceBuf);
        }

        return value / 36f;
    }

    /*
     * get the best successor by doing the last valid dice on all checker. then
     * go down recursicely.
     * 
     * Unfortunately this uses plenty of heap.
     */
    private float bestSucc(int[] player1, int[] player2, int[] dice) {

        int[] buf1 = new int[3];
        int[] buf2 = new int[3];
        float bestSuc = -1000f;

        for (int d = 0; d < dice.length; d++) {
            if (dice[d] != 0) {
                for (int i = 0; i < 3; i++) {
                    System.arraycopy(player1, 0, buf1, 0, 3);
                    System.arraycopy(player2, 0, buf2, 0, 3);
                    if (makeMove(buf1, buf2, i, dice[d])) {
                        int old = dice[d];
                        dice[d] = 0;
                        float v = bestSucc(buf1, buf2, dice);
                        if (v > bestSuc)
                            bestSuc = v;
                        dice[d] = old;
                    }
                }
            }
        }

        // no moves possible:
        // 1 - otherone's-prob
        if (bestSuc == -1000f) {
            return 1f - betterTable[codePosition(player2, player1)];
        }

        return bestSuc;
    }

    public static int codePosition(int[] playerAtMove, int player2[]) {
        int code = codeCheckers(playerAtMove) + 3276 * codeCheckers(player2);
        return code;
    }

    public static int codeCheckers(int[] pos) {

        // Maple result for:
        // sum(sum(sum(1,c = b .. 25),b = a .. 25),a = 0 .. A-1)
        // + sum(sum(1,c = b .. 25),b = A .. B-1)
        // + sum(1,c = B .. C-1)

        // t0 =
        // 6.0*pos[2]+(153.0-3.0*pos[1])*pos[1]+(2027.0+(-78.0+pos[0])*pos[0])*pos[0];

        int t0 = (6 * pos[2] + (153 - 3 * pos[1]) * pos[1] + (2027 + (-78 + pos[0])
                * pos[0])
                * pos[0]) / 6;

        assert t0 < CODES;
        return t0;
    }

    /*
     * return true if a move could be made
     */
    private boolean makeMove(int[] player1, int[] player2, int checker,
            int length) {

        // already all played off.
        if (player1[2] == 0)
            return false;

        // bearoff situation?
        if (length > player1[2] && checker == 2) {
            player1[0] = 0;
        } else {
            int from = player1[checker];
            int to = from - length;
            if (to < 0)
                return false;
            if (to == 0)
                player1[checker] = to;
            else {
                switch (getPoint(player2, 25 - to)) {
                case 0:
                    player1[checker] = to;
                    break;

                case 1:
                    player1[checker] = to;
                    for (int i = 0; i < player2.length; i++) {
                        if (player2[i] == 25 - to) {
                            player2[i] = 25;
                            break;
                        }
                    }
                    break;

                default:
                    return false;
                }
            }
        }

        sort(player1);
        sort(player2);

        assert isLegalPosition(player1, player2);

        return true;
    }

    private void sort(int[] array) {
        for (int i = 0; i < array.length - 1; i++) {
            for (int j = i + 1; j < array.length; j++) {
                if (array[j] < array[i]) {
                    int p = array[i];
                    array[j] = array[i];
                    array[i] = p;
                }
            }
        }
    }

    private int getPoint(int player[], int pointnumber) {
        int ret = 0;
        for (int i = 0; i < player.length; i++) {
            if (player[i] == pointnumber)
                ret++;
        }
        return ret;
    }

    private static void outandback(int value) {
        String s = Integer.toString(value);
        System.err.print(s);
        for (int i = 0; i < s.length(); i++) {
            System.err.print("\b");
        }
    }

}