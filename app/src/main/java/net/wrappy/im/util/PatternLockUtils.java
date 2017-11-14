package net.wrappy.im.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import net.wrappy.im.ui.ConfirmPatternActivity;
import net.wrappy.im.ui.SetPatternActivity;

import java.util.List;

import me.zhanghai.android.patternlock.PatternUtils;
import me.zhanghai.android.patternlock.PatternView;


/**
 * Created by PCPV on 11/14/2017.
 */

public class PatternLockUtils {

    public static final int REQUEST_CODE_CONFIRM_PATTERN = 1214;

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
        context.startActivity(new Intent(context, SetPatternActivity.class));
    }

    // NOTE: Should only be called when there is a pattern for this account.
    public static void confirmPattern(Activity activity, int requestCode) {
        activity.startActivityForResult(new Intent(activity, ConfirmPatternActivity.class),
                requestCode);
    }

    public static void confirmPattern(Activity activity) {
        confirmPattern(activity, REQUEST_CODE_CONFIRM_PATTERN);
    }

    public static void confirmPatternIfHas(Activity activity) {
        if (hasPattern(activity)) {
            confirmPattern(activity);
        }
    }



}
