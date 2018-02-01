package net.wrappy.im.model;

/**
 * Created by ben on 01/02/2018.
 */

public class WpKMemberRoleDtoList {
    private long id;
    private String member;
    private long identifier;
    private String identity;
    private long active;

    public WpKMemberRoleDtoList() {}

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMember() {
        return member;
    }

    public void setMember(String member) {
        this.member = member;
    }

    public long getIdentifier() {
        return identifier;
    }

    public void setIdentifier(long identifier) {
        this.identifier = identifier;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public long getActive() {
        return active;
    }

    public void setActive(long active) {
        this.active = active;
    }
}
