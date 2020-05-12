import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Broadcaster extends Thread {
    // initialize socket and input output streams
    private Socket socket = null;
    private Scanner input = null;
    private DataOutputStream out = null;

    private NetworkInfo.NodeInfo myInfo;

    Broadcaster(NetworkInfo.NodeInfo info) {
        myInfo = info;
        start();
    }

    @Override
    public void run() {
        for(NetworkInfo.NodeInfo nodeInfo : NetworkInfo.NODE_INFOS) {
            if(myInfo.ipAddress.equals(nodeInfo.ipAddress) && myInfo.port == nodeInfo.port)
                continue;
            // establish a connection
            try {
                socket = new Socket(nodeInfo.ipAddress, nodeInfo.port);
                System.out.println("Connected");

                // takes input from terminal
                input = new Scanner(System.in);

                // sends output to the socket
                out = new DataOutputStream(socket.getOutputStream());
            }
            catch(IOException u) {
                u.printStackTrace();
            }


            try {
                out.writeUTF("Hello from the other side");
            }
            catch (IOException e) {
                e.printStackTrace();
            }

            // close the connection
            try {
                input.close();
                if (out != null) {
                    out.close();
                }
                if (socket != null) {
                    socket.close();
                }
            }
            catch(IOException i) {
                System.out.println(i);
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("I am broadcaster");
        Broadcaster broadcaster = new Broadcaster(new NetworkInfo.NodeInfo("", 0));
    }
}
