package net.wrappy.im.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;

/**
 * Created by ben on 10/12/2017.
 */

public class WpkCountry extends T implements Parcelable {
    int id;
    HashMap<String,String> l10N;
    String code;
    String prefix;
    int enable;

    public WpkCountry() {}

    protected WpkCountry(Parcel in) {
        id = in.readInt();
        code = in.readString();
        prefix = in.readString();
        enable = in.readInt();
    }

    public static final Creator<WpkCountry> CREATOR = new Creator<WpkCountry>() {
        @Override
        public WpkCountry createFromParcel(Parcel in) {
            return new WpkCountry(in);
        }

        @Override
        public WpkCountry[] newArray(int size) {
            return new WpkCountry[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public HashMap<String, String> getL10N() {
        return l10N;
    }

    public void setL10N(HashMap<String, String> l10N) {
        this.l10N = l10N;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public int getEnable() {
        return enable;
    }

    public void setEnable(int enable) {
        this.enable = enable;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(code);
        parcel.writeString(prefix);
        parcel.writeInt(enable);
    }
}
