package net.wrappy.im.GethService.model;

import com.google.gson.JsonObject;

/**
 * Created by PCPV on 07/28/2017.
 */

public class TransactionJsonModel {
    public long blockNumber;
    public long timeStamp;
    public String hash;
    public long nonce;
    public String blockHash;
    public long transactionIndex;
    public String from;
    public String to;
    public String value;
    public long gas;
    public long gasPrice;
    public long isError;
    public String input;
    public String contractAddress;
    public long cumulativeGasUsed;
    public long gasUsed;
    public long confirmations;

    public TransactionJsonModel() {}

    public void convertJson(JsonObject json){
        try { blockNumber=json.get("blockNumber").getAsLong(); }catch (Exception ex){}
        try { timeStamp=json.get("timeStamp").getAsLong(); }catch (Exception ex){}
        try { hash=json.get("hash").getAsString(); }catch (Exception ex){}
        try { nonce=json.get("nonce").getAsLong(); }catch (Exception ex){}
        try { blockHash=json.get("blockHash").getAsString(); }catch (Exception ex){}
        try { transactionIndex=json.get("transactionIndex").getAsLong(); }catch (Exception ex){}
        try { from=json.get("from").getAsString(); }catch (Exception ex){}
        try { to=json.get("to").getAsString(); }catch (Exception ex){}
        try { value=json.get("value").getAsString(); }catch (Exception ex){}
        try { gas=json.get("gas").getAsLong(); }catch (Exception ex){}
        try { gasPrice=json.get("gasPrice").getAsLong(); }catch (Exception ex){}
        try { isError=json.get("isError").getAsLong(); }catch (Exception ex){}
        try { input=json.get("input").getAsString(); }catch (Exception ex){}
        try { contractAddress=json.get("contractAddress").getAsString(); }catch (Exception ex){}
        try { cumulativeGasUsed=json.get("cumulativeGasUsed").getAsLong(); }catch (Exception ex){}
        try { gasUsed=json.get("gasUsed").getAsLong(); }catch (Exception ex){}
        try { confirmations=json.get("confirmations").getAsLong(); }catch (Exception ex){}
    }
}
