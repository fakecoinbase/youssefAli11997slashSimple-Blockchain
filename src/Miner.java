import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.*;
import java.util.concurrent.CountDownLatch;

public class Miner {

    public static HashMap<String, Transaction> pendingTxPool;
    //private static HashMap<String, Transaction> transactionsHistory;
    public static List<Block> blockchain;
    public static HashSet<String> uTxoPool;
    public static final int BLOCK_SIZE = 200;
    public static final int BLOCK_REWARD = 5;
    public static final int DIFF = 3;
    public static int WORKING_MODE = 0; //0 For POW | 1 For BFT
    public static boolean miningBlock = false;
    public static Account account;
    public static HashMap<Integer, Boolean> currentWorkingThreads;

    static {
        pendingTxPool = new HashMap<>();
        //transactionsHistory = new HashMap<>();
        blockchain = new ArrayList<>();
        uTxoPool = new HashSet<>();
        currentWorkingThreads = new HashMap<>();
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
        beginListening();
        /*while(true){

        }*/
    }

    public static void beginListening() throws IOException {
        // server is listening
        ServerSocket ss = new ServerSocket(5000);

        // running infinite loop for getting
        // client request
        while (true) {
            Socket socket = null;

            try {
                // socket object to receive incoming client requests
                socket = ss.accept();
                Broadcaster.addNewSocket(socket);
                Broadcaster.addNewOutputStream(new DataOutputStream(socket.getOutputStream()));

                System.out.println("A new peer is connected : " + socket);

                // obtaining input and out streams
                DataInputStream dis = new DataInputStream(socket.getInputStream());
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

                System.out.println("Assigning new thread for this peer");

                Thread t = new Listener(socket, dis, dos);
                t.start();

            }
            catch (Exception e){
                socket.close();
                e.printStackTrace();
            }
        }
    }

    public static synchronized void receivedNewTransaction(Transaction transaction) {
        // TODO: verify transaction before adding to pending transactions
        boolean valid = false;
        boolean firstSpending = true;
        valid = verifyTransaction(transaction);
        if(valid){
            if(!transaction.isCoinBase())
                firstSpending = ensureNoDoubleSpending(transaction);
        }
        if(valid && firstSpending){
            String txid = transaction.getHash();
            if(getTransaction(txid) == null) {
                pendingTxPool.put(txid, transaction);
                populateUTxOPool(transaction);
                if(pendingTxPool.size() > BLOCK_SIZE && !isMining()){
                    startANewMiningThread();
                }
            }
        }
    }

    private static boolean isMining() {
        for(Boolean bool: currentWorkingThreads.values()){
            if(bool)
                return true;
        }
        return false;
    }

    private static void populateUTxOPool(Transaction transaction) {
        // remove all spent UTXOs
        if(!transaction.isCoinBase()) {
            for (int i = 0; i < transaction.inputs.length; i++) {
                Transaction prevTx = getTransaction(transaction.inputs[i].previousTransactionHash);
                Output spent = prevTx.outputs[transaction.inputs[i].outputIndex];
                uTxoPool.remove(spent.getHash());
            }
        }
        //add all UTXOs
        for(int i = 0 ; i < transaction.outputs.length ; i ++){
            Output unSpent = transaction.outputs[i];
            uTxoPool.add(unSpent.getHash());
        }
    }

    private static boolean ensureNoDoubleSpending(Transaction transaction) {
        for(int i = 0 ; i < transaction.inputs.length ; i ++){
            Transaction prevTx = getTransaction(transaction.inputs[i].previousTransactionHash);
            Output output = prevTx.outputs[transaction.inputs[i].outputIndex];
            if(!uTxoPool.contains(output.getHash())){
                return false;
            }
        }
        return true;
    }

    private static boolean verifyTransaction(Transaction transaction) {
        if(transaction.isCoinBase()){
            if(!Account.validateSignature(Arrays.toString(transaction.outputs), transaction.publicKey, transaction.outputSignature, false)){
                return false;
            }
            return true;
        }
        else{
            // validate Inputs
            for(int i = 0 ; i < transaction.inputs.length ; i ++){
                Transaction prevTx = getTransaction(transaction.inputs[i].previousTransactionHash);
                if(prevTx == null)
                    return false;
                Output referenced = prevTx.outputs[transaction.inputs[i].outputIndex];
                if(!Account.validateAddress(transaction.publicKey, referenced.address)){
                    return false;
                }
                if(!Account.validateSignature(referenced.toString(), transaction.publicKey, transaction.signatures[i], false)){
                    return false;
                }
            }

            //validate Outputs
            if(!Account.validateSignature(Arrays.toString(transaction.outputs), transaction.publicKey, transaction.outputSignature, false)){
                return false;
            }

            //validate Value Sent
            double totalInput = 0;
            for(int i = 0 ; i < transaction.inputs.length ; i ++){
                Transaction prevTx = getTransaction(transaction.inputs[i].previousTransactionHash);
                Output referenced = prevTx.outputs[transaction.inputs[i].outputIndex];
                totalInput += referenced.value;
            }
            double totalOutput = 0;
            for(int i = 0 ; i < transaction.outputs.length ; i ++){
                Output sent = transaction.outputs[i];
                totalOutput += sent.value;
            }
            if(totalInput < totalOutput){
                return false;
            }
            return true;
        }
    }

    public static Transaction getTransaction(String previousTransactionHash) {
        if(pendingTxPool.containsKey(previousTransactionHash))
            return pendingTxPool.get(previousTransactionHash);
        else{
            for(Block bl: blockchain){
                if(bl.contains(previousTransactionHash))
                    return bl.get(previousTransactionHash);
            }
            return null;
        }
    }

    public static synchronized void receivedNewBlock(Block block) {
        boolean valid = validateBlock(block);
        if(valid){
            invalidateAllMining();
            updatePendingPool(block);
            blockchain.add(block);
            if(pendingTxPool.size() > BLOCK_SIZE && !isMining()){
                startANewMiningThread();
            }
        }
        // TODO: MAYBE A RACE CONDITION
    }

    private static void updatePendingPool(Block block) {
        for(Transaction tx: block.transactions){
            pendingTxPool.remove(tx.getHash());
        }
    }

    private static void invalidateAllMining() {
        for(Integer integer: currentWorkingThreads.keySet()){
            currentWorkingThreads.put(integer, false);
        }
    }

    private static boolean validateBlock(Block block){
        boolean condition1 = MerkleTree.getMerkleTreeRoot(block.transactions).equalsIgnoreCase(block.merkleRootHash);
        boolean condition2 = block.prevBlockHash.equalsIgnoreCase(blockchain.get(blockchain.size()-1).getHash());
        return condition1 && condition2;
    }

    public static void foundABlock(Block block, int hashCode) {
        if(currentWorkingThreads.get(hashCode)){
            //TODO: BROADCAST CURRENT BLOCK FOUND
            updatePendingPool(block);
            blockchain.add(block);
            if(pendingTxPool.size() > BLOCK_SIZE && !isMining()){
                startANewMiningThread();
            }
        }
    }

    public static void startANewMiningThread(){
        Thread miner = new MinerThread();
        miner.start();
        currentWorkingThreads.put(miner.hashCode(), true);
    }
}
