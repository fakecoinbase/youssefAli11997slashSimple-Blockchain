package bft;

import bft.messages.Commit;
import bft.messages.PrePrepare;
import bft.messages.Prepare;
import blockchain.*;
import network.Broadcaster;
import network.Listener;
import network.NetworkInfo;
import network.NodeInfo;
import nodes.Miner;
import org.web3j.crypto.Sign;
import security_utils.MerkleTree;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.*;

public class Validator {
    private static boolean isProposer = false;
    private static final int NUMBER_OF_VALIDATORS = NetworkInfo.NODE_INFOS.length;
    private static int currentProposer = -1;
    private static Utils.State state = Utils.State.FINAL_COMMITED;
    private static NodeInfo myInfo;
    public static HashMap<String, Transaction> pendingTxPool;
    //private static HashMap<String, blockchain.Transaction> transactionsHistory;
    public static List<Block> blockchain;
    public static HashSet<String> uTxoPool;
    //public static final int BLOCK_SIZE = 200;
    public static final int BLOCK_REWARD = 5;
    public static int WORKING_MODE = 1; //0 For POW | 1 For BFT
    public static Account account;
    public static HashMap<Integer, Boolean> currentWorkingThreads;
    public static int doubleSpending = 0;
    public static int notValid = 0;

    public static BFTBroadcaster bftBroadcaster;
    public static Block currentWorkedOnBlock;

    public static List<Prepare> prepareMessagesPool;
    public static List<Commit> commitMessagesPool;

    static {
        pendingTxPool = new HashMap<>();
        //transactionsHistory = new HashMap<>();
        blockchain = new ArrayList<>();
        blockchain.add(Block.getGenesisBlock());
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

    public Validator(NodeInfo info) {
        myInfo = info;
    }

    public static void newRoundPhase() {

        prepareMessagesPool.clear();
        commitMessagesPool.clear();

        // choosing the new validator in a round robin fashion
        currentProposer++;
        currentProposer %= NUMBER_OF_VALIDATORS;

        // if current node is the proposer
        NodeInfo currentProposerInfo = NetworkInfo.NODE_INFOS[currentProposer];
        if(currentProposerInfo.ipAddress.equals(myInfo.ipAddress)
        && currentProposerInfo.port == myInfo.port) {
            // proposer collects transactions from pool
            // create a new block and broadcast it
            collectPendingTransactions();

            // change to pre-prepared state
            state = Utils.State.PRE_PREPARED;
        }
        // else wait for the pre-prepare message

    }

    public static void receivedPrePrepareMessage(PrePrepare prePrepare) {
        if(state != Utils.State.FINAL_COMMITED) return;

        // enter the pre-prepared state
        state = Utils.State.PRE_PREPARED;

        // verify the proposal (the sender and the block itself)
        if(validatePrePrepare(prePrepare)) {
            currentWorkedOnBlock = prePrepare.block;
            Sign.SignatureData signature = account.signMessage(currentWorkedOnBlock.getHash(), false);
            bftBroadcaster.broadcast(new Prepare(currentWorkedOnBlock.getHash(), account.publicKey, signature));
        }
    }

    public static void receivedPrepareMessage(Prepare prepare) {
        if(state != Utils.State.PRE_PREPARED) return;

        if(validatePrepare(prepare)) {
            prepareMessagesPool.add(prepare);
            // wait for 2*(#nodes) / 3 valid prepare messages
            if(prepareMessagesPool.size() >= 2 * NUMBER_OF_VALIDATORS / 3) {
                // then enter prepared state
                state = Utils.State.PREPARED;
                Sign.SignatureData signature = account.signMessage(currentWorkedOnBlock.getHash(), false);
                bftBroadcaster.broadcast(new Commit(currentWorkedOnBlock.getHash(), account.publicKey, signature));
            }
        }
    }

    public static void receivedCommitMessage(Commit commit) {
        if(state != Utils.State.PREPARED) return;

        if(validateCommit(commit)) {
            commitMessagesPool.add(commit);
            // wait for 2*(#nodes) / 3 valid commit messages
            if(commitMessagesPool.size() >= 2 * NUMBER_OF_VALIDATORS / 3) {
                // then enter committed state
                state = Utils.State.COMMITTED;
                blockchain.add(currentWorkedOnBlock);
                // TODO: validators append the received valid commit messages into the block
                state = Utils.State.FINAL_COMMITED;
                newRoundPhase();
            }
        }
    }

    public static void collectPendingTransactions() {
        ArrayList<Transaction> toBeIncludedInBlock = new ArrayList<>();
        for(Map.Entry<String, Transaction> entry : Miner.pendingTxPool.entrySet()){
            toBeIncludedInBlock.add(entry.getValue());
        }
        //Transaction coinBase = calculateCoinBase(toBeIncludedInBlock);
        //toBeIncludedInBlock.add(0, coinBase);
        String root = MerkleTree.getMerkleTreeRoot(toBeIncludedInBlock);
        Block toBeAdded = new Block(Miner.blockchain.get(Miner.blockchain.size()-1).getHash(), root, toBeIncludedInBlock);
        formedABlock(toBeAdded);
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
            }
        }
        if(!valid)
            notValid ++;
        if(!firstSpending)
            doubleSpending ++;
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
            updatePendingPool(block);
        }
    }

    private static void updatePendingPool(Block block) {
        for(Transaction tx: block.transactions){
            pendingTxPool.remove(tx.getHash());
        }
    }

    private static boolean validateBlock(Block block){
        boolean condition1 = MerkleTree.getMerkleTreeRoot(block.transactions).equalsIgnoreCase(block.merkleRootHash);
        boolean condition2 = block.prevBlockHash.equalsIgnoreCase(blockchain.get(blockchain.size()-1).getHash());
        return condition1 && condition2;
    }


    public static boolean validatePrePrepare(PrePrepare prePrepare) {
        boolean isValidBlock = validateBlock(prePrepare.block);
        boolean isValidSignature = Account.validateSignature(prePrepare.block.toString(), prePrepare.publicKey, prePrepare.signature, false);
        return isValidBlock && isValidSignature;
    }

    public static boolean validatePrepare(Prepare prepare) {
        boolean isValidBlockHash = prepare.blockHash.equalsIgnoreCase(currentWorkedOnBlock.getHash());
        boolean isValidSignature = Account.validateSignature(prepare.blockHash, prepare.publicKey, prepare.signature, false);
        return isValidBlockHash && isValidSignature;
    }

    public static boolean validateCommit(Commit commit) {
        boolean isValidBlockHash = commit.blockHash.equalsIgnoreCase(currentWorkedOnBlock.getHash());
        boolean isValidSignature = Account.validateSignature(commit.blockHash, commit.publicKey, commit.signature, false);
        return isValidBlockHash && isValidSignature;
    }

    public static void formedABlock(Block block) {
        Sign.SignatureData signature = account.signMessage(block.toString(), false);
        bftBroadcaster.broadcast(new PrePrepare(block, account.publicKey, signature));
        updatePendingPool(block);
        blockchain.add(block);
        System.out.println(block);
    }
}
