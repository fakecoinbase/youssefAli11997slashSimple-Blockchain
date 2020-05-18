package bft.messages;

import org.web3j.crypto.Sign;

import java.math.BigInteger;

public class Prepare {
    public String blockHash;
    public BigInteger publicKey;
    public Sign.SignatureData signature;


    public Prepare(String blockHash) {
        this.blockHash = blockHash;
    }
}
