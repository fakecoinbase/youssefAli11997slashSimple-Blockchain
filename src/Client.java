import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    // initialize socket and input output streams
    private Socket socket = null;
    private Scanner input = null;
    private DataOutputStream out = null;

    // constructor to put ip address and port
    public Client(String address, int port) {
        // establish a connection
        try {
            socket = new Socket(address, port);
            System.out.println("Connected");

            // takes input from terminal
            input = new Scanner(System.in);

            // sends output to the socket
            out = new DataOutputStream(socket.getOutputStream());
        } catch(IOException u) {
            u.printStackTrace();
        }

        // string to read message from input
        String line = "";

        // keep reading until "Over" is input
        while (!line.equals("Over")) {
            line = input.nextLine();
            System.out.println(line);
            if (out != null) {
                try {
                    out.writeUTF(line);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
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

    public static void main(String[] args) {
        Client client = new Client("127.0.0.1", 5000);
    }
}
