import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class MerkleTree {
    public static String getMerkleTreeRoot(List<Transaction> transactionList){
        ArrayList<String> listOfHashes = new ArrayList<>();
        for(Transaction tx: transactionList)
            listOfHashes.add(tx.getHash());
        while (Integer.bitCount(listOfHashes.size()) != 1 || listOfHashes.size() == 1){
            listOfHashes.add(listOfHashes.get(listOfHashes.size()-1));
        }
        ArrayList<String> res = listOfHashes;
        System.out.println(res.size());
        while(res.size() != 1){
            ArrayList<String> tmp = new ArrayList<>();
            for(int i = 0 ; i < res.size() ; i += 2){
                String firstHash = res.get(i);
                String secHash = res.get(i+1);
                String concat = firstHash + secHash;
                String newHash = Hash.getSHA256(concat);
                tmp.add(newHash);
            }
            res = tmp;
        }
        return res.get(0);
    }
}
