package jgam.ai;

public class RolloutInitialAI extends RolloutAI {
    
    public RolloutInitialAI() {
        super(new InitialAI());
        setRounds(50);
    }
    
}
