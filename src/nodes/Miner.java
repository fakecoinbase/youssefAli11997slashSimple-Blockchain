package nodes;

import blockchain.*;
import network.Broadcaster;
import network.Listener;
import network.NetworkInfo;
import network.NodeInfo;
import security_utils.MerkleTree;
import testing.PrintThread;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.*;

public class Miner {
    public static Broadcaster broadcaster;
    public volatile static Map<String, Transaction> pendingTxPool;
    public volatile static Vector<Vector<Block>> blockchain;
    //public volatile static ArrayList<ArrayList<Block>> staleBlocks;
    public volatile static Set<String> uTxoPool;
    public static int nodeNumber;
    public static final int BLOCK_SIZE = 20;
    public static final int BLOCK_REWARD = 5;
    public static final int DIFF = 20;
    public static int WORKING_MODE = 0; //0 For POW | 1 For BFT
    public static Account account;
    public static Hashtable<Integer, Boolean> currentWorkingThreads;
    public static int doubleSpending = 0;
    public static int notValid = 0;
    public static int validd=0;
    public static int blockss=0;

    static {
        pendingTxPool = Collections.synchronizedMap(new LinkedHashMap<>());
        blockchain = new Vector<>();
        Vector<Block> firstList = new Vector<>();
        firstList.add(Block.getGenesisBlock());
        blockchain.add(firstList);
        uTxoPool = Collections.synchronizedSet(new HashSet<>());
        currentWorkingThreads = new Hashtable<>();
        //staleBlocks = new ArrayList<>();
        try {
            account = new Account();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);
        nodeNumber = sc.nextInt();
        broadcaster = new Broadcaster(NetworkInfo.NODE_INFOS[nodeNumber]);
        broadcaster.connectWithPeers();
        Thread testing = new PrintThread();
        testing.start();
        beginListening();
    }

    public static void beginListening() throws IOException {
        // server is listening
        ServerSocket ss = new ServerSocket(NetworkInfo.NODE_INFOS[nodeNumber].port);

        // running infinite loop for getting
        // client request
        while (true) {
            Socket socket = null;

            try {
                // socket object to receive incoming client requests
                socket = ss.accept();
                broadcaster.connectWithPeers();

                System.out.println("A new peer is connected : " + socket);

                // obtaining input and out streams
                DataInputStream dis = new DataInputStream(socket.getInputStream());
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

                System.out.println("Assigning new thread for this peer");

                Thread t = new Listener(socket, dis, dos);
                t.start();

            } catch (Exception e) {
                socket.close();
                e.printStackTrace();
            }
        }
    }

    public static synchronized void receivedNewTransaction(Transaction transaction) {
        // TODO: verify transaction before adding to pending transactions
        //System.out.println("TRAN REC: " + transaction);
        boolean valid = false;
        boolean firstSpending = true;
        valid = verifyTransaction(transaction);
        if (valid) {
            if (!transaction.isCoinBase())
                firstSpending = ensureNoDoubleSpending(transaction);
        }
        if (valid && firstSpending) {
            String txid = transaction.getHash();
            if (getTransaction(txid) == null) {
                pendingTxPool.put(txid, transaction);
                populateUTxOPool(transaction);
                if (pendingTxPool.size() > BLOCK_SIZE && !isMining()) {
                    startANewMiningThread();
                    blockss++;
                }
            }
            validd++;
        }
        if (!valid) {
            notValid++;
            //System.out.println("Invalid Transaction "+transaction.index);
        }
        if (!firstSpending){
            doubleSpending++;
        }
    }

    private static boolean isMining() {
        for (Boolean bool : currentWorkingThreads.values()) {
            if (bool)
                return true;
        }
        return false;
    }

    private static void populateUTxOPool(Transaction transaction) {
        // remove all spent UTXOs
        if (!transaction.isCoinBase()) {
            for (int i = 0; i < transaction.inputs.length; i++) {
                Transaction prevTx= getTransaction(transaction.inputs[i].previousTransactionHash);
                Output spent = prevTx.outputs[transaction.inputs[i].outputIndex];
                uTxoPool.remove(spent.getHash());

            }
        }
        //add all UTXOs
        for (int i = 0; i < transaction.outputs.length; i++) {
            Output unSpent = transaction.outputs[i];
            uTxoPool.add(unSpent.getHash());
        }
    }

    private static boolean ensureNoDoubleSpending(Transaction transaction) {
        for (int i = 0; i < transaction.inputs.length; i++) {
            Transaction prevTx= getTransaction(transaction.inputs[i].previousTransactionHash);
            Output output = prevTx.outputs[transaction.inputs[i].outputIndex];
            if (!uTxoPool.contains(output.getHash())) {
                return false;
            }
        }
        return true;
    }

