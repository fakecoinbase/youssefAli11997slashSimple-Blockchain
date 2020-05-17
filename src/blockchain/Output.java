package blockchain;

import security_utils.Hash;

import java.io.Serializable;

public class Output implements Serializable {
    //TODO SUPPORT A SPECIFIC PRECISION OR USE BIG DECIMAL
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