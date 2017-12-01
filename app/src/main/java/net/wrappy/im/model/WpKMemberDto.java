package net.wrappy.im.model;

/**
 * Created by ben on 30/11/2017.
 */

public class WpKMemberDto {
    private String identifier;
    private String email;
    private String mobile;

    public WpKMemberDto(String identifier, String email, String mobile) {
        this.identifier = identifier;
        this.email = email;
        this.mobile = mobile;
    }

    public WpKMemberDto() {}

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }
}
