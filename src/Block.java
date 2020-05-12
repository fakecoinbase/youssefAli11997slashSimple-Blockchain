import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Block implements Serializable {

    private String prevBlockHash;
    private String merkleRootHash;
    private final long timestamp = new Date().getTime();
    private List<Transaction> transactions = new ArrayList<>();

    public Block(String prevBlockHash, String merkleRootHash, List<Transaction> transactions) {
        this.prevBlockHash = prevBlockHash;
        this.merkleRootHash = merkleRootHash;
        this.transactions = transactions;
    }
}
