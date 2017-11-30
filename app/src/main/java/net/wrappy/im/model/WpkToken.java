package net.wrappy.im.model;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import net.wrappy.im.provider.Store;

/**
 * Created by ben on 30/11/2017.
 */

public class WpkToken implements Parcelable {

    public static final String STORE_ACCESS_TOKEN = "access_token";
    public static final String STORE_TOKEN_TYPE = "token_type";
    public static final String STORE_REFRESH_TOKEN = "refresh_token";
    public static final String STORE_EXPIRES_IN = "expires_in";


    private String access_token;
    private String token_type;
    private String refresh_token;
    private int expires_in;
    private String scope;
    private String jid;
    private String xmppPassword;

    public WpkToken(String access_token, String token_type, String refresh_token, int expires_in, String scope, String jid, String xmppPassword) {
        this.access_token = access_token;
        this.token_type = token_type;
        this.refresh_token = refresh_token;
        this.expires_in = expires_in;
        this.scope = scope;
        this.jid = jid;
        this.xmppPassword = xmppPassword;
    }

    public WpkToken() {}

    protected WpkToken(Parcel in) {
        access_token = in.readString();
        token_type = in.readString();
        refresh_token = in.readString();
        expires_in = in.readInt();
        scope = in.readString();
        jid = in.readString();
        xmppPassword = in.readString();
    }

    public static final Creator<WpkToken> CREATOR = new Creator<WpkToken>() {
        @Override
        public WpkToken createFromParcel(Parcel in) {
            return new WpkToken(in);
        }

        @Override
        public WpkToken[] newArray(int size) {
            return new WpkToken[size];
        }
    };

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public String getToken_type() {
        return token_type;
    }

    public void setToken_type(String token_type) {
        this.token_type = token_type;
    }

    public String getRefresh_token() {
        return refresh_token;
    }

    public void setRefresh_token(String refresh_token) {
        this.refresh_token = refresh_token;
    }

    public int getExpires_in() {
        return expires_in;
    }

    public void setExpires_in(int expires_in) {
        this.expires_in = expires_in;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getJid() {
        return jid;
    }

    public void setJid(String jid) {
        this.jid = jid;
    }

    public String getXmppPassword() {
        return xmppPassword;
    }

    public void setXmppPassword(String xmppPassword) {
        this.xmppPassword = xmppPassword;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(access_token);
        parcel.writeString(token_type);
        parcel.writeString(refresh_token);
        parcel.writeInt(expires_in);
        parcel.writeString(scope);
        parcel.writeString(jid);
        parcel.writeString(xmppPassword);
    }

    public void saveToken(Context context) {
        Store.putStringData(context,STORE_ACCESS_TOKEN,getAccess_token());
        Store.putStringData(context,STORE_REFRESH_TOKEN,getRefresh_token());
        Store.putStringData(context,STORE_TOKEN_TYPE,getToken_type());
        Store.putIntData(context,STORE_EXPIRES_IN,getExpires_in());
    }
}
