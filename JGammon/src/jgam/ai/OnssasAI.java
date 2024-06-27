package jgam.ai;

import jgam.game.BoardSetup;
import jgam.game.PossibleMoves;
import jgam.game.SingleMove;

import java.util.Iterator;
import java.util.List;

public class OnssasAI implements AI {

    
    public void init() throws Exception {

    }
    
    public void dispose() {

    }

    public String getName() {
        return "Onssas AI";
    }

    public String getDescription() {
        return "Onssas' first AI attempt";
    }

    private double heuristica(BoardSetup bs) {
        double eval = 0.0;

        int player = bs.getPlayerAtMove();
        int opponent = 3 - player;

        int checkersInLastHalf = 0;
        int checkersInFirstSix = 0;

        for (int i = 1; i < 25; i++) {
            int numCheckers = bs.getPoint(player, i);
            int opponentNumCheckers = bs.getPoint(opponent, i);

            // Caso tenha uma peça do oponente na casa, soma 80 pontos
            // porque é uma peça que pode ser capturada
            // Caso não tenha peça do oponente, soma 40 pontos
            if (opponentNumCheckers == 1)
                eval += 80.0;
            else if (opponentNumCheckers == 0)
                eval += 40.0;
            else
                eval -= 150.0;

            // Caso tenha 4 ou mais peças na casa, subtrai 50 pontos por peça
            // Caso tenha 2 ou 3 peças, soma 200 pontos
            // Caso tenha 1 peça, subtrai 250 pontos
            if (numCheckers >= 4)
                eval -= 50.0 * numCheckers;
            else if (numCheckers >= 2)
                eval += 200.0;
            else if (numCheckers == 1)
                eval -= 250.0;

            if ((player == 1 && i >= 13) || (player == 2 && i <= 12))
                checkersInLastHalf += numCheckers;

            if ((player == 1 && i <= 6) || (player == 2 && i >= 19))
                checkersInFirstSix += numCheckers;

            if ((player == 1 && i <= 13) || (player == 2 && i >= 12))
                eval -= 20.0 * numCheckers;
            else
                eval += 20.0 * numCheckers;
        }

        // Caso tenha 15 peças na última metade do tabuleiro, soma 1000 pontos
        // Caso tenha 12 ou mais peças, soma 400 pontos
        // Caso tenha 9 ou mais peças, soma 250 pontos
        // Caso tenha 6 ou mais peças, soma 40 pontos
        // Caso tenha 3 ou mais peças, soma 15 pontos
        if (checkersInLastHalf == 15)
            eval += 1000.0;
        else if (checkersInLastHalf >= 12)
            eval += 400.0;
        else if (checkersInLastHalf >= 9)
            eval += 250.0;
        else if (checkersInLastHalf >= 6)
            eval += 40.0;
        else if (checkersInLastHalf >= 3)
            eval += 15.0;

        // Caso tenha 0 peças nas primeiras 6 casas, soma 1000 pontos
        // Caso tenha 1 peça, subtrai 500 pontos
        // Caso tenha 2 ou mais peças, subtrai 1000 pontos
        if (checkersInFirstSix == 0)
            eval += 1000.0;
        else if (checkersInFirstSix == 1)
            eval -= 500.0;
        else if (checkersInFirstSix >= 2)
            eval -= 1000.0;

        // Faz de tudo pra tirar as duas ultimas pecas
        // da primeira posicao do tabuleiro para nao atrasa-las
        if (player == 1)
            eval -= 1000.0 * bs.getPoint(1, 24);
        else
            eval -= 1000.0 * bs.getPoint(2, 1);

        int totalPoints = bs.getPoint(player, 25);

        // Recompensa alta por tirar peças do tabuleiro
        eval += 5000.0 * totalPoints;

        return eval;
    }

    public SingleMove[] makeMoves(BoardSetup bs) throws CannotDecideException {
        double eval = Double.NEGATIVE_INFINITY;
        int movimento = -1;

        PossibleMoves pm = new PossibleMoves(bs);
        List<PossibleMoves> moveList = pm.getPossbibleNextSetups();

        int i = 0;
        for (Iterator iter = moveList.iterator(); iter.hasNext(); i++) {
            BoardSetup boardSetup = (BoardSetup) iter.next();
            double thisEvaluation = heuristica(boardSetup);
            if (thisEvaluation > eval) {
                eval = thisEvaluation;
                movimento = i;
            }
        }

        if (movimento == -1)
            return new SingleMove[0];
        else
            return pm.getMoveChain(movimento);
    }

    public boolean vantagemClara(BoardSetup boardSetup, int player) {
        int opponent = 3 - player;
        int checkersInLastHalf = 0;
        int opponentCheckersInLastHalf = 0;

        for (int i = 1; i < 25; i++) {
            int numCheckers = boardSetup.getPoint(player, i);
            int opponentNumCheckers = boardSetup.getPoint(opponent, i);

            if ((player == 1 && i >= 13) || (player == 2 && i <= 12))
                checkersInLastHalf += numCheckers;

            if ((opponent == 1 && i >= 13) || (opponent == 2 && i <= 12))
                opponentCheckersInLastHalf += opponentNumCheckers;
        }

        if (checkersInLastHalf == 15 && opponentCheckersInLastHalf < 12)
            return true;
        else
            return false;
    }

    public int rollOrDouble(BoardSetup boardSetup) throws CannotDecideException {
        int player = boardSetup.getPlayerAtMove();
        if(vantagemClara(boardSetup, player)){
            System.out.println("Vantagem clara encontrada");
            return DOUBLE;
        }    
        else
            return ROLL;
    }

    public int takeOrDrop(BoardSetup boardSetup) throws CannotDecideException {
        int player = boardSetup.getPlayerAtMove();
        int opponent = 3 - player;
        if(!vantagemClara(boardSetup, opponent)){
            return TAKE;
        }    
        else
            return DROP;
    }
}
