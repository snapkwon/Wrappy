/*
 * Copyright (C) 2007 Esmertec AG. Copyright (C) 2007 The Android Open Source
 * Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package net.wrappy.im.model;

import android.os.Parcel;
import android.os.Parcelable;

import net.wrappy.im.plugin.xmpp.XmppAddress;

public class RegistrationAccount implements Parcelable {
    private String username;
    private String jid;
    private String nickname;
    private String domain;
    private String password;
    private String server;
    private int port;
    private String email;
    private String phone;
    private String gender;

    public RegistrationAccount(String username, String password) {
        this.username = username;
        this.password = password;
        XmppAddress address = new XmppAddress(getUsername());
        this.jid = address.getUser();
        this.domain = address.getAddress().split("@")[1];
    }

    protected RegistrationAccount(Parcel in) {
        username = in.readString();
        jid = in.readString();
        nickname = in.readString();
        domain = in.readString();
        password = in.readString();
        server = in.readString();
        port = in.readInt();
        email = in.readString();
        phone = in.readString();
        gender = in.readString();
    }

    public static final Creator<RegistrationAccount> CREATOR = new Creator<RegistrationAccount>() {
        @Override
        public RegistrationAccount createFromParcel(Parcel in) {
            return new RegistrationAccount(in);
        }

        @Override
        public RegistrationAccount[] newArray(int size) {
            return new RegistrationAccount[size];
        }
    };

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getJid() {
        return jid;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(username);
        parcel.writeString(jid);
        parcel.writeString(nickname);
        parcel.writeString(domain);
        parcel.writeString(password);
        parcel.writeString(server);
        parcel.writeInt(port);
        parcel.writeString(email);
        parcel.writeString(phone);
        parcel.writeString(gender);
    }
}
