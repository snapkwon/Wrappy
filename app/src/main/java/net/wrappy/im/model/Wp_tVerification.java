package net.wrappy.im.model;

/**
 * Created by ben on 02/01/2018.
 */

public class Wp_tVerification {
    int member;
    String code;
    String channel;
    String type;
    int ttl;
    int verified;
    String made;
    String revised;
    String purged;
    String device;

    public Wp_tVerification() {}

    public int getMember() {
        return member;
    }

    public void setMember(int member) {
        this.member = member;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getTtl() {
        return ttl;
    }

    public void setTtl(int ttl) {
        this.ttl = ttl;
    }

    public int getVerified() {
        return verified;
    }

    public void setVerified(int verified) {
        this.verified = verified;
    }

    public String getMade() {
        return made;
    }

    public void setMade(String made) {
        this.made = made;
    }

    public String getRevised() {
        return revised;
    }

    public void setRevised(String revised) {
        this.revised = revised;
    }

    public String getPurged() {
        return purged;
    }

    public void setPurged(String purged) {
        this.purged = purged;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }
}
