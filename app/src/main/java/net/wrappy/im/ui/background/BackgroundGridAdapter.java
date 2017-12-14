package net.wrappy.im.ui.background;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import net.wrappy.im.R;
import net.wrappy.im.helper.layout.CircleImageView;
import net.wrappy.im.ui.conference.ConferenceConstant;

import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by CuongDuong on 12/11/2017.
 */

public class BackgroundGridAdapter extends BaseAdapter {

    private Context mContext;
    private int[] imageId;

    public BackgroundGridAdapter(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public int getCount() {
        return imageId.length;
    }

    @Override
    public Object getItem(int position) {
        return imageId[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        CircleImageView circleImageView;

        if (convertView == null) {
            circleImageView = new CircleImageView(mContext);
            circleImageView.setLayoutParams(new GridView.LayoutParams(85, 85));
            circleImageView.setScaleType(CircleImageView.ScaleType.CENTER_CROP);
            circleImageView.setPadding(8, 8, 8, 8);
        } else {
            circleImageView = (CircleImageView) convertView;
        }

        circleImageView.setImageResource(mThumbIds[position]);

        return circleImageView;
    }

    public Integer[] mThumbIds = {
            R.drawable.chat_bg_thumb_1,
            R.drawable.chat_bg_thumb_2,
            R.drawable.chat_bg_thumb_3,
            R.drawable.chat_bg_thumb_4,
            R.drawable.chat_bg_thumb_5,
            R.drawable.chat_bg_thumb_6,
            R.drawable.chat_bg_thumb_7,
            R.drawable.chat_bg_thumb_8
    };
}
