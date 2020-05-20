package testing;

import bft.BFTBroadcaster;
import bft.Validator;
import blockchain.Transaction;
import network.Broadcaster;
import network.NodeInfo;
import nodes.Miner;

import java.io.File;
import java.io.FileNotFoundException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Scanner;

public class BFTTester {
    public static String filePath = "/home/youssefali/Documents/Simple-Blockchain/doublespends_v2.txt";
    public static BFTBroadcaster broadcaster = new BFTBroadcaster(new NodeInfo("", 0));

    public static void main(String [] args) throws NoSuchAlgorithmException, FileNotFoundException, InvalidAlgorithmParameterException, NoSuchProviderException {
        File dataSet = new File(filePath);
        Scanner sc = new Scanner(dataSet);
        int doubleSpentCount = 0;
        while (sc.hasNext()){
            sc.nextLine();
            doubleSpentCount++;
        }
        System.out.println(doubleSpentCount);
        HashMap<Integer, Transaction> txList = DatasetParser.getTransactions();
        ArrayList<Integer> keys =  new ArrayList<>(txList.keySet());
        Collections.sort(keys);
        System.out.println("Map Size: " + txList.size());
        for(Integer key: keys) {
            //System.out.println(key);
            //Validator.receivedNewTransaction(txList.get(key));
            broadcaster.broadcast(txList.get(key));
        }
        System.out.println("Total Double Spent: " +  Miner.doubleSpending);
        System.out.println("Total Not Valid: " + Miner.notValid);
        System.out.println("Total Blockchain size: " + Miner.blockchain.size());
        System.out.println("Total Valid Txs: " + Miner.validd);
        System.out.println("Total Blockss: " + Miner.blockss);
        System.out.println("Total lastBlock: " + Miner.blockchain.get(Miner.blockchain.size()-1));
    }
}
