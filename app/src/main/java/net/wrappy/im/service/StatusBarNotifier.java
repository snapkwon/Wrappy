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

package net.wrappy.im.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import net.wrappy.im.ImApp;
import net.wrappy.im.MainActivity;
import net.wrappy.im.Preferences;
import net.wrappy.im.R;
import net.wrappy.im.RouterActivity;
import net.wrappy.im.model.Contact;
import net.wrappy.im.provider.Imps;

import java.util.ArrayList;

import me.leolin.shortcutbadger.ShortcutBadger;

public class StatusBarNotifier {
    private static final boolean DBG = false;

    private static final long SUPPRESS_SOUND_INTERVAL_MS = 3000L;

    static final long[] VIBRATE_PATTERN = new long[] { 0, 250, 250, 250 };

    private Context mContext;
    private NotificationManager mNotificationManager;

    private Handler mHandler;
    private ArrayList<NotificationInfo> mNotificationInfos;
    private long mLastSoundPlayedMs;

    public StatusBarNotifier(Context context) {
        mContext = context;
        mNotificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        mHandler = new Handler();
        mNotificationInfos = new ArrayList<>();
    }

    public void notifyChat(long providerId, long accountId, long chatId, String username,
                           String nickname, String msg, boolean lightWeightNotify) {
        if (!Preferences.isNotificationEnabled()) {
            return;
        }
        //msg = html2text(msg); // strip tags for html client inbound msgs
        Bitmap avatar = BitmapFactory.decodeResource(mContext.getResources(),
                R.mipmap.ic_launcher);
        String title = nickname;
        String snippet = mContext.getString(R.string.new_messages_notify) + ' ' + nickname;// + ": " + msg;
        Intent intent = getDefaultIntent(accountId, providerId);//new Intent(Intent.ACTION_VIEW);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(ContentUris.withAppendedId(Imps.Chats.CONTENT_URI, chatId),Imps.Chats.CONTENT_ITEM_TYPE);
        intent.putExtra(ImServiceConstants.EXTRA_INTENT_FROM_ADDRESS, username);
        intent.putExtra("title", title);
        intent.putExtra("isgroup", false);
        intent.addCategory(ImApp.IMPS_CATEGORY);
        notify(username, title, snippet, msg, providerId, accountId, intent, lightWeightNotify, R.drawable.notify_wrappy, avatar);
    }

    public void notifyGroupChat(long providerId, long accountId, long chatId, String remoteAddress, String groupname,
            String nickname, String msg, boolean lightWeightNotify) {

        Bitmap avatar = BitmapFactory.decodeResource(mContext.getResources(),
                R.mipmap.ic_launcher);

        String snippet = mContext.getString(R.string.new_messages_notify) + ' ' + groupname;// + ": " + msg;
        Intent intent = getDefaultIntent(accountId, providerId);//new Intent(Intent.ACTION_VIEW);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(ContentUris.withAppendedId(Imps.Chats.CONTENT_URI, chatId),Imps.Chats.CONTENT_ITEM_TYPE);
        intent.putExtra(ImServiceConstants.EXTRA_INTENT_FROM_ADDRESS, remoteAddress);
        intent.putExtra("title", groupname);
        intent.putExtra("isgroup", true);
        intent.addCategory(ImApp.IMPS_CATEGORY);
        notify(remoteAddress, groupname, snippet, nickname + ": " + msg, providerId, accountId, intent, lightWeightNotify, R.drawable.notify_wrappy, avatar);
    }

    public void notifyError(String username, String error) {

        Intent intent = getDefaultIntent(-1,-1);
        notify(username, error, error, error, -1, -1, intent, true, R.drawable.alerts_and_states_error);
    }

    public void notifySubscriptionRequest(long providerId, long accountId, long contactId,
            String username, String nickname) {
        if (!Preferences.isNotificationEnabled()) {
            return;
        }
        String title = nickname;
        String message = mContext.getString(R.string.subscription_notify_text, nickname);
        Intent intent = getDefaultIntent(accountId, providerId);
        intent.setAction(Intent.ACTION_VIEW);
        intent.putExtra(ImServiceConstants.EXTRA_INTENT_FROM_ADDRESS, username);
        intent.setType(Imps.Contacts.CONTENT_ITEM_TYPE);
        intent.addCategory(ImApp.IMPS_CATEGORY);
        notify(username, title, message, message, providerId, accountId, intent, false, R.drawable.ic_people_white_24dp);
    }

