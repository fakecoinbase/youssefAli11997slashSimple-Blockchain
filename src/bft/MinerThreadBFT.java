package bft;

import blockchain.*;
import nodes.Miner;
import org.web3j.crypto.Sign;
import security_utils.MerkleTree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class MinerThreadBFT extends Thread {

    @Override
    public void run() {
        ArrayList<Transaction> toBeIncludedInBlock = new ArrayList<>();
        int i = 0;
        for(Map.Entry<String, Transaction> entry : Miner.pendingTxPool.entrySet()){
            toBeIncludedInBlock.add(entry.getValue());
            i++;
            if(i == Miner.BLOCK_SIZE)
                break;
        }
        Transaction coinBase = calculateCoinBase(toBeIncludedInBlock);
        toBeIncludedInBlock.add(0, coinBase);
        String root = MerkleTree.getMerkleTreeRoot(toBeIncludedInBlock);
        Block toBeAdded = new Block(Miner.blockchain.get(Miner.blockchain.size()-1).getHash(), root, toBeIncludedInBlock);
        //toBeAdded = ProofOfWork.pow(toBeAdded, Miner.DIFF);
        Miner.foundABlock(toBeAdded, hashCode());
    }

    private Transaction calculateCoinBase(ArrayList<Transaction> toBeIncludedInBlock) {
        double totalFees = 0;
        for(Transaction tx: toBeIncludedInBlock) {
            if (!tx.isCoinBase()) {
                double totalInput = 0;
                for (int i = 0; i < tx.inputs.length; i++) {
                    Transaction prevTx = Miner.getTransaction(tx.inputs[i].previousTransactionHash);
                    Output referenced = prevTx.outputs[tx.inputs[i].outputIndex];
                    totalInput += referenced.value;
                }
                double totalOutput = 0;
                for (int i = 0; i < tx.outputs.length; i++) {
                    Output sent = tx.outputs[i];
                    totalOutput += sent.value;
                }
                totalFees += (totalInput - totalOutput);
            }
        }
        totalFees += Miner.BLOCK_REWARD;
        Output output = new Output(totalFees, 0, Miner.account.address);
        Output [] outputs = new Output[]{output};
        Transaction tx = new Transaction(false, 0, new Sign.SignatureData[]{}, new Input[]{},
                Miner.account.publicKey, 1, outputs, Miner.account.signMessage(Arrays.toString(outputs), false));
        return tx;
    }
}
