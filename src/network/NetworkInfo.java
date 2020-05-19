package network;

public class NetworkInfo {

    public static final NodeInfo[] NODE_INFOS = {
            new NodeInfo("156.194.27.142", 4444),
            new NodeInfo("41.232.70.18", 5555),
            new NodeInfo("127.0.0.1", 6666)
            //new NodeInfo("127.0.0.1", 5002),
    };

    public static final int MAX_BLOCK_TRANSACTIONS = 5; // arbitrarily chosen
}
