package blockchain;

import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Hash;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;

import java.math.BigInteger;
import java.security.*;

public class Account {
    public ECKeyPair keyPair;
    public BigInteger publicKey;
    public BigInteger privateKey;
    public String address;

    public Account() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        keyPair = Keys.createEcKeyPair();
        publicKey = keyPair.getPublicKey();
        privateKey = keyPair.getPrivateKey();
        address = Keys.getAddress(keyPair);
    }

    public Account(BigInteger privateKey, BigInteger publicKey){
        keyPair = new ECKeyPair(privateKey, publicKey);
        this.publicKey = keyPair.getPublicKey();
        this.privateKey = keyPair.getPrivateKey();
        address = Keys.getAddress(keyPair);
    }

    public static String getAddress(BigInteger publicKey){
        return Keys.getAddress(publicKey);
    }

    public Sign.SignatureData signMessage(String message, boolean toBeHashed){
        byte[] msgHash = Hash.sha3(message.getBytes());
        return Sign.signMessage(msgHash, keyPair, toBeHashed);
    }

    public static Sign.SignatureData signMessage(String message, ECKeyPair keyPair, boolean toBeHashed){
        byte[] msgHash = Hash.sha3(message.getBytes());
        return Sign.signMessage(msgHash, keyPair, toBeHashed);
    }

    public boolean validateSignature(String message , Sign.SignatureData signature, boolean hashed){
        boolean vaild = false;
        BigInteger pubKeyRecovered;
        try {
            if(!hashed){
                pubKeyRecovered = Sign.signedMessageToKey(message.getBytes(), signature);
            }
            else {
                pubKeyRecovered = Sign.signedMessageHashToKey(message.getBytes(), signature);
            }
            if(pubKeyRecovered.equals(publicKey))
                vaild = true;
        } catch (SignatureException e) {
            e.printStackTrace();
        }
        return vaild;
    }

    public boolean validateSignature(String message , SerializableSignature signature, boolean hashed){
        return validateSignature(message, SerializableSignature.getSignature(signature), hashed);
    }

    public static boolean validateSignature(String message, BigInteger fullPubKey , Sign.SignatureData signature, boolean hashed){
        boolean vaild = false;
        BigInteger pubKeyRecovered;
        try {
            if(!hashed){
                pubKeyRecovered = Sign.signedMessageToKey(message.getBytes(), signature);
            }
            else {
                pubKeyRecovered = Sign.signedMessageHashToKey(message.getBytes(), signature);
            }
            if(pubKeyRecovered.equals(fullPubKey))
                vaild = true;
        } catch (SignatureException e) {
            e.printStackTrace();
        }
        return vaild;
    }

    public static boolean validateSignature(String message, BigInteger fullPubKey , SerializableSignature signature, boolean hashed){
        return validateSignature(message, fullPubKey, SerializableSignature.getSignature(signature), hashed);
    }

    public static boolean validateAddress(BigInteger fullPubKey, String address){
        String decodedPubKey = Keys.getAddress(fullPubKey);
        return decodedPubKey.equalsIgnoreCase(address);
    }
}
