package net.wrappy.im.util;


import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.wrappy.im.model.ConferenceMessage;

import java.util.EnumMap;
import java.util.Map;

public final class ConferenceUtils {

    /**
     *
     */
    public static String convertConferenceMessage(String message) {
        ConferenceMessage conferenceMessage = new ConferenceMessage(message);
        return conferenceMessage.getType().getType() + " chat " + conferenceMessage.getState().getState().toLowerCase() + ".";
    }
}