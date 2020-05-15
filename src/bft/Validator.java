package bft;

public class Validator {
    private boolean isProposer = false;
    public final int NUMBER_OF_VALIDATORS = 5;
    public int currentProposer = -1;

    public void newRound() {
        // choosing the new validator in a round robin fashion
        currentProposer++;
        currentProposer %= NUMBER_OF_VALIDATORS;
    }
}
