package net.wrappy.im.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ben on 30/11/2017.
 */

public class WpKAuthDto implements Parcelable {

    private String secret;

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(secret);
    }
}
