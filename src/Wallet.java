import java.io.IOException;
import java.util.ArrayList;

public class Wallet {
    private static Broadcaster broadcaster = new Broadcaster(new NetworkInfo.NodeInfo("",0));

    public static void main(String[] args) throws IOException {
        broadcaster.connectWithPeers();
        broadcaster.connectWithPeers();
    }
}
