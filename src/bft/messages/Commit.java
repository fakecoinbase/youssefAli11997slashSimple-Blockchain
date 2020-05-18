package bft.messages;

import org.web3j.crypto.Sign;

import java.math.BigInteger;

public class Commit {
    public String blockHash;
    public BigInteger publicKey;
    public Sign.SignatureData signature;


    public Commit(String blockHash) {
        this.blockHash = blockHash;
    }
}
