package testing;

import blockchain.Block;
import nodes.Miner;

import java.util.Vector;

public class PrintThread extends Thread {
    @Override
    public void run() {
        try {
            sleep(600000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Vector<Vector<Block>> result = Miner.blockchain;
        for(int i = 0 ; i < result.size() ; i ++){
            System.out.println("Chain #"+ i + "-----------------------------------");
            for(Block b: result.get(i)){
                System.out.println("BLOCK HASH: " + b.getHash() + " , " + b);
            }
            System.out.println("---------------------------------------------------");
        }
    }
}
