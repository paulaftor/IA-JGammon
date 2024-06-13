package jgam.ai;

public class InitialCombiAI extends CombiAI {

    public InitialCombiAI() {
        setFallbackAI(new RandomAI());
        setAIForCategory(Mode.RACE, new RolloutEagerRaceAI());
        setAIForCategory(Mode.NORMAL, new InitialAI());
        setAIForCategory(Mode.BEAROFF, new BearOffAI());
        setAIForCategory(Mode.UNSEPARATED_BEAROFF, new BearOffAI());
    }

    @Override
    public String getName() {
        return "SimpleRule";
    }

    @Override
    public String getDescription() {
        return "Deterministic machine based on simple rules";
    }
}
