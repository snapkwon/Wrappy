/*
 * Copyright (C) 2007-2008 Esmertec AG. Copyright (C) 2007-2008 The Android Open
 * Source Project
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

package net.wrappy.im.ui;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.annotation.ColorInt;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.UnderlineSpan;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import net.wrappy.im.ImApp;
import net.wrappy.im.R;
import net.wrappy.im.enums.EnumPresenceStatus;
import net.wrappy.im.helper.RestAPI;
import net.wrappy.im.helper.glide.GlideHelper;
import net.wrappy.im.model.Contact;
import net.wrappy.im.model.ImErrorInfo;
import net.wrappy.im.model.Presence;
import net.wrappy.im.plugin.xmpp.XmppAddress;
import net.wrappy.im.provider.Imps;
import net.wrappy.im.service.IContactListManager;
import net.wrappy.im.service.IImConnection;
import net.wrappy.im.ui.widgets.RoundedAvatarDrawable;
import net.wrappy.im.util.LogCleaner;

public class ContactListItem extends FrameLayout {
    public static final String[] CONTACT_PROJECTION = {Imps.Contacts._ID, Imps.Contacts.PROVIDER,
            Imps.Contacts.ACCOUNT, Imps.Contacts.USERNAME,
            Imps.Contacts.NICKNAME, Imps.Contacts.TYPE,
            Imps.Contacts.SUBSCRIPTION_TYPE,
            Imps.Contacts.SUBSCRIPTION_STATUS,
            Imps.Presence.PRESENCE_STATUS,
            Imps.Presence.PRESENCE_CUSTOM_STATUS,
            Imps.Chats.LAST_MESSAGE_DATE,
            Imps.Chats.LAST_UNREAD_MESSAGE,
            Imps.Contacts.AVATAR_HASH,
            Imps.Contacts.AVATAR_DATA,
            Imps.Contacts.CONTACT_EMAIL

    };


    public static final int COLUMN_CONTACT_ID = 0;
    public static final int COLUMN_CONTACT_PROVIDER = 1;
    public static final int COLUMN_CONTACT_ACCOUNT = 2;
    public static final int COLUMN_CONTACT_USERNAME = 3;
    public static final int COLUMN_CONTACT_NICKNAME = 4;
    public static final int COLUMN_CONTACT_TYPE = 5;
    public static final int COLUMN_SUBSCRIPTION_TYPE = 6;
    public static final int COLUMN_SUBSCRIPTION_STATUS = 7;
    public static final int COLUMN_CONTACT_PRESENCE_STATUS = 8;
    public static final int COLUMN_CONTACT_CUSTOM_STATUS = 9;
    public static final int COLUMN_LAST_MESSAGE_DATE = 10;
    public static final int COLUMN_LAST_MESSAGE = 11;
    public static final int COLUMN_AVATAR_HASH = 12;
    public static final int COLUMN_AVATAR_DATA = 13;
    public static final int COLUMN_CONTACT_EMAIL = 14;

    static Drawable AVATAR_DEFAULT_GROUP = null;

    private String address;
    private String email;
    private String nickname;

    private ContactViewHolder mHolder;

    public ContactListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ContactViewHolder getViewHolder() {
        return mHolder;
    }

    public void setViewHolder(ContactViewHolder viewHolder) {
        mHolder = viewHolder;
    }

    public void bind(ContactViewHolder holder, Cursor cursor, String underLineText, boolean scrolling) {
        bind(holder, cursor, underLineText, true, scrolling);
    }

    public void bind(final ContactViewHolder holder, Cursor cursor, String underLineText, boolean showChatMsg, boolean scrolling) {

        address = cursor.getString(COLUMN_CONTACT_USERNAME);
        nickname = cursor.getString(COLUMN_CONTACT_NICKNAME);
        email = cursor.getString(COLUMN_CONTACT_EMAIL);

        final int type = cursor.getInt(COLUMN_CONTACT_TYPE);
        final String lastMsg = cursor.getString(COLUMN_LAST_MESSAGE);

        long lastMsgDate = cursor.getLong(COLUMN_LAST_MESSAGE_DATE);
        final int presence = cursor.getInt(COLUMN_CONTACT_PRESENCE_STATUS);

        final int subType = cursor.getInt(COLUMN_SUBSCRIPTION_TYPE);
        final int subStatus = cursor.getInt(COLUMN_SUBSCRIPTION_STATUS);
        final String reference = cursor.getString(COLUMN_AVATAR_HASH);

        String statusText = cursor.getString(COLUMN_CONTACT_CUSTOM_STATUS);
        String s = DatabaseUtils.dumpCursorToString(cursor);
        if (TextUtils.isEmpty(nickname)) {
            nickname = address.split("@")[0].split("\\.")[0];
        } else {
            nickname = nickname.split("@")[0].split("\\.")[0];
        }

        if (!TextUtils.isEmpty(underLineText)) {
            // highlight/underline the word being searched 
            String lowercase = nickname.toLowerCase();
            int start = lowercase.indexOf(underLineText.toLowerCase());
            if (start >= 0) {
                int end = start + underLineText.length();
                SpannableString str = new SpannableString(nickname);
                str.setSpan(new UnderlineSpan(), start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

                holder.mLine1.setText(str);

            } else
                holder.mLine1.setText(nickname);

        } else
            holder.mLine1.setText(nickname);

        //holder.mStatusIcon.setVisibility(View.GONE);

        if (holder.mAvatar != null) {
            if (Imps.Contacts.TYPE_GROUP == type) {

                holder.mAvatar.setVisibility(View.VISIBLE);

                if (AVATAR_DEFAULT_GROUP == null)
                    AVATAR_DEFAULT_GROUP = new RoundedAvatarDrawable(BitmapFactory.decodeResource(getResources(),
                            R.drawable.group_chat));


                holder.mAvatar.setImageDrawable(AVATAR_DEFAULT_GROUP);

            } else {
                GlideHelper.loadAvatarFromNickname(getContext(), holder.mAvatar, nickname);
                if (!TextUtils.isEmpty(reference)) {
                    GlideHelper.loadBitmapToCircleImage(getContext(), mHolder.mAvatar, RestAPI.getAvatarUrl(reference));
                }

            }
        }

        EnumPresenceStatus presenceStatus = EnumPresenceStatus.getStatus(presence);
        statusText = presenceStatus.getStatus();//remove address from xmpp
        @ColorInt int presentColor = getResources().getColor(presenceStatus.getColor());

        if (type == Imps.Contacts.TYPE_HIDDEN) {
            statusText += " | " + getContext().getString(R.string.action_archive);
        }

        if (holder.mLine2 != null) {
            holder.mLine2.setText(statusText);
            holder.mLine2.setTextColor(presentColor);
        }

        if (Imps.Contacts.TYPE_NORMAL == type) {

            if (subType == Imps.ContactsColumns.SUBSCRIPTION_TYPE_INVITATIONS) {
                holder.mSubBox.setVisibility(View.VISIBLE);

                holder.mButtonSubApprove.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        approveSubscription();
                        mHolder.itemView.performClick();
                    }

                });

                holder.mButtonSubDecline.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        declineSubscription();
                    }
                });

            } else if (subStatus == Imps.ContactsColumns.SUBSCRIPTION_STATUS_SUBSCRIBE_PENDING) {
                if (holder.mLine2 != null)
                    holder.mLine2.setText(getContext().getString(R.string.title_pending));
            } else if (subType == Imps.ContactsColumns.SUBSCRIPTION_TYPE_NONE || subType == Imps.ContactsColumns.SUBSCRIPTION_TYPE_REMOVE) {
                if (holder.mLine2 != null)
                    holder.mLine2.setText(getContext().getString(R.string.recipient_blocked_the_user));
            } else {
                if (holder.mSubBox != null) {
                    holder.mSubBox.setVisibility(View.GONE);
                }
            }
        }

        holder.mLine1.setVisibility(View.VISIBLE);
    }

    public void setAvatarBorder(int status, RoundedAvatarDrawable avatar) {
        switch (status) {
            case Presence.AVAILABLE:
                avatar.setBorderColor(getResources().getColor(R.color.holo_green_light));
                break;

            case Presence.IDLE:
                avatar.setBorderColor(getResources().getColor(R.color.holo_green_dark));

                break;

            case Presence.AWAY:
                avatar.setBorderColor(getResources().getColor(R.color.holo_orange_light));
                break;

            case Presence.DO_NOT_DISTURB:
                avatar.setBorderColor(getResources().getColor(R.color.holo_red_dark));

                break;

            case Presence.OFFLINE:
                avatar.setBorderColor(getResources().getColor(android.R.color.transparent));
                break;


            default:
        }
    }

    public int getAvatarBorder(int status) {
        switch (status) {
            case Presence.AVAILABLE:
                return (getResources().getColor(R.color.holo_green_light));

            case Presence.IDLE:
                return (getResources().getColor(R.color.holo_green_dark));
            case Presence.AWAY:
                return (getResources().getColor(R.color.holo_orange_light));

            case Presence.DO_NOT_DISTURB:
                return (getResources().getColor(R.color.holo_red_dark));

            case Presence.OFFLINE:
                return (getResources().getColor(R.color.holo_grey_dark));

            default:
        }

        return Color.TRANSPARENT;
    }


    /*
    private String queryGroupMembers(ContentResolver resolver, long groupId) {
        String[] projection = { Imps.GroupMembers.NICKNAME };
        Uri uri = ContentUris.withAppendedId(Imps.GroupMembers.CONTENT_URI, groupId);
        Cursor c = resolver.query(uri, projection, null, null, null);
        StringBuilder buf = new StringBuilder();
        if (c != null) {
            while (c.moveToNext()) {
                buf.append(c.getString(0));
                                                Imps.Avatars.DATA
                if (!c.isLast()) {
                    buf.append(',');
                }
            }
        }
        c.close();

        return buf.toString();
    }*/

    void approveSubscription() {

        ImApp app = ((ImApp) ((Activity) getContext()).getApplication());
        IImConnection mConn = app.getConnection(mHolder.mProviderId, mHolder.mAccountId);


        if (mConn != null) {
            try {
                IContactListManager manager = mConn.getContactListManager();
                manager.approveSubscription(new Contact(new XmppAddress(address), nickname, Imps.Contacts.TYPE_NORMAL));
            } catch (RemoteException e) {

                // mHandler.showServiceErrorAlert(e.getLocalizedMessage());
                LogCleaner.error(ImApp.LOG_TAG, "approve sub error", e);
            }
        }
    }

    void declineSubscription() {

        ImApp app = ((ImApp) ((Activity) getContext()).getApplication());
        IImConnection mConn = app.getConnection(mHolder.mProviderId, mHolder.mAccountId);

        if (mConn != null) {
            try {
                IContactListManager manager = mConn.getContactListManager();
                manager.declineSubscription(new Contact(new XmppAddress(address), nickname, Imps.Contacts.TYPE_NORMAL));
                app.dismissChatNotification(mHolder.mProviderId, address);
                manager.removeContact(address);
            } catch (RemoteException e) {
                // mHandler.showServiceErrorAlert(e.getLocalizedMessage());
                LogCleaner.error(ImApp.LOG_TAG, "decline sub error", e);
            }
        }
    }

    void deleteContact() {
        try {

            IImConnection mConn;
            ImApp app = ((ImApp) ((Activity) getContext()).getApplication());
            mConn = app.getConnection(mHolder.mProviderId, mHolder.mAccountId);

            IContactListManager manager = mConn.getContactListManager();

            int res = manager.removeContact(address);
            if (res != ImErrorInfo.NO_ERROR) {
                //mHandler.showAlert(R.string.error,
                //      ErrorResUtils.getErrorRes(getResources(), res, address));
            }

        } catch (RemoteException re) {

        }
    }

    public void applyStyleColors(ContactViewHolder holder) {
        //not set color
        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getContext());
        int themeColorHeader = settings.getInt("themeColor", -1);
        int themeColorText = settings.getInt("themeColorText", -1);
        int themeColorBg = settings.getInt("themeColorBg", -1);


        if (themeColorText != -1) {
            if (holder.mLine1 != null)
                holder.mLine1.setTextColor(themeColorText);

            if (holder.mLine2 != null)
                holder.mLine2.setTextColor(themeColorText);

            //holder.mLine2.setTextColor(darker(themeColorText,2.0f));

        }

    }


}
