package net.wrappy.im.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ben on 30/11/2017.
 */

public class WpKAuthDto implements Parcelable {

    private String secret;
    private String account;
    private String method;
    private Long member;
    private Long id;

    public WpKAuthDto(String secret) {
        this.secret = secret;
    }

    public WpKAuthDto() {}

    protected WpKAuthDto(Parcel in) {
        secret = in.readString();
    }

    public static final Creator<WpKAuthDto> CREATOR = new Creator<WpKAuthDto>() {
        @Override
        public WpKAuthDto createFromParcel(Parcel in) {
            return new WpKAuthDto(in);
        }

        @Override
        public WpKAuthDto[] newArray(int size) {
            return new WpKAuthDto[size];
        }
    };

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public Long getMember() {
        return member;
    }

    public void setMember(Long member) {
        this.member = member;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(secret);
    }
}
