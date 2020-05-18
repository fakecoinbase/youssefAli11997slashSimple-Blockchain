package blockchain;

import org.web3j.crypto.Sign;
import security_utils.Hash;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Arrays;

public class Transaction implements Serializable {
    public boolean witnessFlag;
    public int inputCounter;
    public Input[] inputs;
    //public Sign.SignatureData [] signatures;
    public SerializableSignature [] signatures;
    public BigInteger publicKey;
    public int outputCounter;
    public Output[] outputs;
    public SerializableSignature outputSignature;
    //public Sign.SignatureData outputSignature;
    public int index;
    public Transaction(boolean witnessFlag, int inputCounter, Sign.SignatureData [] signatures, Input[] inputs, BigInteger publicKey, int outputCounter, Output[] outputs, Sign.SignatureData outputSignature) {
        this.witnessFlag = witnessFlag;
        this.inputCounter = inputCounter;
        this.inputs = inputs;
        this.signatures = SerializableSignature.toSerializable(signatures);
        this.publicKey = publicKey;
        this.outputCounter = outputCounter;
        this.outputs = outputs;
        this.outputSignature = SerializableSignature.toSerializable(outputSignature);
    }
    public Transaction(boolean witnessFlag, int inputCounter, Sign.SignatureData [] signatures, Input[] inputs, BigInteger publicKey, int outputCounter, Output[] outputs, Sign.SignatureData outputSignature,int index) {
        this.witnessFlag = witnessFlag;
        this.inputCounter = inputCounter;
        this.inputs = inputs;
        this.signatures = SerializableSignature.toSerializable(signatures);
        this.publicKey = publicKey;
        this.outputCounter = outputCounter;
        this.outputs = outputs;
        this.outputSignature = SerializableSignature.toSerializable(outputSignature);
        this.index=index;
    }
    @Override
    public String toString() {
        return "Transaction{" +
                "witnessFlag=" + witnessFlag +
                ", inputCounter=" + inputCounter +
                ", inputs=" + Arrays.toString(inputs) +
                ", signatures=" + Arrays.hashCode(SerializableSignature.getSignature(signatures)) +
                ", publicKey=" + publicKey +
                ", outputCounter=" + outputCounter +
                ", outputs=" + Arrays.toString(outputs) +
                ", outputSignature=" + SerializableSignature.getSignature(outputSignature).hashCode() +
                '}';
    }
    public void setIndex(int inde){
        this.index=inde;
    }

    public String getHash(){
        return Hash.getSHA256(toString());
    }

    public boolean isCoinBase() {
        return !witnessFlag;
    }
}