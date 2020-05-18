package nodes;

import blockchain.Transaction;
import network.Broadcaster;
import network.NodeInfo;
import testing.DatasetParser;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class Wallet {
    private static Broadcaster broadcaster = new Broadcaster(new NodeInfo("",4222));

    public static void main(String[] args) throws IOException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        broadcaster.connectWithPeers();
        HashMap<Integer, Transaction> txList = DatasetParser.getTransactions();
        ArrayList<Integer> keys =  new ArrayList<>(txList.keySet());
        Collections.sort(keys);
        System.out.println("Map Size: " + txList.size());
        for(Integer key: keys) {
            broadcaster.broadcast(txList.get(key));
        }
    }
}
