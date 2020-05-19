package bft.messages;

import blockchain.SerializableSignature;
import org.web3j.crypto.Sign;

import java.io.Serializable;
import java.math.BigInteger;

public class Commit implements Serializable {
    public String blockHash;
    public BigInteger publicKey;
    public SerializableSignature signature;


    public Commit(String blockHash, BigInteger publicKey, Sign.SignatureData signature) {
        this.blockHash = blockHash;
        this.publicKey = publicKey;
        this.signature = SerializableSignature.toSerializable(signature);
    }
}
