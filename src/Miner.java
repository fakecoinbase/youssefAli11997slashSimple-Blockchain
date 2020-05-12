import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Miner {

    private static List<Transaction> pendingTransactions = new ArrayList<>();
    private static List<Block> blockchain = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        beginListening();
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
        pendingTransactions.add(transaction);
    }

    public static synchronized void receivedNewBlock(Block block) {
        // TODO: verify block before adding to blockchains
        // TODO: handle forks
        blockchain.add(block);
    }
}
