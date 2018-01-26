package net.wrappy.im.util;


import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;

import net.wrappy.im.ImApp;
import net.wrappy.im.R;
import net.wrappy.im.model.ConferenceMessage;
import net.wrappy.im.provider.Imps;
import net.wrappy.im.ui.conference.ConferenceConstant;

public final class ConferenceUtils {

    /**
     *
     */
    public static String convertConferenceMessage(String message) {
        ConferenceMessage conferenceMessage = new ConferenceMessage(message);
        return String.format(ImApp.sImApp.getString(R.string.chat_message_item), conferenceMessage.getType().getType(), conferenceMessage.getState().getState().toLowerCase());
    }

    public static SpannableStringBuilder getConferenceMessageInConversation(String message) {
        ConferenceMessage conferenceMessage = new ConferenceMessage(message);
        Drawable drawable = conferenceMessage.getState().getIcon();
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        ImageSpan imageSpan = new ImageSpan(drawable, DynamicDrawableSpan.ALIGN_BASELINE);
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append("  ").append(conferenceMessage.getState().getState()).append(" call");
        builder.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return builder;
//        return conferenceMessage.getType().getType() + " chat " + conferenceMessage.getState().getState().toLowerCase() + ".";
    }

    public static String getStateConferenceMessage(String message) {
        ConferenceMessage conferenceMessage = new ConferenceMessage(message);
        return conferenceMessage.getState().getState();
    }

    public static String getGoogleMapThumbnail(String lati, String longi) {
        return "http://maps.google.com/maps/api/staticmap?markers=color:blue|" + lati + "," + longi + "&zoom=15&size=200x200&sensor=false";
    }

    public static String[] getEditedMessage(String data) {
        try {
            data = data.replace(ConferenceConstant.EDIT_CHAT_FREFIX, "");
            String key_length = data.substring(0, data.indexOf(":"));
            data = data.substring(data.indexOf(":") + 1);
            String[] ret = new String[2];
            ret[0] = data.substring(0, Integer.parseInt(key_length));
            data = data.substring(Integer.parseInt(key_length) + 1);
            ret[1] = data;
            return ret;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isInvisibleMessage(String message) {
        return !TextUtils.isEmpty(message) && (message.startsWith(ConferenceConstant.SEND_BACKGROUND_CHAT_PREFIX)
                || message.startsWith(ConferenceConstant.DELETE_CHAT_FREFIX) || message.startsWith(ConferenceConstant.EDIT_CHAT_FREFIX)
                || message.startsWith(ConferenceConstant.ERROR_ENCRYPTION_FROM_IOS));
    }

    public static void saveBitmapPreferences(String imagePath, String mNickname, Context context) {
        PreferenceUtils.putString(mNickname, imagePath, context);
    }

    public static String getInfoMessage(Context context, String message) {
        if (message.startsWith(ConferenceConstant.CONFERENCE_PREFIX)) {
            return (ConferenceUtils.getConferenceMessageInConversation(message).toString());
        } else if (message.startsWith(ConferenceConstant.SEND_LOCATION_FREFIX)) {
            return (context.getResources().getString(R.string.location_message));
        } else if (message.startsWith(ConferenceConstant.DELETE_GROUP_BY_ADMIN)) {
            String member = message.split(":")[2];
            return String.format(context.getString(R.string.message_kicked_from_group), member);
        } else if (message.startsWith(ConferenceConstant.REMOVE_MEMBER_GROUP_BY_ADMIN)) {
            String account = Imps.Account.getAccountName(context.getContentResolver(), ImApp.sImApp.getDefaultAccountId());
            String member = message.split(":")[2];
            if (member.startsWith(account)) {
                return "";
            } else {
                return String.format(context.getString(R.string.message_kicked_from_group), member);
            }
        } else if (message.startsWith(ConferenceConstant.REGEX) && message.endsWith(ConferenceConstant.REGEX)) {
            String[] splitMsg = message.split(ConferenceConstant.REGEX);
            String senderSticker = splitMsg.length > 2 ? splitMsg[2] : "";
            if (TextUtils.isEmpty(senderSticker)) {
                return (context.getResources().getString(R.string.message_sticker));
            } else {
                return (context.getResources().getString(R.string.user_sent_sticker, senderSticker));
            }
        } else if (message.startsWith(ConferenceConstant.SEND_LOCATION_FREFIX)) {
            return (context.getResources().getString(R.string.location_message));
        } else if (message.startsWith(ConferenceConstant.SEND_BACKGROUND_CHAT_PREFIX)) {
            return "";
        } else {
            return message;
        }
    }
}