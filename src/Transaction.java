public class Transaction {
    class Input {
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
    }

    class Output {
        int value;
        int outputIndex;
        String payeePublicKey;

        public Output(int value, int outputIndex, String payeePublicKey) {
            this.value = value;
            this.outputIndex = outputIndex;
            this.payeePublicKey = payeePublicKey;
        }
    }

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
}
