package blockchain;

import security_utils.Hash;

import java.math.BigInteger;

public class ProofOfWork {

    public static Block pow(Block block, int diff){
        Block newBl = new Block(block);
        while(!checkZeros(diff, newBl.getHash())){
            newBl.nonce +=1;
        }
        return newBl;
    }

    public static boolean checkZeros(int numberOfZeros, String hash){
       StringBuffer binary =  new StringBuffer(new BigInteger(hash, 16).toString(2));
       while(binary.length() < Hash.HASH_BITS_COUNT){
           binary.insert(0, 0);
       }
       int initialZeros = 0;
       for(int i = 0 ; i < binary.length() ; i ++){
           if(binary.charAt(i) == '0'){
               initialZeros ++;
               if(initialZeros >= numberOfZeros){
                   return true;
               }
           }
           else{
               if(initialZeros >= numberOfZeros){
                   return true;
               }
               else{
                   return false;
               }
           }
       }
       return false;
    }

}
