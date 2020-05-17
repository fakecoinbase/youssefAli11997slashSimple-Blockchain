package bft;

public class Utils {
    public static enum State {
        PRE_PREPARED,
        PREPARED,
        COMMITTED,
        FINAL_COMMITED
    }
}
