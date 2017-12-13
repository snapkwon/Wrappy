package net.wrappy.im.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by ben on 13/12/2017.
 */

public class WpkRoster implements Parcelable{
    Integer id;
    String name;
    String type;
    String reference;
    ArrayList<String> listUsername;

    public WpkRoster() {}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public ArrayList<String> getListUsername() {
        return listUsername;
    }

    public void setListUsername(ArrayList<String> listUsername) {
        this.listUsername = listUsername;
    }

    protected WpkRoster(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readInt();
        }
        name = in.readString();
        type = in.readString();
        reference = in.readString();
        listUsername = in.createStringArrayList();
    }

    public static final Creator<WpkRoster> CREATOR = new Creator<WpkRoster>() {
        @Override
        public WpkRoster createFromParcel(Parcel in) {
            return new WpkRoster(in);
        }

        @Override
        public WpkRoster[] newArray(int size) {
            return new WpkRoster[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        if (id == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeInt(id);
        }
        parcel.writeString(name);
        parcel.writeString(type);
        parcel.writeString(reference);
        parcel.writeStringList(listUsername);
    }
}
