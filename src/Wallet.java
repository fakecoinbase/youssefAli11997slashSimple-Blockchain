import java.io.IOException;

public class Wallet {
    private static Broadcaster broadcaster = new Broadcaster(new NetworkInfo.NodeInfo("",0));

    public static void main(String[] args) throws IOException {
        broadcaster.connectWithPeers();
        Input[] inputsArr = {new Input("xyz",1,"sign","wit")};
        broadcaster.broadcast(new Transaction(true,1, inputsArr, 0, new Output[0]));
    }
}
