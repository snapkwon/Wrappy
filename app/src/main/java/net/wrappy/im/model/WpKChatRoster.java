package net.wrappy.im.model;

/**
 * Created by khoa.nguyen on 06/12/2017.
 */

public class WpKChatRoster {
    Integer id;
    Integer identifier;
    String member;
    String reference;
    WpKMemberDto contact;

    public WpKMemberDto getContact() {
        return contact;
    }
}
