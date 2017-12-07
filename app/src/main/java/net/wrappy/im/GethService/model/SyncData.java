package net.wrappy.im.GethService.model;

/**
 * Created by sonntht on 25/10/2017.
 */

public class SyncData {
    long peers;
    long currentBlock;
    long highestBlock;

    public SyncData(){
        peers = 0;
        currentBlock = 0;
        highestBlock = 0;
    }

    public long getCurrentBlock() {
        return currentBlock;
    }

    public long getHighestBlock() {
        return highestBlock;
    }

    public long getPeers() {
        return peers;
    }

    public void setCurrentBlock(long currentBlock) {
        this.currentBlock = currentBlock;
    }

    public void setHighestBlock(long highestBlock) {
        this.highestBlock = highestBlock;
    }

    public void setPeers(long peers) {
        this.peers = peers;
    }
}
