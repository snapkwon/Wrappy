package net.wrappy.im.model;

import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Created by ben on 30/11/2017.
 */

public class WpKMemberDto extends T {
    private Long id;
    private String identifier;
    private String email;
    private String mobile;
    private String gender;
    private Avatar avatar;
    private Banner banner;
    private String given;
    private String reference;
    @SerializedName("wpKAuthList")
    private ArrayList<WpKAuthDto> wpKAuthDtoList;

    public WpKMemberDto() {
    }

    public static Type getType() {
        return new TypeToken<WpKMemberDto>(){}.getType();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender.toUpperCase();
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

    public Avatar getAvatar() {
        return avatar;
    }

    public void setAvatar(Avatar avatar) {
        this.avatar = avatar;
    }

    public Banner getBanner() {
        return banner;
    }

    public void setBanner(Banner banner) {
        this.banner = banner;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public WpKAuthDto getXMPPAuthDto() {
        for (WpKAuthDto wpKAuthDto : getWpKAuthDtoList())
            if ("XMPP".equals(wpKAuthDto.getMethod()))
                return wpKAuthDto;
        return null;
    }

    public String getGiven() {
        return given;
    }

    public void setGiven(String given) {
        this.given = given;
    }
}
