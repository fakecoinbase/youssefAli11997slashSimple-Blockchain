package bft.messages;

import blockchain.Block;
import blockchain.SerializableSignature;
import org.web3j.crypto.Sign;

import java.io.Serializable;
import java.math.BigInteger;

public class PrePrepare implements Serializable {
    public Block block;
    public BigInteger publicKey;
    public SerializableSignature signature;


    public PrePrepare(Block block, BigInteger publicKey, Sign.SignatureData signature) {
        this.block = block;
        this.publicKey = publicKey;
        this.signature = SerializableSignature.toSerializable(signature);
    }
}
