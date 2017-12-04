package net.wrappy.im.GethService.db;

/**
 * Created by sonntht on 25/10/2017.
 */

public class Balance {
    public static final String TAG = Balance.class.getSimpleName();
    public static final String TABLE = "Balance";

    // Labels Table Columns names
    public static final String KEY_address = "address";
    public static final String KEY_tokenAddress = "tokenAddress";
    public static final String KEY_chain = "chain";
    public static final String KEY_block = "block";
    public static final String KEY_balance = "balance";
    public static final String KEY_tokenBalance = "tokenBalance";

    private String address;
    private String tokenAddress;
    private String chain;
    private long block;
    private String balance;
    private String tokenBalance;

    public void setAddress(String address) {
        this.address = address;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public void setBlock(long block) {
        this.block = block;
    }

    public void setChain(String chain) {
        this.chain = chain;
    }

    public void setTokenAddress(String tokenAddress) {
        this.tokenAddress = tokenAddress;
    }

    public void setTokenBalance(String tokenBalance) {
        this.tokenBalance = tokenBalance;
    }

    public String getBalance() {
        if(balance == null || balance == ""){
            balance = "0";
        }
        return balance;
    }

    public long getBlock() {
        return block;
    }

    public String getTokenBalance() {
        if(tokenBalance == null || tokenBalance == ""){
            tokenBalance = "0";
        }
        return tokenBalance;
    }

    public String getAddress() {
        return address;
    }

    public String getChain() {
        return chain;
    }

    public String getTokenAddress() {
        return tokenAddress;
    }
}