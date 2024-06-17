package jgam.ai;

import jgam.game.BoardSetup;
import jgam.game.MoveChain;
import jgam.util.ProgressMonitor;

public class TentativaIAavaliativa extends EvaluatingAI{

    public TentativaIAavaliativa() {
    }

    public String getDescription() {
        return "Tentativa de IA avaliativa";
    }

    public String getName() {
        return "IA Avaliativa";
    }

    public void init() {
    }
    
    public double probabilityToWin(BoardSetup setup) throws CannotDecideException {
        
        return 0;
    }

    public void dispose() {
    }

    
}