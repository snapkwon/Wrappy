package net.wrappy.im.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ben on 06/12/2017.
 */

public class WpKChatGroupDto implements Parcelable {
    Integer id;
    Integer identifier;
    String name;
    String description;
    String reference;
    WpKIcon icon;

    public WpKChatGroupDto() {}

    protected WpKChatGroupDto(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readInt();
        }
        if (in.readByte() == 0) {
            identifier = null;
        } else {
            identifier = in.readInt();
        }
        name = in.readString();
        description = in.readString();
        reference = in.readString();
        icon = in.readParcelable(WpKIcon.class.getClassLoader());
    }

    public static final Creator<WpKChatGroupDto> CREATOR = new Creator<WpKChatGroupDto>() {
        @Override
        public WpKChatGroupDto createFromParcel(Parcel in) {
            return new WpKChatGroupDto(in);
        }

        @Override
        public WpKChatGroupDto[] newArray(int size) {
            return new WpKChatGroupDto[size];
        }
    };

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getIdentifier() {
        return identifier;
    }

    public void setIdentifier(Integer identifier) {
        this.identifier = identifier;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public WpKIcon getIcon() {
        return icon;
    }

    public void setIcon(WpKIcon icon) {
        this.icon = icon;
    }

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
        if (identifier == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeInt(identifier);
        }
        parcel.writeString(name);
        parcel.writeString(description);
        parcel.writeString(reference);
        parcel.writeParcelable(icon, i);
    }
}
