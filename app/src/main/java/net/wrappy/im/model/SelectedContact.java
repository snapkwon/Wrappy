package net.wrappy.im.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ben on 12/12/2017.
 */

public class SelectedContact implements Parcelable {
    public long id;
    public String username;
    public String nickname;
    public Integer account;
    public Integer provider;

    public SelectedContact(long id, String username, int account, int provider,String nickname) {
        this.id = id;
        this.username = username;
        this.account = account;
        this.provider = provider;
        this.nickname = nickname;
    }

    protected SelectedContact(Parcel in) {
        id = in.readLong();
        username = in.readString();
        if (in.readByte() == 0) {
            account = null;
        } else {
            account = in.readInt();
        }
        if (in.readByte() == 0) {
            provider = null;
        } else {
            provider = in.readInt();
        }
    }

    public static final Creator<SelectedContact> CREATOR = new Creator<SelectedContact>() {
        @Override
        public SelectedContact createFromParcel(Parcel in) {
            return new SelectedContact(in);
        }

        @Override
        public SelectedContact[] newArray(int size) {
            return new SelectedContact[size];
        }
    };

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNickName() {
        return nickname;
    }

    public void setNickname(long id) {
        this.nickname = nickname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getAccount() {
        return account;
    }

    public void setAccount(Integer account) {
        this.account = account;
    }

    public Integer getProvider() {
        return provider;
    }

    public void setProvider(Integer provider) {
        this.provider = provider;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(id);
        parcel.writeString(username);
        if (account == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeInt(account);
        }
        if (provider == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeInt(provider);
        }
    }
}
