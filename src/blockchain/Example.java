package blockchain;

import blockchain.*;
import nodes.Miner;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Sign;
import security_utils.Hash;
import security_utils.MerkleTree;

import java.io.FileNotFoundException;
import java.lang.reflect.Array;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
import java.util.Arrays;

public class Example {

    public static void main(String[] args) throws FileNotFoundException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        /*Account a0 = new Account(new BigInteger("70893185459726002695592608125398790426333873995722573030079532815328280498190"), new BigInteger("8215640083585108509366447839296126441084574042232327306701634491944088710415765502464597092981937927688869353556143662385303665231368190017969296472859459"));
        System.out.println(a0.privateKey);
        System.out.println(a0.publicKey);
        Account a1 = new Account(new BigInteger("82271208259943611701582848519997004421462029627958678228983483907593411458324"), new BigInteger("3286623872192439214729904261484080141965504753974924126111814573191674956153250025532403318294624692814286073000869959678347177542365158516792121776399601"));
        System.out.println(a1.privateKey);
        System.out.println(a1.publicKey);
        Account a2 = new Account(new BigInteger("79173443666829951390411152092696190420999761076539009525875505760997011349356"), new BigInteger("11906666409040611612527693054961072436127022027492143227529128140960529201801252223140880511264335214089358086709037088531043718423834267287065591749848933"));
        System.out.println(a2.privateKey);
        System.out.println(a2.publicKey);
        Output o1 = new Output(15.24179, 0, a1.address);
        Output [] o1s = new Output[]{o1};
        Transaction t1 = new Transaction(false, 0, new Sign.SignatureData[]{}, new Input[]{}, a0.publicKey, 1, o1s, a0.signMessage(Arrays.toString(o1s), false));
        Miner.receivedNewTransaction(t1);
        System.out.println(Miner.pendingTxPool.entrySet());
        System.out.println(Miner.uTxoPool);
        Input i1 = new Input(t1.getHash(), 0);
        Input[] i1s = new Input[]{i1};
        Output o2 = new Output(15.24179, 0, a2.address);
        Output [] o2s = new Output[]{o2};
        Sign.SignatureData i1s1 = a1.signMessage(o1.toString(), false);
        Sign.SignatureData [] i1s1s = new Sign.SignatureData[]{i1s1};
        Transaction t2 = new Transaction(true, 1, i1s1s, i1s, a1.publicKey, 1, o2s, a1.signMessage(Arrays.toString(o2s), false));
        Miner.receivedNewTransaction(t2);
        System.out.println(Miner.pendingTxPool.entrySet());
        System.out.println(Miner.uTxoPool);
        Transaction t3 = new Transaction(true, 1, i1s1s, i1s, a1.publicKey, 1, o2s, a1.signMessage(Arrays.toString(o2s), false));
        Miner.receivedNewTransaction(t3);
        System.out.println(Miner.pendingTxPool.entrySet());
        System.out.println(Miner.uTxoPool);
        ArrayList<Transaction> txs = new ArrayList<>();
        txs.add(t1);
        txs.add(t2);
        String prev = "PrevBlockSupposedToBeGenesisWillBeAddedSoon";
        Block bl = new Block(Hash.getSHA256(prev), MerkleTree.getMerkleTreeRoot(txs), txs);
        Block newB = ProofOfWork.pow(bl, 3);
        System.out.println(newB.nonce);
        System.out.println(newB.getHash());*/
        /*BigInteger publicKey = new BigInteger("70893185459726002695592608125398790426333873995722573030079532815328280498190");
        BigInteger privateKey = new BigInteger("8215640083585108509366447839296126441084574042232327306701634491944088710415765502464597092981937927688869353556143662385303665231368190017969296472859459");
        Output output = new Output(50, 0 , Account.getAddress(publicKey));
        Output [] ots = new Output[]{output};
        Sign.SignatureData signatureData = Account.signMessage(Arrays.toString(ots), new ECKeyPair(publicKey, privateKey), false);
        System.out.println(Arrays.toString(signatureData.getV()));
        System.out.println(Arrays.toString(signatureData.getR()));
        System.out.println(Arrays.toString(signatureData.getS()));*/
    }
}
