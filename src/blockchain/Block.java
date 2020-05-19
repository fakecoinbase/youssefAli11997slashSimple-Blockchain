package blockchain;

import org.web3j.crypto.Sign;
import security_utils.Hash;
import security_utils.MerkleTree;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Block implements Serializable {

    public String prevBlockHash;
    public String merkleRootHash;
    public long timestamp = new Date().getTime();
    public int nonce;
    public int height;
    public List<Transaction> transactions;
    public HashMap<String, Transaction> transactionMap;

    public Block(Block bl){
        this.prevBlockHash = bl.prevBlockHash;
        this.merkleRootHash = bl.merkleRootHash;
        this.transactions = bl.transactions;
        this.transactionMap = new HashMap<>();
        this.height= bl.height;
        fillMap();
        this.timestamp = bl.timestamp;
        this.nonce = bl.nonce;
    }

    public Block(String prevBlockHash, String merkleRootHash, List<Transaction> transactions,int height) {
        this.height = height;
        this.prevBlockHash = prevBlockHash;
        this.merkleRootHash = merkleRootHash;
        this.transactions = transactions;
        this.transactionMap = new HashMap<>();
        fillMap();
        this.nonce = 0;
    }

    public Block(String prevBlockHash, String merkleRootHash, List<Transaction> transactions, long timestamp,int height) {
        this.prevBlockHash = prevBlockHash;
        this.height = height;
        this.merkleRootHash = merkleRootHash;
        this.transactions = transactions;
        this.transactionMap = new HashMap<>();
        fillMap();
        this.timestamp = timestamp;
        this.nonce = 0;
    }

    private void fillMap() {
        for(Transaction tx: transactions){
            transactionMap.put(tx.getHash(), tx);
        }
    }

    public static Block getGenesisBlock(){
        String prevHash = Hash.getSHA256("FIRST BLOCK EVER.");
        BigInteger publicKey = new BigInteger("70893185459726002695592608125398790426333873995722573030079532815328280498190");
        Output output = new Output(50, 0 , Account.getAddress(publicKey));
        Output [] outputs = new Output[]{output};
        byte [] v = new byte []{27};
        byte [] r = new byte []{73, 77, 101, 50, -72, 85, 33, -123, 118, 121, -78, 12, -64, 6, -9, -24, 99, 79, -40, -54, -1, 47, 74, -102, 98, 97, -92, 61, -6, 75, 64, 40};
        byte [] s = new byte []{64, -45, 3, 86, -28, -53, 28, -24, -80, -116, 117, -1, 47, 101, -127, 40, -19, -92, 4, -2, 14, 31, -15, 95, 93, -56, 19, 23, -6, -53, -78, -79};
        Sign.SignatureData signatureData = new Sign.SignatureData(v, r ,s);
        Transaction tx = new Transaction(false, 0, new Sign.SignatureData[]{}, new Input[]{},
                publicKey, 1, outputs, signatureData);
        ArrayList<Transaction> txList = new ArrayList<>();
        txList.add(tx);
        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        // you can change format of date
        Date date = null;
        try {
            date = formatter.parse("15/05/2020");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Block genesis = new Block(prevHash, MerkleTree.getMerkleTreeRoot(txList), txList, date.getTime(),0);
        return genesis;
    }


    @Override
    public String toString() {
        return "Block{" +
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
        if(transactionMap.containsKey(txid))
            return true;
        return false;
    }

    public Transaction get(String txid){
        return transactionMap.getOrDefault(txid, null);
    }
}