    public void notifySubscriptionApproved(Contact contact, long providerId, long accountId) {
        if (!Preferences.isNotificationEnabled()) {
            return;
        }

        String title = ImApp.getNickname(contact.getAddress().getBareAddress());
        ;
        String message = mContext.getString(R.string.invite_accepted);
        Intent intent = getDefaultIntent(accountId, providerId);//new Intent(Intent.ACTION_VIEW);
        intent.setAction(Intent.ACTION_VIEW);
        intent.putExtra(ImServiceConstants.EXTRA_INTENT_FROM_ADDRESS, contact.getAddress().getBareAddress());
        intent.setType(Imps.Contacts.CONTENT_ITEM_TYPE);
        intent.addCategory(ImApp.IMPS_CATEGORY);

        notify(contact.getAddress().getAddress(), title, message, message, providerId, accountId, intent, false, R.drawable.ic_people_white_24dp);
    }


    public void notifyGroupInvitation(long providerId, long accountId, long invitationId,
            String username) {

        Intent intent = new Intent(Intent.ACTION_VIEW, ContentUris.withAppendedId(
                Imps.Invitation.CONTENT_URI, invitationId));

        String title = mContext.getString(R.string.notify_groupchat_label);
        String message = mContext.getString(R.string.group_chat_invite_notify_text, username);
        notify(username, title, message, message, providerId, accountId, intent, false, R.drawable.notify_wrappy);
    }

    public void notifyLoggedIn(long providerId, long accountId) {

        Intent intent = new Intent(mContext, MainActivity.class);
        ;

        String title = mContext.getString(R.string.app_name);
        String message = mContext.getString(R.string.presence_available);
        notify(message, title, message, message, providerId, accountId, intent, false, R.drawable.notify_wrappy);
    }

    public void notifyLocked() {

        Intent intent = new Intent(mContext, RouterActivity.class);

        String title = mContext.getString(R.string.app_name);
        String message = mContext.getString(R.string.account_setup_pers_now_title);
        notify(message, title, message, message, -1, -1, intent, true, R.drawable.ic_lock_outline_black_18dp);


    }

    public void notifyDisconnected(long providerId, long accountId) {

        Intent intent = new Intent(mContext, MainActivity.class);
        ;

        String title = mContext.getString(R.string.app_name);
        String message = mContext.getString(R.string.presence_offline);
        notify(message, title, message, message, providerId, accountId, intent, false, R.drawable.alerts_and_states_error);
    }

    public void dismissNotifications(long providerId) {

        Object[] infos = mNotificationInfos.toArray();

        for (Object nInfo : infos) {
            NotificationInfo info = (NotificationInfo)nInfo;
            if (info.mProviderId == providerId) {
                mNotificationManager.cancel(info.computeNotificationId());
                mNotificationInfos.remove(info);
            }
        }
    }

    public void dismissChatNotification(long providerId, String username) {
        Object[] infos = mNotificationInfos.toArray();

        for (Object nInfo : infos) {
            NotificationInfo info = (NotificationInfo)nInfo;
            if (info.getSender() != null && info.getSender().equals(username)) {
                mNotificationManager.cancel(info.computeNotificationId());
                mNotificationInfos.remove(info);
            }
        }
        ShortcutBadger.applyCount(ImApp.sImApp.getApplicationContext(), mNotificationInfos.size());
    }

    public void notify(String title, String tickerText, String message,
                        Intent intent, boolean lightWeightNotify) {

        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP);

        NotificationInfo info;
                info = new NotificationInfo(-1, -1);
            info.setInfo("", title, message, null, intent);

