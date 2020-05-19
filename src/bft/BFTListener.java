package bft;

import bft.messages.Commit;
import bft.messages.PrePrepare;
import bft.messages.Prepare;
import blockchain.Block;
import blockchain.Transaction;
import nodes.Miner;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

public class BFTListener extends Thread {
    private Socket socket = null;
    private DataInputStream dis =  null;
    private DataOutputStream dos = null;
    private boolean receiveCondition;

    public BFTListener(Socket socket, DataInputStream in, DataOutputStream out) {
        this.socket = socket;
        this.dis = in;
        this.dos = out;
    }

    public void receive() {
        ObjectInputStream in = null;
        try {
            in = new ObjectInputStream(dis);
            Object object = in.readObject();

            if(object instanceof Transaction) {
                Transaction transaction = (Transaction) object;
                Validator.receivedNewTransaction(transaction);

                System.out.println(transaction.toString());
            }
            else if(object instanceof Block) { // is not really used in BFT
                Block block = (Block) object;
                Validator.receivedNewBlock(block);
            }
            else if(object instanceof PrePrepare) {
                PrePrepare prePrepare = (PrePrepare) object;
                System.out.println("Received pre-prepare message");
                System.out.println(prePrepare);
                Validator.receivedPrePrepareMessage(prePrepare);
            }
            else if(object instanceof Prepare) {
                Prepare prepare = (Prepare) object;
                System.out.println("Received prepare message");
                System.out.println(prepare);
                Validator.receivedPrepareMessage(prepare);
            }
            else if(object instanceof Commit) {
                Commit commit = (Commit) object;
                System.out.println("Received commit message");
                System.out.println(commit);
                Validator.receivedCommitMessage(commit);
            }
        }
        catch (IOException | ClassNotFoundException e) {
            this.receiveCondition = false;
        }
    }

    @Override
    public void run() {
        while(receiveCondition) {
            receive();
        }
    }
}
