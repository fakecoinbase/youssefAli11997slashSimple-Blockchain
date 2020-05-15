package nodes;

import network.Broadcaster;
import network.NodeInfo;

import java.io.IOException;

public class Wallet {
    private static Broadcaster broadcaster = new Broadcaster(new NodeInfo("",0));

    public static void main(String[] args) throws IOException {
        broadcaster.connectWithPeers();
    }
}
