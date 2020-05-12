import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Transaction implements Serializable {
    private boolean witnessFlag;
    private int inputCounter;
    private Input[] inputs;
    private int outputCounter;
    private Output[] outputs;

    public Transaction(boolean witnessFlag, int inputCounter, Input[] inputs, int outputCounter, Output[] outputs) {
        this.witnessFlag = witnessFlag;
        this.inputCounter = inputCounter;
        this.inputs = inputs;
        this.outputCounter = outputCounter;
        this.outputs = outputs;
    }

    @Override
    public String toString() {
        return "flag: " + witnessFlag + "\n" +
                "ipCounter: " + inputCounter + "\n" +
                "inputs size: " + inputs.length + "\n" +
                "first input: " + inputs[0] + "\n";
    }
}

class Input implements Serializable {
    String previousTransactionHash;
    int outputIndex; // that is used as this input
    String payerSignature;
    String witness;

    public Input(String previousTransactionHash, int outputIndex, String payerSignature, String witness) {
        this.previousTransactionHash = previousTransactionHash;
        this.outputIndex = outputIndex;
        this.payerSignature = payerSignature;
        this.witness = witness;
    }

    @Override
    public String toString() {
        return "prevTxHash: " + previousTransactionHash + "\n" +
                "opIdx: " + outputIndex + "\n" +
                "payerSignature: " + payerSignature + "\n" +
                "witness: " + witness + "\n";
    }
}

class Output implements Serializable {
    int value;
    int outputIndex;
    String payeePublicKey;

    public Output(int value, int outputIndex, String payeePublicKey) {
        this.value = value;
        this.outputIndex = outputIndex;
        this.payeePublicKey = payeePublicKey;
    }
}