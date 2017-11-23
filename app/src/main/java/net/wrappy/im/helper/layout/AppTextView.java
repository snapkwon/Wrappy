package net.wrappy.im.helper.layout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.TextView;

import net.wrappy.im.R;

/**
 * Created by ben on 13/11/2017.
 */

@SuppressLint("AppCompatCustomView")
public class AppTextView extends TextView {

    String fonts[] = {AppFonts.FONT_REGULAR, AppFonts.FONT_ITALIC, AppFonts.FONT_LIGHT, AppFonts.FONT_LIGHT_ITALIC, AppFonts.FONT_BOLD, AppFonts.FONT_BOLD_ITALIC};

    public AppTextView(Context context) {
        super(context);
    }

    public AppTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public AppTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public void setTextFont(String font) {
        Typeface myTypeface = Typeface.createFromAsset(getContext().getAssets(), font);
        setTypeface(myTypeface);
    }

    private void init(AttributeSet attrs) {
        Typeface myTypeface = Typeface.createFromAsset(getContext().getAssets(), AppFonts.FONT_REGULAR);
        setTypeface(myTypeface);
        if (!isInEditMode()) {
            initFont(attrs);
        }
    }

    private void initFont(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.AppFonts);
            if (a.getString(R.styleable.AppFonts_font_type) != null) {
                String fontName = fonts[Integer.valueOf(a.getString(net.wrappy.im.R.styleable.AppFonts_font_type))];

                if (fontName != null) {
                    Typeface myTypeface = Typeface.createFromAsset(getContext().getAssets(), fontName);
                    setTypeface(myTypeface);
                }
                a.recycle();
            }
        }
    }
}
