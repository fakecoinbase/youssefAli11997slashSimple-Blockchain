package blockchain;

import security_utils.Hash;

import java.io.Serializable;

public class Input implements Serializable {
    public String previousTransactionHash;
    public int outputIndex; // that is used as this input

    public Input(String previousTransactionHash, int outputIndex) {
        this.previousTransactionHash = previousTransactionHash;
        this.outputIndex = outputIndex;
    }

    @Override
    public String toString() {
        return "Input{" +
                "previousTransactionHash='" + previousTransactionHash + '\'' +
                ", outputIndex=" + outputIndex +
                '}';
    }

    public String getHash(){
        return Hash.getSHA256(toString());
    }
}