    private static boolean verifyTransaction(Transaction transaction) {
        if (transaction.isCoinBase()) {
            if (!Account.validateSignature(Arrays.toString(transaction.outputs), transaction.publicKey, transaction.outputSignature, false)) {
                return false;
            }
            return true;
        } else {
            // validate Inputs
            for (int i = 0; i < transaction.inputs.length; i++) {
                Transaction prevTx = null;
                prevTx = getTransaction(transaction.inputs[i].previousTransactionHash);
                //  if(prevTx==null)System.out.println(transaction.index+"  1");
                if (prevTx == null)
                    return false;
                Output referenced = prevTx.outputs[transaction.inputs[i].outputIndex];
                if (!Account.validateAddress(transaction.publicKey, referenced.address)) {
                    return false;
                }
                if(!Account.validateSignature(transaction.inputs[i].toString(), transaction.publicKey, transaction.signatures[i], false)){
                    return false;
                }
            }

            //validate Outputs
            if (!Account.validateSignature(Arrays.toString(transaction.outputs), transaction.publicKey, transaction.outputSignature, false)) {
                return false;
            }

            //validate Value Sent
            double totalInput = 0;
            for (int i = 0; i < transaction.inputs.length; i++) {
                Transaction prevTx = getTransaction(transaction.inputs[i].previousTransactionHash);
                Output referenced = prevTx.outputs[transaction.inputs[i].outputIndex];
                totalInput += referenced.value;
            }
            double totalOutput = 0;
            for (int i = 0; i < transaction.outputs.length; i++) {
                Output sent = transaction.outputs[i];
                totalOutput += sent.value;
            }
            if (totalInput < totalOutput) {
                return false;
            }
            return true;
        }
    }

    public static synchronized Transaction getTransaction(String previousTransactionHash) {
        // search previous blocks
        for (int i = 0 ;i<blockchain.size();i++) {
            Vector<Block> l = blockchain.get(i);
            for(int j=0;j<l.size();j++) {
                Block bl = l.get(j);
                if (bl.contains(previousTransactionHash))
                    return bl.get(previousTransactionHash);
            }
        }

        // search pending
        if (pendingTxPool.containsKey(previousTransactionHash))
            return pendingTxPool.get(previousTransactionHash);

        return null;
    }

    public static synchronized void receivedNewBlock(Block block) {
        System.out.println("I RECEIVED A BLOCK!");
        int height = block.height;
        boolean valid = validateBlock(block,height);

        if (valid){
            if(isMining()) {
                invalidateAllMining();
            }
            updatePendingPool(block);
            for(int i=0;i<blockchain.size();i++){
                Vector<Block> l = blockchain.get(i);
                if(l.size()>=height
                &&l.get(height-1).getHash().equalsIgnoreCase(block.prevBlockHash)){
                    if(l.size()==height){
                        blockchain.get(i).add(block);
                        System.out.println("added received leaf Block "+block);
                    }else{
                        Vector<Block> ll = new Vector<>(l.subList(0,height));
                        ll.add(block);
                        blockchain.add(ll);
                        System.out.println("added received intermediate Block "+block);
                    }
                    break;
                }
            }
            if (pendingTxPool.size() > BLOCK_SIZE && !isMining()) {
                startANewMiningThread();
            }
        }
    }

    private static void updatePendingPool(Block block) {
        for (Transaction tx : block.transactions) {
            pendingTxPool.remove(tx.getHash());
        }
    }

    private static void invalidateAllMining() {
        for (Integer integer : currentWorkingThreads.keySet()) {
            currentWorkingThreads.put(integer, false);
        }
    }

    private static boolean validateBlock(Block block, int height) {
        boolean condition1 = MerkleTree.getMerkleTreeRoot(block.transactions).equalsIgnoreCase(block.merkleRootHash);
        boolean condition2 = ProofOfWork.validatePow(block, DIFF);
        boolean condition3 = false;
        for(int i=0;i<blockchain.size();i++){
            Vector<Block> l = blockchain.get(i);
            if(l.size()>=height
            && l.get(height-1).getHash().equalsIgnoreCase(block.prevBlockHash)){
                condition3=true;
                break;
            }
        }
        //Should Check If there are multiple coinbase but since the txdataset has multiple coinbase transactions we didn't
        return condition1 && condition2 && condition3;
    }

    /*private static boolean validateBlock(Block block) {
        boolean condition1 = MerkleTree.getMerkleTreeRoot(block.transactions).equalsIgnoreCase(block.merkleRootHash);
        boolean condition2 = ProofOfWork.validatePow(block, DIFF);
        boolean condition3 = block.prevBlockHash.equalsIgnoreCase(blockchain.get(blockchain.size() - 1).getHash());
        //Should Check If there are multiple coinbase but since the txdataset has multiple coinbase transactions we didn't
        return condition1 && condition2 && condition3;
    }*/

    public static void foundABlock(Block block, int hashCode) {
        if (currentWorkingThreads.get(hashCode)) {
            currentWorkingThreads.put(hashCode,false);
            int height = block.height;
            for(int i=0;i<blockchain.size();i++){
                Vector<Block> l = blockchain.get(i);
                if(l.size()>=height &&
                        l.get(height-1).getHash().equalsIgnoreCase(block.prevBlockHash)){
                    blockchain.get(i).add(block);
                    break;
                }
            }
            updatePendingPool(block);
            broadcaster.broadcast(block);
            System.out.println("BLOCK FOUND: " + block);
            if (pendingTxPool.size() > BLOCK_SIZE && !isMining()) {
                startANewMiningThread();
            }
        }
    }

    public static void startANewMiningThread() {
        Thread miner = new MinerThread();
        currentWorkingThreads.put(miner.hashCode(), true);
        miner.start();
    }

    public static Block getNewestBlock() {
        Block res = null;
        for(int i=0;i<blockchain.size();i++){
            Vector<Block> l = blockchain.get(i);
            int size = l.size() ;
            if(res==null || size>res.height+1){
                res=l.get(size-1);
            }
        }
        return res;
    }
}
