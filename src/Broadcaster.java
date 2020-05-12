import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Broadcaster {
    private List<Socket> sockets = new ArrayList<>();
    private Scanner input = new Scanner(System.in);
    private List<DataOutputStream> outputStreams = new ArrayList<>();

    private NetworkInfo.NodeInfo myInfo;

    Broadcaster(NetworkInfo.NodeInfo info) {
        myInfo = info;
    }

    public void connectWithPeers() {
        for(NetworkInfo.NodeInfo nodeInfo : NetworkInfo.NODE_INFOS) {

            // skip my own node
            if(myInfo.ipAddress.equals(nodeInfo.ipAddress) && myInfo.port == nodeInfo.port)
                continue;

            // establish a connection
            try {
                Socket socket = new Socket(nodeInfo.ipAddress, nodeInfo.port);
                System.out.println("Connected");

                DataOutputStream out = new DataOutputStream(socket.getOutputStream());

                sockets.add(socket);
                outputStreams.add(out);
            }
            catch(IOException u) {
                u.printStackTrace();
            }
        }
    }

    public void broadcast(String msg) {
        for(DataOutputStream outputStream : outputStreams) {
            try {
                outputStream.writeUTF(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void broadcast(Transaction transaction) {
        for(DataOutputStream outputStream : outputStreams) {
            try {
                ObjectOutputStream os = new ObjectOutputStream(outputStream);
                os.writeObject(transaction);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
