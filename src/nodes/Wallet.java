package nodes;

import network.Broadcaster;
import network.NetworkInfo;

import java.io.IOException;

public class Wallet {
    private static Broadcaster broadcaster = new Broadcaster(new NetworkInfo.NodeInfo("",0));

    public static void main(String[] args) throws IOException {
        broadcaster.connectWithPeers();
    }
}
