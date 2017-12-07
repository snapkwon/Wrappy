package net.wrappy.im.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by ben on 30/11/2017.
 */

public class WpKMemberDto {
    private Long id;
    private String identifier;
    private String email;
    private String mobile;
    private String avatar;
    @SerializedName("wpKAuthList")
    private ArrayList<WpKAuthDto> wpKAuthDtoList;

    public WpKMemberDto(String identifier, String email, String mobile) {
        this.identifier = identifier;
        this.email = email;
        this.mobile = mobile;
    }

    public WpKMemberDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public ArrayList<WpKAuthDto> getWpKAuthDtoList() {
        return wpKAuthDtoList != null ? wpKAuthDtoList : new ArrayList<WpKAuthDto>();
    }

    public void setWpKAuthDtoList(ArrayList<WpKAuthDto> wpKAuthDtoList) {
        this.wpKAuthDtoList = wpKAuthDtoList;
    }

    public WpKAuthDto getXMPPAuthDto() {
        for (WpKAuthDto wpKAuthDto : getWpKAuthDtoList())
            if ("XMPP".equals(wpKAuthDto.getMethod()))
                return wpKAuthDto;
        return null;
    }
}
