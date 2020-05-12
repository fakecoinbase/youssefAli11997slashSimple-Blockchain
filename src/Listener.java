import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Listener extends Thread {
    //initialize socket and input stream
    private Socket socket = null;
    private DataInputStream dis =  null;
    private DataOutputStream dos = null;

    public Listener(Socket socket, DataInputStream in, DataOutputStream out) {
        this.socket = socket;
        this.dis = in;
        this.dos = out;
    }

    @Override
    public void run() {
        String received;

        //while(true) {
            try {
                received = dis.readUTF();
                System.out.println(received);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        //}
    }
}
