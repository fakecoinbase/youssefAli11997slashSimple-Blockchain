package network;

public class NetworkInfo {

    public static final NodeInfo[] NODE_INFOS = {
            new NodeInfo("127.0.0.1", 5000),
            new NodeInfo("127.0.0.1", 5001),
            //new NodeInfo("127.0.0.1", 5002),
    };

    public static final int MAX_BLOCK_TRANSACTIONS = 5; // arbitrarily chosen
}
