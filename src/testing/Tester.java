package testing;

import blockchain.Transaction;
import nodes.Miner;

import java.io.File;
import java.io.FileNotFoundException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.*;

public class Tester {
    public static String filePath = "/home/mashaal/Desktop/Simple Blockchain/doublespends_v2.txt";

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
        for(Integer key: keys)
            Miner.receivedNewTransaction(txList.get(key));
        System.out.println("Total Double Spent: " +  Miner.doubleSpending);
        System.out.println("Total Not Valid: " + Miner.notValid);
        System.out.println("Total Blockchain size: " + Miner.blockchain.size());
        System.out.println("Total lastBlock: " + Miner.blockchain.get(Miner.blockchain.size()-1));
    }
}
