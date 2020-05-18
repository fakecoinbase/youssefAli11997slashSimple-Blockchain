package blockchain;

import org.web3j.crypto.Sign;

import java.io.Serializable;

public class SerializableSignature implements Serializable {
    byte [] v;
    byte [] r;
    byte [] s;

    public SerializableSignature(byte [] v, byte [] r, byte [] s){
        this.v = v;
        this.r = r;
        this.s = s;
    }

    public static Sign.SignatureData getSignature(SerializableSignature serializableSignature){
        return new Sign.SignatureData(serializableSignature.v, serializableSignature.r, serializableSignature.s);
    }

    public static SerializableSignature toSerializable(Sign.SignatureData signature){
        return new SerializableSignature(signature.getV(), signature.getR(), signature.getS());
    }

    public static Sign.SignatureData [] getSignature(SerializableSignature [] serializableSignatures){
        Sign.SignatureData [] signatures = new Sign.SignatureData[serializableSignatures.length];
        for(int i = 0 ; i < serializableSignatures.length ; i ++){
            signatures[i] = getSignature(serializableSignatures[i]);
        }
        return signatures;
    }

    public static SerializableSignature [] toSerializable(Sign.SignatureData [] signatures){
        SerializableSignature [] serializableSignatures = new SerializableSignature [signatures.length];
        for(int i = 0 ; i < signatures.length ; i ++){
            serializableSignatures[i] = toSerializable(signatures[i]);
        }
        return serializableSignatures;
    }
}
