package network;

import blockchain.Block;
import blockchain.Transaction;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.*;

public class Broadcaster {
    private static Set<Socket> sockets = new HashSet<>();
    private Scanner input = new Scanner(System.in);
    private static List<DataOutputStream> outputStreams = new ArrayList<>();
    private static List<ObjectOutputStream> objectOutputStreams = new ArrayList<>();

    private NodeInfo myInfo;

    public Broadcaster(NodeInfo info) {
        myInfo = info;
    }

    public void connectWithPeers() {
        for(NodeInfo nodeInfo : NetworkInfo.NODE_INFOS) {

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
                objectOutputStreams.add(new ObjectOutputStream(out));
            }
            catch(IOException u) {
                //u.printStackTrace();
            }
        }
    }

    public static void addNewSocket(Socket socket) {
        sockets.add(socket);
    }
    public static void addNewOutputStream(DataOutputStream dos) {
        outputStreams.add(dos);
        System.out.println(outputStreams.size());
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
        /*for(DataOutputStream outputStream : outputStreams) {
            try {
                ObjectOutputStream os = new ObjectOutputStream(outputStream);
                os.writeObject(transaction);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/
        for(ObjectOutputStream outputStream : objectOutputStreams) {
            try {
                outputStream.writeObject(transaction);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void broadcast(Block block) {
        /*for(DataOutputStream outputStream : outputStreams) {
            try {
                System.out.println("BROADCASTING!!!!");
                ObjectOutputStream os = new ObjectOutputStream(outputStream);
                os.writeObject(block);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/
        for(ObjectOutputStream outputStream : objectOutputStreams) {
            try {
                System.out.println("BROADCASTING!!!!");
                outputStream.writeObject(block);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
