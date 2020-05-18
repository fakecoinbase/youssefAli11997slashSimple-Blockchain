package bft.messages;

import bft.Validator;
import blockchain.Block;
import org.web3j.crypto.Sign;

import java.math.BigInteger;

public class PrePrepare {
    public Block block;
    public BigInteger publicKey;
    public Sign.SignatureData signature;


    public PrePrepare(Block block, BigInteger publicKey, Sign.SignatureData signature) {
        this.block = block;
        this.publicKey = publicKey;
        this.signature = signature;
    }
}
