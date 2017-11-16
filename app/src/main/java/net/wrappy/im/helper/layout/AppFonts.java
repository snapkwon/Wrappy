package net.wrappy.im.helper.layout;

import android.content.Context;
import android.graphics.Typeface;

/**
 * Created by ben on 13/11/2017.
 */

public class AppFonts {
    public static final String FONT_REGULAR = "fonts/rmedium.ttf";
    public static final String FONT_ITALIC = "fonts/rmediumitalic.ttf";
    public static final String FONT_BOLD = "fonts/Roboto-Bold.ttf";
    public static final String FONT_LIGHT = "fonts/Roboto-Light.ttf";
    public static final String FONT_MEDIUM = "fonts/Roboto-Medium.ttf";

    public static Typeface getFont(Context context, String fontName) {
        Typeface myTypeface = Typeface.createFromAsset(context.getAssets(), fontName);
        return myTypeface;
    }
}
