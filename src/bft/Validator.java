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
import sun.nio.ch.Net;
import testing.DatasetParser;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.*;
import java.util.concurrent.SynchronousQueue;

public class Validator {
    private static boolean isProposer = false;
    private static final int NUMBER_OF_VALIDATORS = NetworkInfo.NODE_INFOS.length;
    private static int currentProposer = -1; // change to -1
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
    public static int validd = 0;
    public static int blockss = 0;

    public static BFTBroadcaster bftBroadcaster;
    public static Block currentWorkedOnBlock;
    public static int nodeNumber;

    public static List<Prepare> prepareMessagesPool = new ArrayList<>();
    public static List<Commit> commitMessagesPool = new ArrayList<>();

    public static Stack<PrePrepare> prePrepareStack = new Stack<>();

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
        System.out.println("In new round phase");

        prepareMessagesPool.clear();
        commitMessagesPool.clear();

        // choosing the new validator in a round robin fashion
        currentProposer++;
        currentProposer %= NUMBER_OF_VALIDATORS;

        // if current node is the proposer
        NodeInfo currentProposerInfo = NetworkInfo.NODE_INFOS[currentProposer];
        if(currentProposerInfo.ipAddress.equals(myInfo.ipAddress)
        && currentProposerInfo.port == myInfo.port) {
            System.out.println("I am the proposer");
            // proposer collects transactions from pool
            // create a new block and broadcast it
            collectPendingTransactions();

            // change to pre-prepared state
            state = Utils.State.PRE_PREPARED;
        }
        // else wait for the pre-prepare message
        else {
            System.out.println("I am a validator");
            if(!prePrepareStack.isEmpty()) {
                // enter the pre-prepared state
                state = Utils.State.PRE_PREPARED;

                PrePrepare prePrepare = prePrepareStack.pop();

                System.out.println("validating pre-prepare...");
                // verify the proposal (the sender and the block itself)
                if(validatePrePrepare(prePrepare)) {
                    System.out.println("valid pre-prepare");
                    currentWorkedOnBlock = prePrepare.block;
                    Sign.SignatureData signature = account.signMessage(currentWorkedOnBlock.getHash(), false);
                    bftBroadcaster.broadcast(new Prepare(currentWorkedOnBlock.getHash(), account.publicKey, signature));
                }
            }
        }

    }

    public static void receivedPrePrepareMessage(PrePrepare prePrepare) {
        prePrepareStack.add(prePrepare);

        if(state != Utils.State.FINAL_COMMITED) return;

        // enter the pre-prepared state
        state = Utils.State.PRE_PREPARED;

        System.out.println("validating pre-prepare...");
        // verify the proposal (the sender and the block itself)
        if(validatePrePrepare(prePrepare)) {
            System.out.println("valid pre-prepare");
            currentWorkedOnBlock = prePrepare.block;
            Sign.SignatureData signature = account.signMessage(currentWorkedOnBlock.getHash(), false);
            bftBroadcaster.broadcast(new Prepare(currentWorkedOnBlock.getHash(), account.publicKey, signature));
        }
    }

    public static void receivedPrepareMessage(Prepare prepare) {
        prepareMessagesPool.add(prepare);

        if(state != Utils.State.PRE_PREPARED) return;

        if(haveGotEnoughPrepareMessages()) {
            System.out.println("Received enough prepare messages");
            state = Utils.State.PREPARED;
            Sign.SignatureData signature = account.signMessage(currentWorkedOnBlock.getHash(), false);
            bftBroadcaster.broadcast(new Commit(currentWorkedOnBlock.getHash(), account.publicKey, signature));
        }

        /*if(validatePrepare(prepare)) {
            // wait for 2*(#nodes) / 3 valid prepare messages
            //if(prepareMessagesPool.size() >= 2 * NUMBER_OF_VALIDATORS / 3) {
            if(prepareMessagesPool.size() >= 2) {
            //if(prepareMessagesPool.size() >= NUMBER_OF_VALIDATORS-1) {
                // then enter prepared state

            }
        }*/
    }

    public static boolean haveGotEnoughPrepareMessages() {
        int validCount = 0;
        for(Prepare prepare : prepareMessagesPool) {
            if(validatePrepare(prepare))
                validCount++;
        }

        return validCount >= 2; // 2 * NUMBER_OF_VALIDATORS / 3;
    }

    public static void receivedCommitMessage(Commit commit) {
        commitMessagesPool.add(commit);

        if(state != Utils.State.PREPARED) return;

        if(haveGotEnoughCommitMessages()) {
            System.out.println("Received enough commit messages");
            // then enter committed state
            state = Utils.State.COMMITTED;
            updatePendingPool(currentWorkedOnBlock);
            blockchain.add(currentWorkedOnBlock);
            state = Utils.State.FINAL_COMMITED;
            System.out.println("Now we are to start a new round");
            newRoundPhase();
        }

        /*if(validateCommit(commit)) {
            commitMessagesPool.add(commit);
            // wait for 2*(#nodes) / 3 valid commit messages
            //if(commitMessagesPool.size() >= 2 * NUMBER_OF_VALIDATORS / 3) {
            if(commitMessagesPool.size() >= 2) {
            //if(prepareMessagesPool.size() >= NUMBER_OF_VALIDATORS-1) {
                System.out.println("Received enough commit messages");
                // then enter committed state
                state = Utils.State.COMMITTED;
                updatePendingPool(currentWorkedOnBlock);
                blockchain.add(currentWorkedOnBlock);
                // TODO: validators append the received valid commit messages into the block
                state = Utils.State.FINAL_COMMITED;
                System.out.println("Now we are to start a new round");
                newRoundPhase();
            }
        }*/
    }

    public static boolean haveGotEnoughCommitMessages() {
        int validCount = 0;
        for(Commit commit : commitMessagesPool) {
            if(validateCommit(commit))
                validCount++;
        }

        return validCount >= 2; // 2 * NUMBER_OF_VALIDATORS / 3;
    }

    public static void collectPendingTransactions() {
        ArrayList<Transaction> toBeIncludedInBlock = new ArrayList<>();
        for(Map.Entry<String, Transaction> entry : pendingTxPool.entrySet()){
            toBeIncludedInBlock.add(entry.getValue());
        }
        //Transaction coinBase = calculateCoinBase(toBeIncludedInBlock);
        //toBeIncludedInBlock.add(0, coinBase);
        String root = MerkleTree.getMerkleTreeRoot(toBeIncludedInBlock);
        Block prev = getNewestBlock();
        Block toBeAdded = new Block(prev.getHash(), root, toBeIncludedInBlock, prev.height+1);
        formedABlock(toBeAdded);
    }

    public static void beginListening() throws IOException {
        // server is listening
        ServerSocket ss = new ServerSocket(myInfo.port);

        // running infinite loop for getting
        // client request
        while (true) {
            System.out.println("size: " + bftBroadcaster.connectedPeers);
            if(bftBroadcaster.connectedPeers >= NetworkInfo.NODE_INFOS.length-1)
                break;
            //if(nodeNumber == NetworkInfo.NODE_INFOS.length-1)
            //    break;

            Socket socket = null;

            try {
                // socket object to receive incoming client requests
                socket = ss.accept();
                bftBroadcaster.connectWithPeers();

                System.out.println("A new peer is connected : " + socket);
                bftBroadcaster.connectedPeers++;

                // obtaining input and out streams
                DataInputStream dis = new DataInputStream(socket.getInputStream());
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

                System.out.println("Assigning new thread for this peer");

                Thread t = new BFTListener(socket, dis, dos);
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
        if(!valid) {
            notValid++;
            System.out.println("Invalid Transaction "+transaction.index);
        }
        if(!firstSpending)
            doubleSpending ++;
    }

    private static void populateUTxOPool(Transaction transaction) {
        // remove all spent UTXOs
        if (!transaction.isCoinBase()) {
            for (int i = 0; i < transaction.inputs.length; i++) {
                Transaction prevTx=null;
                do {
                    prevTx = getTransaction(transaction.inputs[i].previousTransactionHash);
                    if(prevTx==null)System.out.println(transaction.index+" 3");
                }while(prevTx==null);
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
            Transaction prevTx = null;
            do {
                prevTx = getTransaction(transaction.inputs[i].previousTransactionHash);
                if(prevTx==null)System.out.println(transaction.index+" 4");

            }while(prevTx==null);
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
        if (pendingTxPool.containsKey(previousTransactionHash))
            return pendingTxPool.get(previousTransactionHash);
        else {

            for (int i = 0 ;i<blockchain.size();i++) {
                Block bl = blockchain.get(i);
                if (bl.contains(previousTransactionHash))
                    return bl.get(previousTransactionHash);

                // System.out.println(previousTransactionHash + " not found");
            }
        }

        // search pending
        if (pendingTxPool.containsKey(previousTransactionHash))
            return pendingTxPool.get(previousTransactionHash);

        return null;
    }

    // isn't needed in bft
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
        boolean condition2 = block.prevBlockHash.equalsIgnoreCase(blockchain.get(blockchain.size() - 1).getHash());
        return condition1 && condition2;
    }


    public static boolean validatePrePrepare(PrePrepare prePrepare) {
        boolean isValidBlock = validateBlock(prePrepare.block);
        boolean isValidSignature = Account.validateSignature(prePrepare.block.toString(), prePrepare.publicKey, prePrepare.signature, false);
        return isValidBlock && isValidSignature;
    }

    public static boolean validatePrepare(Prepare prepare) {
        if(currentWorkedOnBlock == null) return false;
        boolean isValidBlockHash = prepare.blockHash.equalsIgnoreCase(currentWorkedOnBlock.getHash());
        boolean isValidSignature = Account.validateSignature(prepare.blockHash, prepare.publicKey, prepare.signature, false);
        return isValidBlockHash && isValidSignature;
    }

    public static boolean validateCommit(Commit commit) {
        if(currentWorkedOnBlock == null) return false;
        boolean isValidBlockHash = commit.blockHash.equalsIgnoreCase(currentWorkedOnBlock.getHash());
        boolean isValidSignature = Account.validateSignature(commit.blockHash, commit.publicKey, commit.signature, false);
        return isValidBlockHash && isValidSignature;
    }

    public static void formedABlock(Block block) {
        Sign.SignatureData signature = account.signMessage(block.toString(), false);
        currentWorkedOnBlock = block;
        bftBroadcaster.broadcast(new PrePrepare(block, account.publicKey, signature));
        //updatePendingPool(block);
        //blockchain.add(block);
        System.out.println(block);
    }

    public static Block getNewestBlock() {
        if(blockchain.size() == 0) return null;
        return blockchain.get(blockchain.size()-1);
    }

    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);
        nodeNumber = sc.nextInt();
        myInfo = NetworkInfo.NODE_INFOS[nodeNumber];
        bftBroadcaster = new BFTBroadcaster(NetworkInfo.NODE_INFOS[nodeNumber]);
        System.out.println("Connect to peers!");
        bftBroadcaster.connectWithPeers();
        System.out.println("Begin Listening!");
        beginListening();
        System.out.println("Out of begin listening!!");
        try {
            // initialize with some transactions
            HashMap<Integer, Transaction> txList = DatasetParser.getTransactions();
            ArrayList<Integer> keys =  new ArrayList<>(txList.keySet());
            Collections.sort(keys);
            for(Integer key: keys) {
                //System.out.println(key);
                receivedNewTransaction(txList.get(key));
            }

            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Entering new round phase!!!!!");
            newRoundPhase();
            while(true);
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
    }
}
