package net.wrappy.im.util;

import net.wrappy.im.BuildConfig;

/**
 * Created by hp on 12/1/2017.
 */

public class Constant {
    public final static String DOMAIN = BuildConfig.DOMAIN;
    public final static String EMAIL_DOMAIN = "@" + DOMAIN;
    public final static String DEFAULT_CONFERENCE_SERVER = "conference." + DOMAIN;
    public final static boolean OMEMO_ENABLED = false;
    public final static boolean CONFERENCE_ENABLED = true;
    public final static long TIME_DELETE_MESSAGE = 30;//Days to delete message in local database
    public static final long MISSED_CALL_TIME = 60000L;//Time to set the call was missed
}
