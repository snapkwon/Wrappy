package net.wrappy.im.util;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import net.wrappy.im.ui.PatternActivity;

import java.util.List;

import me.tornado.android.patternlock.PatternUtils;
import me.tornado.android.patternlock.PatternView;


/**
 * Created by PCPV on 11/14/2017.
 */

public class PatternLockUtils {


    private PatternLockUtils() {}

    public static void setPattern(List<PatternView.Cell> pattern, Context context) {
        PreferenceUtils.putString(PreferenceContract.KEY_PATTERN_SHA1,
                PatternUtils.patternToString(pattern), context);

    }

    private static String getPatternSha1(Context context) {
        return PreferenceUtils.getString(PreferenceContract.KEY_PATTERN_SHA1,
                PreferenceContract.DEFAULT_PATTERN_SHA1, context);
    }

    public static boolean hasPattern(Context context) {
        return !TextUtils.isEmpty(getPatternSha1(context));
    }

    public static boolean isPatternCorrect(List<PatternView.Cell> pattern, Context context) {
        return TextUtils.equals(PatternUtils.patternToSha1String(pattern), getPatternSha1(context));
    }

    public static void clearPattern(Context context) {
        PreferenceUtils.remove(PreferenceContract.KEY_PATTERN_SHA1, context);
    }

    public static void setPatternByUser(Context context) {
        context.startActivity(new Intent(context, PatternActivity.class));
    }
}
