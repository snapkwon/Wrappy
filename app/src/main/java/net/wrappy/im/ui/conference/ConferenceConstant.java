package net.wrappy.im.ui.conference;

/**
 * Created by USER on 11/30/2017.
 */

public class ConferenceConstant {
    public static final String CONFERENCE_HOST = "https://video-bridge2.proteusiondev.com:7443/ofmeet/%s";
    public static final String KEY = "video-meeting-request";
    public static final String REGEX = ":";
    public static final String CONFERENCE_PREFIX = REGEX + KEY;
    public static final String CONFERENCE_BRIDGE = "video-bridge-name-";
    public static final int NUM_OF_FIELDS = 4;

    public static final String SEND_LOCATION_FREFIX = ":wrappylocation:";// lat:lng
    public static final String DELETE_CHAT_FREFIX = ":wrappy-delete:";// chat ID
    public static final String EDIT_CHAT_FREFIX = ":wrappy-edit:";//id length:text length:chat ID, new text

    public static final String SEND_BACKGROUND_CHAT_PREFIX = ":wrappychangebackground:"; // image uri

    public static final String SEND_STICKER_BUNNY = ":bunny";
    public static final String SEND_STICKER_EMOJI = ":emoji";
    public static final String SEND_STICKER_ARTBOARD = ":sticker";

    public static final String DELETE_GROUP_BY_ADMIN = ":delete-group";
}
