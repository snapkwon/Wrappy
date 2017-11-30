package net.wrappy.im.GethService;

import net.wrappy.im.GethService.utils.JSONParser;

import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by sonntht on 24/10/2017.
 */

public class WalletInfo {
    private static final DecimalFormat decimalFormatter = new DecimalFormat("0.00##", new DecimalFormatSymbols(Locale.US));
    public long currentBlock;
    public long highestBlock;
    public long peerCount;
    public String ETHAddress;

    public String ETHBalance;
    public String PROBalance;
    private Double ETHRate;
    private Double PRORate;

    public JSONObject coinInfo;
    public static final String GETH_PATH = "/.ethereum/rinkeby/GethDroid";
    public static final String KEYSTORE_PATH = "/.ethereum/rinkeby/keystore";

    public WalletInfo(){
        ETHAddress = "";
        currentBlock = 0;
        highestBlock = 0;
        peerCount = 0;

        ETHRate = 0.0;
        PRORate = 0.30;
    }

    public void updateCoinInfo(){
        try {
            coinInfo = new JSONObject("{ \"id\": \"ethereum\", \"name\": \"Ethereum\", \"symbol\": \"ETH\", \"rank\": \"2\", \"price_usd\": \"0\", \"price_btc\": \"0\", \"24h_volume_usd\": \"0\", \"market_cap_usd\": \"0\", \"available_supply\": \"0\", \"total_supply\": \"0\", \"percent_change_1h\": \"0\", \"percent_change_24h\": \"0\", \"percent_change_7d\": \"0\", \"last_updated\": \"0\"}");
            JSONParser jsonParser = new JSONParser();
            HashMap<String, String> args = new HashMap<>();
            coinInfo = jsonParser.makeHttpRequest("https://api.coinmarketcap.com/v1/ticker/ethereum/", "GET", args);
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    public void setCurrentBlock(long number){
        this.currentBlock = number;
    }
    public long getCurrentBlock(){
        return this.currentBlock;
    }

    public void setHighestBlock(long number){
        this.highestBlock = number;
    }
    public long getHighestBlock(){
        return this.highestBlock;
    }

    public void setETHBalance(String number){
        this.ETHBalance = number;
    }
    public Double getETHBalance(){
        try {
            BigDecimal balanceForAccount = new BigDecimal(this.ETHBalance);
            balanceForAccount = balanceForAccount.divide(new BigDecimal("1000000000000000000"));
            return Double.parseDouble(decimalFormatter.format(balanceForAccount));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.00;
    }

    public void setPROBalance(String number){
        this.PROBalance = number;
    }
    public Double getPROBalance(){
        try {
            return Double.parseDouble(String.valueOf(PROBalance));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.00;
    }

    public void setETHAddress(String address){
        this.ETHAddress = address;
    }
    public String getETHAddress(){
        return this.ETHAddress;
    }

    public void setETHRate(Double ETHRate) {
        this.ETHRate = ETHRate;
    }

    public Double getETHRate() {
        return ETHRate;
    }

    public Double getPRORate() {
        return PRORate;
    }

    public void setPRORate(Double PRORate) {
        this.PRORate = PRORate;
    }

    public Double getETHPrice() {
        try {
            String ethRate = coinInfo.getString("price_usd");
            return getETHBalance() * Double.parseDouble(ethRate);
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return 0.00;
    }

    public Double getETHChange(){
        try {
            return Double.parseDouble(coinInfo.getString("percent_change_24h"));
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return 0.00;
    }

    public Double getPROChange(){

        return 14.25;
    }

    public Double getPROPrice(){

        return getPROBalance() * PRORate;
    }

    public static String convertWeiToEther(String wei) {
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
    }

    public long getPeerCount() {
        return peerCount;
    }

    public void setPeerCount(long peerCount) {
        this.peerCount = peerCount;
    }
}
