package net.wrappy.im.util;


import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.Log;

import net.wrappy.im.model.ConferenceMessage;
import net.wrappy.im.ui.conference.ConferenceConstant;

import java.io.IOException;

public final class ConferenceUtils {

    /**
     *
     */
    public static String convertConferenceMessage(String message) {
        ConferenceMessage conferenceMessage = new ConferenceMessage(message);
        return conferenceMessage.getType().getType() + " chat " + conferenceMessage.getState().getState().toLowerCase() + ".";
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

    public static boolean listFiles(Context context, String dirFrom, String imagePath) throws IOException {
        Resources res = context.getResources(); //if you are in an activity
        AssetManager am = res.getAssets();
        String fileList[] = am.list(dirFrom);

        if (fileList != null) {
            for (String file : fileList) {

                Log.d("Cuong", "file: " + file);

                if (imagePath.equalsIgnoreCase(file))
                    return true;
            }
        }
        return false;
    }

//    imagePath = dirFrom + / + imagePath
}