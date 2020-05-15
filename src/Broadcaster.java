import jnr.constants.platform.Sock;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.*;

public class Broadcaster {
    private static Set<Socket> sockets = new HashSet<>();
    private Scanner input = new Scanner(System.in);
    private static List<DataOutputStream> outputStreams = new ArrayList<>();

    private NetworkInfo.NodeInfo myInfo;

    Broadcaster(NetworkInfo.NodeInfo info) {
        myInfo = info;
    }

    public void connectWithPeers() {
        for(NetworkInfo.NodeInfo nodeInfo : NetworkInfo.NODE_INFOS) {

            // skip my own node
            if(myInfo.ipAddress.equals(nodeInfo.ipAddress) && myInfo.port == nodeInfo.port)
                continue;

            // skip already connected to nodes
            boolean shouldContinue = false;
            for(Socket socket : sockets) {
                String s = socket.getRemoteSocketAddress().toString();
                String remoteIp = s.replace("/","").substring(0, s.indexOf(":")-1);
                if(nodeInfo.ipAddress.equals(remoteIp) && nodeInfo.port == socket.getPort()){
                    shouldContinue = true;
                }
            }

            if(shouldContinue) continue;

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

    public static void addNewSocket(Socket socket) {
        sockets.add(socket);
    }
    public static void addNewOutputStream(DataOutputStream dos) {
        outputStreams.add(dos);
    }

    public void broadcast(String msg) {
        connectWithPeers();
        for(DataOutputStream outputStream : outputStreams) {
            try {
                outputStream.writeUTF(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void broadcast(Transaction transaction) {
        connectWithPeers();
        for(DataOutputStream outputStream : outputStreams) {
            try {
                ObjectOutputStream os = new ObjectOutputStream(outputStream);
                os.writeObject(transaction);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void broadcast(Block block) {
        connectWithPeers();
        for(DataOutputStream outputStream : outputStreams) {
            try {
                ObjectOutputStream os = new ObjectOutputStream(outputStream);
                os.writeObject(block);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
