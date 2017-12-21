package net.wrappy.im.model;

import android.os.Parcel;

/**
 * Created by ben on 20/12/2017.
 */

public class Banner {
    String reference;

    public Banner(String reference) {
        this.reference = reference;
    }

    protected Banner(Parcel in) {
        reference = in.readString();
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }
}
