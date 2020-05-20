package network;

public class NetworkInfo {

    public static final NodeInfo[] NODE_INFOS = {
            new NodeInfo("127.0.0.1", 5000),
            new NodeInfo("127.0.0.1", 5001),
            //new NodeInfo("127.0.0.1", 5002),
            //new NodeInfo("127.0.0.1", 5003),
            //new NodeInfo("127.0.0.1", 5004)
    };

    public static final int MAX_BLOCK_TRANSACTIONS = 5; // arbitrarily chosen
}
