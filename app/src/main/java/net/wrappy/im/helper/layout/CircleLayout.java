package net.wrappy.im.helper.layout;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import net.wrappy.im.helper.AppFuncs;

/**
 * Created by ben on 07/12/2017.
 */

public class CircleLayout extends FrameLayout {
    Context context;
    public CircleLayout(@NonNull Context context) {
        super(context);
        this.context = context;
    }

    public CircleLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public CircleLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Path clipPath = new Path();
        clipPath.addRoundRect(new RectF(canvas.getClipBounds()), AppFuncs.convertDpToPixel(24,context), AppFuncs.convertDpToPixel(24,context), Path.Direction.CW);
        canvas.clipPath(clipPath);
        super.onDraw(canvas);
    }
}
