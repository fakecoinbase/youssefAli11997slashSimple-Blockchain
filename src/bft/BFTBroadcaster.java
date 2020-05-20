package bft;

import bft.messages.Commit;
import bft.messages.PrePrepare;
import bft.messages.Prepare;
import blockchain.Block;
import blockchain.Transaction;
import network.NetworkInfo;
import network.NodeInfo;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.*;

public class BFTBroadcaster {
    public Set<Socket> sockets = new HashSet<>();
    private Scanner input = new Scanner(System.in);
    private static List<DataOutputStream> outputStreams = new ArrayList<>();
    private static List<ObjectOutputStream> objectOutputStreams = new ArrayList<>();
    public int connectedPeers = 0;

    private NodeInfo myInfo;

    public BFTBroadcaster(NodeInfo info) {
        myInfo = info;
    }

    public void connectWithPeers() {
        System.out.println("Here");
        for(NodeInfo nodeInfo : NetworkInfo.NODE_INFOS) {
            System.out.println("Itr!");

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

            System.out.println("\t should continue: " + shouldContinue);

            if(shouldContinue) continue;

            // establish a connection
            try {
                System.out.println("Establishing a connection");
                Socket socket = new Socket(nodeInfo.ipAddress, nodeInfo.port);
                System.out.println("Connected");
                //connectedPeers++;

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
        for(ObjectOutputStream outputStream : objectOutputStreams) {
            try {
                outputStream.writeObject(block);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void broadcast(PrePrepare prePrepare) {
        for(ObjectOutputStream outputStream : objectOutputStreams) {
            try {
                outputStream.writeObject(prePrepare);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void broadcast(Prepare prepare) {
        for(ObjectOutputStream outputStream : objectOutputStreams) {
            try {
                outputStream.writeObject(prepare);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void broadcast(Commit commit) {
        for(ObjectOutputStream outputStream : objectOutputStreams) {
            try {
                outputStream.writeObject(commit);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
