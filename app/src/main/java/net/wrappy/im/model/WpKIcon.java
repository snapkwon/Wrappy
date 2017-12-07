package net.wrappy.im.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ben on 06/12/2017.
 */

public class WpKIcon implements Parcelable {
    String reference;

    public WpKIcon() {}

    protected WpKIcon(Parcel in) {
        reference = in.readString();
    }

    public static final Creator<WpKIcon> CREATOR = new Creator<WpKIcon>() {
        @Override
        public WpKIcon createFromParcel(Parcel in) {
            return new WpKIcon(in);
        }

        @Override
        public WpKIcon[] newArray(int size) {
            return new WpKIcon[size];
        }
    };

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(reference);
    }
}
