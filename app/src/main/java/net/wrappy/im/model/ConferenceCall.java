package net.wrappy.im.model;
/*
* Created by Khoa.Nguyen
* */

import android.net.Uri;

public class ConferenceCall {
    private String bareAddress;
    private String nickname;
    private String body;
    private Uri messageUri;
    private Uri chaturi;
    private boolean isGroup;

    public ConferenceCall(String bareAddress, String nickname, String body, Uri messageUri, Uri chaturi) {
        this.bareAddress = bareAddress;
        this.nickname = nickname;
        this.body = body;
        this.messageUri = messageUri;
        this.chaturi = chaturi;
    }

    public boolean isGroup() {
        return isGroup;
    }

    public void setGroup(boolean group) {
        isGroup = group;
    }

    public String getBareAddress() {
        return bareAddress;
    }

    public String getNickname() {
        return nickname;
    }

    public String getBody() {
        return body;
    }

    public Uri getMessageUri() {
        return messageUri;
    }

    public Uri getChaturi() {
        return chaturi;
    }
}
