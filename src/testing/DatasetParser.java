package testing;

import blockchain.Account;
import blockchain.Input;
import blockchain.Output;
import blockchain.Transaction;
import org.web3j.crypto.Sign;

import java.io.File;
import java.io.FileNotFoundException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

public class DatasetParser {
    public static String filePath = "/home/mashaal/Desktop/Simple Blockchain/txdataset_v2.txt";
    public static ArrayList<Account> accounts;
    //public static ArrayList<Transaction> transactions;
    public static HashMap<Integer, Transaction> transactions;
    public static Account baseAccount;
    static {
        accounts = new ArrayList<>();
        //transactions = new ArrayList<>();
        transactions = new HashMap<>();
        try {
            baseAccount = new Account();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
        accounts.add(baseAccount);
    }

    public static HashMap<Integer, Transaction> getTransactions() throws FileNotFoundException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        File dataSet = new File(filePath);
        Scanner sc = new Scanner(dataSet);
        sc.nextLine();
        while(sc.hasNextLine()){
            String nextLine = sc.nextLine();
            String [] tokens = nextLine.split("\\s+");
            if(tokens.length == 4){
                int txIndex = Integer.parseInt(tokens[0]);
                Account newAccount = new Account();
                accounts.add(newAccount);
                String [] value = tokens[2].split(":");
                double val = Double.parseDouble(value[1]);
                transactions.put(txIndex, createNewCoinBase(val, baseAccount, newAccount));
            }
            else if (tokens.length == 10){
                int txIndex = Integer.parseInt(tokens[0]);
                int accountIndex = Integer.parseInt(tokens[1].split(":")[1]);
                int prevTxIndex = Integer.parseInt(tokens[2].split(":")[1]);
                int outIndex = Integer.parseInt(tokens[3].split(":")[1]) - 1;
                Input input = new Input(transactions.get(prevTxIndex).getHash(), outIndex);
                ArrayList<Output> outputs = new ArrayList<>();
                for(int i = 4 , j = 0 ; i < tokens.length ; i+=2, j++){
                    double val = Double.parseDouble(tokens[i].split(":")[1]);
                    int toIndex = Integer.parseInt(tokens[i+1].split(":")[1]);
                    Output output = new Output(val, j, accounts.get(toIndex).address);
                    outputs.add(output);
                }
                transactions.put(txIndex, createTransaction(transactions.get(prevTxIndex), new Input[]{input}, accounts.get(accountIndex), outputs.toArray(new Output[outputs.size()])));
            }
        }
        return transactions;
    }

    private static Transaction createTransaction(Transaction prevTx,Input[] inputs, Account account, Output[] outputs) {
        Sign.SignatureData [] inputSig = new Sign.SignatureData[inputs.length];
        for(int i  = 0 ; i < inputs.length ; i ++){
            Output output = prevTx.outputs[inputs[i].outputIndex];
            inputSig[i] = account.signMessage(output.toString(), false);
        }
        Sign.SignatureData outputSig = account.signMessage(Arrays.toString(outputs), false);
        return new Transaction(true, inputs.length, inputSig, inputs, account.publicKey, outputs.length, outputs, outputSig);
    }

    private static Transaction createNewCoinBase(double val, Account baseAccount, Account toAccount) {
        Output ot = new Output(val, 0, toAccount.address);
        Output [] ots = new Output[]{ot};
        Transaction tx = new Transaction(false, 0, new Sign.SignatureData[]{}, new Input[]{},
                baseAccount.publicKey, 1, ots, baseAccount.signMessage(Arrays.toString(ots), false));
        return tx;
    }

}
