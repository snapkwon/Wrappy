package net.wrappy.im.GethService;

/**
 * Created by sonntht on 28/02/2017.
 */


import android.content.res.AssetManager;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.wrappy.im.GethService.contracts.PRDToken;
import net.wrappy.im.GethService.contracts.WrappyKYC;
import net.wrappy.im.GethService.utils.JSONParser;

import org.ethereum.geth.Account;
import org.ethereum.geth.Address;
import org.ethereum.geth.BigInt;
import org.ethereum.geth.Block;
import org.ethereum.geth.Context;
import org.ethereum.geth.EthereumClient;
import org.ethereum.geth.Geth;
import org.ethereum.geth.KeyStore;
import org.ethereum.geth.Node;
import org.ethereum.geth.NodeConfig;
import org.ethereum.geth.Signer;
import org.ethereum.geth.SyncProgress;
import org.ethereum.geth.TransactOpts;
import org.ethereum.geth.Transaction;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class Wallet {
    private static Wallet wallet = null;
    private static Signer mySigner;
    private Context context;
    private static final DecimalFormat decimalFormatter = new DecimalFormat("0.00##", new DecimalFormatSymbols(Locale.US));
    private PRDToken prd;
    private WrappyKYC kyc;
    private Double prdRate = 0.30;

    public EthereumClient ethereumClient;
    public String proteusionPath;
    public JSONObject coinInfo;
    public Node node;
    public KeyStore accountManager;

    private final static int GAS_PRICE = 21001;
    private final static int GAS_PRICE_WITH_TEXT = 121001;
    private final static int GAS_PRICE_PRO = 61001;

    private static int NETWORK_ID = 1;
    private final static boolean USE_TESTNET = true;
    public final static String TESTNET_PATH = "/.ethereum/rinkeby";
    public final static String MAINNET_PATH = "/.ethereum";
    public final static String KEYSTORE_PATH = "/keystore";
    public final static String GETHDROID_PATH =  "/GethDroid";
    private String wrappyPrivateKey = "7ac1fb493c19fa3a499e93215ba75314d6d4e969244ac8c98515ecb1df367dd8";
    private String companyAddress = "0xDb34C218858d520100850eCf25A6e4E8a37c3aa1";

    /**
     * @param fileDirectory The root directory where the app is installed.
     * @param fileDirectory The root directory where the app is installed.
     */
    public Wallet(File fileDirectory, AssetManager assetManager) {
        try {
            context = new Context();

            NodeConfig conf = new NodeConfig();

            if(USE_TESTNET){
                NETWORK_ID = 4;
                //Read the genesis file to a string
                InputStream is = assetManager.open("rinkeby.json");
                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();
                String genesis = new String(buffer, "UTF-8");

                conf.setEthereumEnabled(true);
                conf.setEthereumGenesis(genesis);
                conf.setEthereumNetworkID(NETWORK_ID);//
                conf.setMaxPeers(25);
                proteusionPath = fileDirectory + TESTNET_PATH;
            }
            else {
                proteusionPath = fileDirectory + MAINNET_PATH;

            }

            node = Geth.newNode(proteusionPath, conf);
            //node.start();
            ethereumClient = node.getEthereumClient();

            //Creating new private/public keypair: ONLY if there is no account yet,
            accountManager = new KeyStore(proteusionPath + KEYSTORE_PATH, Geth.LightScryptN, Geth.LightScryptP);
            //createAccount("12345");
            //sendETH(Long.parseLong("1000000000000"), "12345");

            prd = new PRDToken(ethereumClient);
            kyc = new WrappyKYC(ethereumClient);

            try{
                coinInfo = new JSONObject("{ \"id\": \"ethereum\", \"name\": \"Ethereum\", \"symbol\": \"ETH\", \"rank\": \"2\", \"price_usd\": \"0\", \"price_btc\": \"0\", \"24h_volume_usd\": \"0\", \"market_cap_usd\": \"0\", \"available_supply\": \"0\", \"total_supply\": \"0\", \"percent_change_1h\": \"0\", \"percent_change_24h\": \"0\", \"percent_change_7d\": \"0\", \"last_updated\": \"0\"}");
            }catch (Exception e){}

        } catch (Exception e) {
             e.printStackTrace();
        }
    }

    public static  Wallet getInstance(File fileDirectory, AssetManager assetManager) {
        if (wallet == null) {
            wallet = new Wallet(fileDirectory, assetManager);
        }

        return wallet;
    }

    public long getPeers(){
        return node.getPeersInfo().size();
    }

    public  String getHexAddress(int index)
    {
        String address = "";
        try {
            address =  accountManager.getAccounts().get(index).getAddress().getHex();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return address;
    }

    public Double getBalance(int index){
        try {
            BigDecimal balanceForAccount = new BigDecimal(ethereumClient.getBalanceAt(context, accountManager.getAccounts().get(index).getAddress(), -1).toString());
            balanceForAccount = balanceForAccount.divide(new BigDecimal("1000000000000000000"));
            return Double.parseDouble(decimalFormatter.format(balanceForAccount));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.00;
    }

    public Double getBalanceAtBlock(int accIndex, long blocknumber){
        try {
            String number = ethereumClient.getBalanceAt(context, accountManager.getAccounts().get(accIndex).getAddress(), blocknumber).toString();
            BigDecimal balanceForAccount = new BigDecimal(number);
            balanceForAccount = balanceForAccount.divide(new BigDecimal("1000000000000000000"));

            return Double.parseDouble(decimalFormatter.format(balanceForAccount));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.00;
    }

    public Double getEthChange(){
        try {
            return Double.parseDouble(coinInfo.getString("percent_change_24h"));
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return 0.00;
    }

    public Double getEthPrice(int index){
        try {
            String ethRate = coinInfo.getString("price_usd");
            return getBalance(index) * Double.parseDouble(ethRate);
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return 0.00;

    }

    public Double convertUSDToETH(Double usd){
        try {
            String ethRate = coinInfo.getString("price_usd");
            return usd / Double.parseDouble(ethRate);
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return 0.00;

    }

    public Double convertETHToUSD(Double eth){
        try {
            String ethRate = coinInfo.getString("price_usd");
            return eth * Double.parseDouble(ethRate);
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return 0.00;

    }

    public Double convertPROToUSD(Double pro){
        try {
           // String ethRate = coinInfo.getString("price_usd");
            return pro * prdRate;
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return 0.00;

    }

    public Double getPRDBalance(int index){
        try {
            return Double.parseDouble(prd.balanceOf(Geth.newCallOpts(), accountManager.getAccounts().get(index).getAddress()).toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.00;
    }

    public String sendPRD(String address , long value , final String passphase ){
        String status = "";
        Transaction transaction ;
        try {

            mySigner = new Signer() {
                @Override
                public Transaction sign(Address address, Transaction transaction) throws Exception {
                    return transaction.withSignature(accountManager.signHashPassphrase(accountManager.getAccounts().get(0), passphase , transaction.getSigHash().getBytes()), new BigInt(NETWORK_ID));
                }
            };

            TransactOpts auth  = new TransactOpts();
            auth.setFrom( accountManager.getAccounts().get(0).getAddress());
            auth.setContext(Geth.newContext());
            auth.setValue(new BigInt(0));
            auth.setGasLimit(GAS_PRICE_PRO);
            auth.setSigner(mySigner);
            auth.setGasPrice(ethereumClient.suggestGasPrice(context));
            auth.setNonce(getNonce());

            transaction = prd.transfer(auth, new Address(address),new BigInt(value));
            status = "Submitted transaction successfully";
        } catch (Exception e) {
            e.printStackTrace();
            status = e.getMessage();
        }

        return status;
    }


    public Double getPRDChange(){

        return 14.25;
    }

    public Double getPRDPrice(int index){

        return getPRDBalance(index) * prdRate;
    }

    public Long getLastBlock(){
        try {
            return ethereumClient.getBlockByNumber(context, -1).getNumber();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Long(0);
    }

    public Block getBlockByNumber(long num){
        try {
            return ethereumClient.getBlockByNumber(context, num);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public String convertWeiToEther(BigInt wei){
        try {
            BigDecimal balanceForAccount = new BigDecimal(wei.toString());
            balanceForAccount = balanceForAccount.divide(new BigDecimal("1000000000000000000"));
            String[] arr = balanceForAccount.toString().split("");
            int position = 0;
            for (int i = (arr.length-1); i > 0; i--) {
                if (!arr[i].equalsIgnoreCase("0")) {
                    position = i;
                    break;
                }
            }
            return balanceForAccount.toString().substring(0,position);
        }catch (Exception ex) {
            return "";
        }

        //return decimalFormatter.format(balanceForAccount);
    }
    public String convertWeiToEther(String wei) {
        try {
            BigDecimal balanceForAccount = new BigDecimal(wei);
            balanceForAccount = balanceForAccount.divide(new BigDecimal("1000000000000000000"));
            String[] arr = balanceForAccount.toString().split("");
            int position = 0;
            for (int i = (arr.length-1); i > 0; i--) {
                if (!arr[i].equalsIgnoreCase("0")) {
                    position = i;
                    break;
                }
            }
            return balanceForAccount.toString().substring(0,position);
        }catch (Exception ex) {
            return "";
        }

        //return decimalFormatter.format(balanceForAccount);
    }

    public String convertWeiToUSD(BigInt wei, Double rate){
        BigDecimal balance = new BigDecimal(wei.toString());
        balance = balance.divide(new BigDecimal("1000000000000000000"));
        return decimalFormatter.format(balance.multiply(new BigDecimal(rate)));
    }

    private static String removeLastChar(String str) {
        return str.substring(0, str.length() - 1);
    }

    public BigInt convertEtherToWei(float eth){
        String t = "1000000000000000000";
        while(eth < 1)
        {
            eth = eth *10;
            t = removeLastChar(t);
        }

        BigInt amount = new BigInt(Long.parseLong(String.valueOf(Math.round(eth))) * Long.parseLong(t));
        return amount;
    }

    public void setCoinInfo(){
        try {
            JSONParser jsonParser = new JSONParser();
            HashMap<String, String> args = new HashMap<>();
            coinInfo = jsonParser.makeHttpRequest("https://api.coinmarketcap.com/v1/ticker/ethereum/", "GET", args);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void setCoinInfoByUrl(JsonObject object) {
        for (Map.Entry<String,JsonElement> entry : object.entrySet()) {
            try {
                coinInfo.put(entry.getKey(),entry.getValue().getAsString());
            }catch (Exception ex) {}
        }
    }

    public byte[] exportAccount(String passphrase, String newPassphrase){
        byte[] jsonAcc = null;
        try {
            jsonAcc = accountManager.exportKey(accountManager.getAccounts().get(0), passphrase, newPassphrase);
        } catch(Exception e){
            e.printStackTrace();
        }
        return jsonAcc;
    }

    public void DeleteAccount(Account account, String passphrase){

        try {
            accountManager.deleteAccount(account, passphrase);
        } catch(Exception e){
            e.printStackTrace();
        }

    }

    public void importAccount(String passphrase, String newPassphrase, String  jsonAcc){
        try {
            accountManager.importKey(jsonAcc.getBytes(), passphrase, newPassphrase);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    private void writeToFile(String data ,String path) {
        try {
            FileOutputStream fos = new FileOutputStream(path + "/UTC-Main");
            fos.write(data.getBytes());
            fos.close();
            fos.flush();
        } catch (Exception e) {

        }
    }

    public void CopyKeyStore( String data){
       writeToFile(data ,proteusionPath + KEYSTORE_PATH);
    }

    public void createAccount(String passphrase){
        try {
            if (accountManager.getAccounts().size() < 1) {
                accountManager.newAccount(passphrase);
            }

        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public long getNonce(){
        long nonce = -1;
        try {
            nonce =  ethereumClient.getPendingNonceAt(context,accountManager.getAccounts().get(0).getAddress());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return nonce;
    }


    public String sendETH(BigInt amount, final String passphrase , long nonce , String address, String des){
        String status = "";
        try {
            //long gasPrice = Long.parseLong("327000000000");
            byte[] theByteArray = des.getBytes();
            //nonce uint64, to *common.Address, amount, gasLimit, gasPrice *big.Int, data []byte)
            Transaction tx = new Transaction(
                    nonce,
                    new Address(address),
                    amount,
                    new BigInt(GAS_PRICE),
                    ethereumClient.suggestGasPrice(context),
                    theByteArray); // Random empty transaction
            BigInt chain = new BigInt(NETWORK_ID); // Chain identifier of the main net

            // Sign a transaction with a single authorization
           Transaction signed = accountManager.signTxPassphrase(accountManager.getAccounts().get(0), passphrase, tx, chain);


            ethereumClient.sendTransaction(context, signed);
            status =  "Submitted transaction successfully " ;
        } catch(Exception e){
            e.printStackTrace();
            status = e.getMessage();
        }
        return status;
    }

    public Address getAddressByPhone(String number){
        try{
            return kyc.getAddressByPhone(Geth.newCallOpts(), new BigInt(Long.parseLong(number)));
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public String CheckgetAddressByPhone(String number){
        try{
            return kyc.getAddressByPhone(Geth.newCallOpts(), new BigInt(Long.parseLong(number))).getHex();
        }catch(Exception e){
            e.printStackTrace();
            return e.getMessage();
        }
    }

    public String getPhoneByAddress(String address){
        try{
          //  return kyc.getPhoneByAddress(Geth.newCallOpts(), accountManager.getAccounts().get(0).getAddress()).toString();
            return kyc.getPhoneByAddress(Geth.newCallOpts(), new Address(address)).toString();
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isNewWallet(File fileDirectory){
        try {
            File file = new File(getKeystorePath(fileDirectory));
            if(file.isDirectory()) {
                if (file.list().length > 0) {
                    return false;

                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return true;
    }
    public static String getKeystorePath(File fileDirectory){
        if(USE_TESTNET){
            return fileDirectory + TESTNET_PATH + KEYSTORE_PATH;
        }

        return fileDirectory + MAINNET_PATH + KEYSTORE_PATH;
    }

    public boolean isWrappyUser(String phone){
        if(getAddressByPhone(phone) != null && wallet!=null) {
            if (getAddressByPhone(phone).getHex().isEmpty() || (wallet.getAddressByPhone(phone).getHex().length() == 42 && !wallet.getAddressByPhone(phone).getHex().equals(wallet.getHexAddress(0))))
                return false;
        }
        return true;
    }

    /*public boolean deployKYC(Address addr, String phone){

        if(!isWrappyUser(phone)){
            try {
                mySigner = new Signer() {
                    @Override
                    public Transaction sign(Address address, Transaction transaction) throws Exception {
                        return accountManager.signTxPrivateKey(wrappyPrivateKey,transaction,new BigInt(NETWORK_ID));


                    }
                };

                TransactOpts auth = new TransactOpts();
                auth.setFrom(new Address(companyAddress));
                auth.setContext(Geth.newContext());
                auth.setValue(new BigInt(0));
                auth.setGasLimit(GAS_PRICE_WITH_TEXT);
                auth.setSigner(mySigner);
                auth.setGasPrice(ethereumClient.suggestGasPrice(context));
                auth.setNonce(ethereumClient.getPendingNonceAt(context,new Address(companyAddress)));

                kyc.newUserByWrappy(auth, addr, new BigInt(Long.parseLong(phone)));
            }
            catch (Exception e)
            {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }*/

    public boolean isSyncing() throws Exception {
        return ethereumClient.syncProgress(this.context) != null;
    }

    public boolean isSynced() throws Exception {
        SyncProgress progress = ethereumClient.syncProgress(this.context);
        return progress.getCurrentBlock() >= progress.getHighestBlock();
    }

}
