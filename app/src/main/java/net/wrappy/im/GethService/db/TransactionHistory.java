package net.wrappy.im.GethService.db;

/**
 * Created by sonntht on 30/10/2017.
 */

public class TransactionHistory {
    public static final String TAG = Balance.class.getSimpleName();
    public static final String TABLE = "TransactionHistory";

    public static final String KEY_address = "address";
    public static final String KEY_tokenAddress = "tokenAddress";
    public static final String KEY_chain = "chain";
    public static final String KEY_block = "block";
    public static final String KEY_isToken = "isToken"; //0: eth, 1: token
    public static final String KEY_type = "type";  //0: sent, 1: received
    public static final String KEY_secondAddress = "secondAddress";
    public static final String KEY_balance = "balance";
    public static final String KEY_time = "time";

    private String address;
    private String tokenAddress;
    private String chain;
    private long block;
    private int isToken;
    private int type;
    private String secondAddress;
    private String balance;
    private String time;

    public void setAddress(String address) {
        this.address = address;
    }

    public void setTokenAddress(String tokenAddress) {
        this.tokenAddress = tokenAddress;
    }

    public void setBlock(long block) {
        this.block = block;
    }

    public void setChain(String chain) {
        this.chain = chain;
    }

    public void setIsToken(int isToken) {
        this.isToken = isToken;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setSecondAddress(String secondAddress) {
        this.secondAddress = secondAddress;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getAddress() {
        return address;
    }

    public String getTokenAddress() {
        return tokenAddress;
    }

    public String getChain() {
        return chain;
    }

    public long getBlock() {
        return block;
    }

    public int getIsToken() {
        return isToken;
    }

    public int getType() {
        return type;
    }

    public String getSecondAddress() {
        return secondAddress;
    }

    public String getBalance() {
        if(balance == null || balance == ""){
            balance = "0";
        }
        return balance;
    }

    public String getTime() {
        return time;
    }
}
