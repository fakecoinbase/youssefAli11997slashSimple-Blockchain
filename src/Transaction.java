import org.web3j.crypto.Sign;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Arrays;

public class Transaction implements Serializable {
    public boolean witnessFlag;
    public int inputCounter;
    public Input[] inputs;
    public Sign.SignatureData [] signatures;
    public BigInteger publicKey;
    public int outputCounter;
    public Output[] outputs;
    public Sign.SignatureData outputSignature;

    public Transaction(boolean witnessFlag, int inputCounter, Sign.SignatureData [] signatures, Input[] inputs, BigInteger publicKey, int outputCounter, Output[] outputs, Sign.SignatureData outputSignature) {
        this.witnessFlag = witnessFlag;
        this.inputCounter = inputCounter;
        this.inputs = inputs;
        this.signatures = signatures;
        this.publicKey = publicKey;
        this.outputCounter = outputCounter;
        this.outputs = outputs;
        this.outputSignature = outputSignature;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "witnessFlag=" + witnessFlag +
                ", inputCounter=" + inputCounter +
                ", inputs=" + Arrays.toString(inputs) +
                ", signatures=" + Arrays.hashCode(signatures) +
                ", publicKey=" + publicKey +
                ", outputCounter=" + outputCounter +
                ", outputs=" + Arrays.toString(outputs) +
                ", outputSignature=" + outputSignature.hashCode() +
                '}';
    }


    public String getHash(){
        return Hash.getSHA256(toString());
    }

    public boolean isCoinBase() {
        return !witnessFlag;
    }
}

class Input implements Serializable {
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

class Output implements Serializable {
    public double value;
    public int outputIndex;
    public String address;

    public Output(double value, int outputIndex, String address) {
        this.value = value;
        this.outputIndex = outputIndex;
        this.address = address;
    }

    @Override
    public String toString() {
        return "Output{" +
                "value=" + value +
                ", outputIndex=" + outputIndex +
                ", address='" + address + '\'' +
                '}';
    }

    public String getHash(){
        return Hash.getSHA256(toString());
    }
}