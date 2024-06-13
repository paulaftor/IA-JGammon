package jgam.ai;

public class RolloutEagerRaceAI extends RolloutAI {

    public RolloutEagerRaceAI() {
        super(new EagerRaceAI());
        setRounds(500);
    }

}
