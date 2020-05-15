package blockchain;

import java.io.Serializable;
import java.util.*;

public class Block implements Serializable {

    public String prevBlockHash;
    public String merkleRootHash;
    public long timestamp = new Date().getTime();
    public int nonce;
    public List<Transaction> transactions;

    public Block(Block bl){
        this.prevBlockHash = bl.prevBlockHash;
        this.merkleRootHash = bl.merkleRootHash;
        this.transactions = bl.transactions;
        this.timestamp = bl.timestamp;
        this.nonce = bl.nonce;
    }

    public Block(String prevBlockHash, String merkleRootHash, List<Transaction> transactions) {
        this.prevBlockHash = prevBlockHash;
        this.merkleRootHash = merkleRootHash;
        this.transactions = transactions;
        this.nonce = 0;
    }

    @Override
    public String toString() {
        return "blockchain.Block{" +
                "prevBlockHash='" + prevBlockHash + '\'' +
                ", merkleRootHash='" + merkleRootHash + '\'' +
                ", timestamp=" + timestamp +
                ", nonce=" + nonce +
                '}';
    }

    public String getHash(){
        return Hash.getSHA256(toString());
    }

    public boolean contains(String txid){
        HashSet<String> txSet = new HashSet<>();
        for(Transaction tx: transactions){
            if(tx.getHash().equalsIgnoreCase(txid))
                return true;
        }
        return false;
    }

    public Transaction get(String txid){
        HashSet<String> txSet = new HashSet<>();
        for(Transaction tx: transactions){
            if(tx.getHash().equalsIgnoreCase(txid))
                return tx;
        }
        return null;
    }
}
