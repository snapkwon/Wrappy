package net.wrappy.im.model;

import android.os.Parcel;

/**
 * Created by ben on 20/12/2017.
 */

public class Avatar {
    String reference;

    public Avatar(String reference) {
        this.reference = reference;
    }

    protected Avatar(Parcel in) {
        reference = in.readString();
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }
}
