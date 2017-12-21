package net.wrappy.im.helper.glide;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import net.wrappy.im.ui.widgets.LetterAvatar;

/**
 * Created by khoa.nguyen on 12/21/2017.
 */

public class GlideHelper {
    public static void loadBitmapToCircleImage(Context context, final ImageView imageView, String url) {
        Glide.with(context).load(url).asBitmap().transform(new CircleTransform(context)).diskCacheStrategy(DiskCacheStrategy.ALL).into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                if (imageView != null)
                    imageView.setImageBitmap(resource);
            }
        });
    }

    public static void loadAvatarFromNickname(Context context, final ImageView imageView, String nickname) {
        try {
            int padding = 24;
            LetterAvatar lavatar = new LetterAvatar(context, nickname, padding);
            imageView.setImageDrawable(lavatar);
            imageView.setVisibility(View.VISIBLE);
        } catch (OutOfMemoryError ome) {
            //this seems to happen now and then even on tiny images; let's catch it and just not set an avatar
        }
    }
}
