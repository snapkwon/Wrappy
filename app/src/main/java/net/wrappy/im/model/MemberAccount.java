package net.wrappy.im.model;

import java.util.ArrayList;

/**
 * Created by ben on 01/02/2018.
 */

public class MemberAccount extends T {
    private ArrayList<WpKMemberRoleDtoList> wpKMemberRoleDtoList;
    private WpKMemberDto wpKMemberDto;
    private WpKMemberDto wpKMemberInviter;

    public MemberAccount() {}

    public ArrayList<WpKMemberRoleDtoList> getWpKMemberRoleDtoList() {
        return wpKMemberRoleDtoList;
    }

    public void setWpKMemberRoleDtoList(ArrayList<WpKMemberRoleDtoList> wpKMemberRoleDtoList) {
        this.wpKMemberRoleDtoList = wpKMemberRoleDtoList;
    }

    public WpKMemberDto getWpKMemberDto() {
        return wpKMemberDto;
    }

    public void setWpKMemberDto(WpKMemberDto wpKMemberDto) {
        this.wpKMemberDto = wpKMemberDto;
    }

    public WpKMemberDto getWpKMemberInviter() {
        return wpKMemberInviter;
    }

    public void setWpKMemberInviter(WpKMemberDto wpKMemberInviter) {
        this.wpKMemberInviter = wpKMemberInviter;
    }
}
