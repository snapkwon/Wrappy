package net.wrappy.im.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by ben on 06/12/2017.
 */

public class WpKChatGroup extends T implements Parcelable {

    ArrayList<String> memberIds;
    WpKChatGroupDto wpKChatGroupDto;

    public WpKChatGroup(ArrayList<String> memberIds, WpKChatGroupDto wpKChatGroupDto) {
        this.memberIds = memberIds;
        this.wpKChatGroupDto = wpKChatGroupDto;
    }

    public WpKChatGroup() {}

    protected WpKChatGroup(Parcel in) {
        memberIds = in.createStringArrayList();
        wpKChatGroupDto = in.readParcelable(WpKChatGroupDto.class.getClassLoader());
    }

    public static final Creator<WpKChatGroup> CREATOR = new Creator<WpKChatGroup>() {
        @Override
        public WpKChatGroup createFromParcel(Parcel in) {
            return new WpKChatGroup(in);
        }

        @Override
        public WpKChatGroup[] newArray(int size) {
            return new WpKChatGroup[size];
        }
    };

    public ArrayList<String> getMemberIds() {
        return memberIds;
    }

    public void setMemberIds(ArrayList<String> memberIds) {
        this.memberIds = memberIds;
    }

    public WpKChatGroupDto getWpKChatGroupDto() {
        return wpKChatGroupDto;
    }

    public void setWpKChatGroupDto(WpKChatGroupDto wpKChatGroupDto) {
        this.wpKChatGroupDto = wpKChatGroupDto;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeStringList(memberIds);
        parcel.writeParcelable(wpKChatGroupDto, i);
    }
}
