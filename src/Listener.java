import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

public class Listener extends Thread {
    private Socket socket = null;
    private DataInputStream dis =  null;
    private DataOutputStream dos = null;

    private final Object LOCK = new Object();

    public Listener(Socket socket, DataInputStream in, DataOutputStream out) {
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
                Miner.receivedNewTransaction(transaction);

                System.out.println(transaction.toString());
            }
            else if(object instanceof Block) {
                Block block = (Block) object;
                Miner.receivedNewBlock(block);
            }
        }
        catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        String received;

        //while(true) {
            receive();
        //}
    }
}
