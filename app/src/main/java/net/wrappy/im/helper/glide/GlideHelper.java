package net.wrappy.im.helper.glide;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.BitmapTypeRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import net.wrappy.im.helper.AppFuncs;
import net.wrappy.im.ui.widgets.LetterAvatar;

/**
 * Created by khoa.nguyen on 12/21/2017.
 */

public class GlideHelper {
    public static void loadBitmapToImageView(Context context, final ImageView imageView, String url) {
        loadBitmap(context, imageView, url, false);
    }

    public static void loadBitmapToCircleImage(Context context, final ImageView imageView, String url) {
        loadBitmap(context, imageView, url, true);
    }

    public static void loadBitmapToCircleImageDefault(Context context, final ImageView imageView, String url, String nickname) {
        loadBitmap(context, imageView, url, true, nickname);
    }

    public static void loadBitmap(Context context, final ImageView imageView, String url, boolean transform) {
        loadBitmap(context, imageView, url, transform, null);
    }

    public static void loadBitmap(final Context context, final ImageView imageView, String url, boolean transform, final String nickname) {
        BitmapTypeRequest<String> request = Glide.with(context).load(url).asBitmap();
        if (transform) request.transform(new CircleTransform(context));
        request.diskCacheStrategy(DiskCacheStrategy.ALL).into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                if (imageView != null && resource != null)
                    imageView.setImageBitmap(resource);
            }

            @Override
            public void onLoadFailed(Exception e, Drawable errorDrawable) {
                super.onLoadFailed(e, errorDrawable);
                AppFuncs.log(e.getLocalizedMessage());
                if (!TextUtils.isEmpty(nickname)) {
                    loadAvatarFromNickname(context, imageView, nickname);
                }
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
