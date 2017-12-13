package net.wrappy.im.util;


import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.Log;

import net.wrappy.im.model.ConferenceMessage;

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