        mNotificationManager.notify(info.computeNotificationId(),
                info.createNotification(tickerText, lightWeightNotify, R.drawable.notify_wrappy, null, intent));


    }

    private void notify(String sender, String title, String tickerText, String message,
                        long providerId, long accountId, Intent intent, boolean lightWeightNotify, int iconSmall) {

        notify(sender,title,tickerText,message,providerId,accountId,intent,lightWeightNotify,iconSmall,null);
    }

    private void notify(String sender, String title, String tickerText, String message,
                        long providerId, long accountId, Intent intent, boolean lightWeightNotify, int iconSmall, Bitmap iconLarge) {

        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP);

        NotificationInfo info = null;

        StringBuffer sbMessage = new StringBuffer();

        synchronized (mNotificationInfos) {

            for (NotificationInfo nInfo : mNotificationInfos)
            {
                if (nInfo.getSender() != null && nInfo.getSender().equals(sender))
                {
                    info = nInfo;
                    if (info.getBigMessage() != null)
                        sbMessage.append(info.getBigMessage()+'\n');
                    else
                        sbMessage.append(info.getMessage()+'\n');
                    break;
                }
            }

            if (info == null) {
                info = new NotificationInfo(providerId, accountId);
                mNotificationInfos.add(info);
                info.setInfo(sender, title, message, null, intent);

            }
            else {
                sbMessage.append(message);
                info.setInfo(sender, title, message, sbMessage.toString(), intent);
            }
        }

        mNotificationManager.notify(info.computeNotificationId(),
                info.createNotification(tickerText, lightWeightNotify, iconSmall, iconLarge, intent));

        ShortcutBadger.applyCount(ImApp.sImApp.getApplicationContext(), mNotificationInfos.size());


    }

    private Intent getDefaultIntent(long accountId, long providerId) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setType(Imps.Contacts.CONTENT_TYPE);
        intent.setClass(mContext, MainActivity.class);
        intent.putExtra(ImServiceConstants.EXTRA_INTENT_ACCOUNT_ID, accountId);
        intent.putExtra(ImServiceConstants.EXTRA_INTENT_PROVIDER_ID, providerId);

        return intent;
    }



    private void setRinger(long providerId, NotificationCompat.Builder builder) {
        Uri ringtoneUri = Preferences.getNotificationRingtoneUri();
        builder.setSound(ringtoneUri);
        mLastSoundPlayedMs = SystemClock.elapsedRealtime();

        if (Preferences.getNotificationVibrate()) {
            builder.setDefaults(Notification.DEFAULT_VIBRATE);
        }
    }

    private static int UNIQUE_INT_PER_CALL = 10000;

    class NotificationInfo {

        private long mProviderId;
        private long mAccountId;
        private String mTitle;
        private String mMessage;
        private String mBigMessage;
        private Intent mIntent;
        private String mSender;

        public NotificationInfo(long providerId, long accountId) {
            mProviderId = providerId;
            mAccountId = accountId;
        }

        public int computeNotificationId() {
            if (mTitle == null)
                return (int)mProviderId;
            return (mSender).hashCode();
        }

        public void setInfo(String sender, String title, String message, String bigMessage, Intent intent) {
            mTitle = title;
            mMessage = message;
            mIntent = intent;
            mSender = sender;
            mBigMessage = bigMessage;
        }


        public Notification createNotification(String tickerText, boolean lightWeightNotify, int icon, Bitmap largeIcon, Intent intent) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);

            builder
                .setSmallIcon(icon)
                .setTicker(lightWeightNotify ? null : tickerText)
                .setWhen(System.currentTimeMillis())
                .setLights(0xff00ff00, 300, 1000)
                .setContentTitle(getTitle())
                .setContentText(getMessage())
                .setContentIntent(PendingIntent.getActivity(mContext, UNIQUE_INT_PER_CALL++, intent, PendingIntent.FLAG_UPDATE_CURRENT))
                .setAutoCancel(true);

            if (!TextUtils.isEmpty(mBigMessage))
            {
                /*
         * Sets the big view "big text" style and supplies the
         * text (the user's reminder message) that will be displayed
         * in the detail area of the expanded notification.
         * These calls are ignored by the support library for
         * pre-4.1 devices.
         */
                builder.setStyle(new NotificationCompat.BigTextStyle()
                    .bigText(mBigMessage));
            }

            if (largeIcon != null)
                builder.setLargeIcon(largeIcon);

            if (!(lightWeightNotify || shouldSuppressSoundNotification())) {
                setRinger(mProviderId, builder);
            }

            return builder.build();
        }


        private Intent getMultipleNotificationIntent() {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setClass(mContext, MainActivity.class);
            intent.putExtra(ImServiceConstants.EXTRA_INTENT_PROVIDER_ID, mProviderId);
            intent.putExtra(ImServiceConstants.EXTRA_INTENT_ACCOUNT_ID, mAccountId);
            intent.putExtra(ImServiceConstants.EXTRA_INTENT_SHOW_MULTIPLE, true);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            return intent;
        }

        public String getSender () {
            return mSender;
        }

        public String getTitle() {
                        return mTitle;
        }

        public String getBigMessage ()
        {
            return mBigMessage;
        }

        public String getMessage() {

            return mMessage;
        }

        public Intent getIntent() {

            return mIntent;
        }
    }

    private boolean shouldSuppressSoundNotification() {
        return (SystemClock.elapsedRealtime() - mLastSoundPlayedMs < SUPPRESS_SOUND_INTERVAL_MS);
    }

    /**
    public static String html2text(String html) {
        return Jsoup.parse(html).text();
    }**/
}
