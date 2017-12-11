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

import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by CuongDuong on 12/11/2017.
 */

public class BackgroundGridAdapter extends BaseAdapter {

    private Context mContext;
    ArrayList<BackgroundItem> mBackgroundItems;

    public BackgroundGridAdapter(Context mContext, ArrayList<BackgroundItem> mBackgroundItems) {
        this.mContext = mContext;
        this.mBackgroundItems = mBackgroundItems;
    }

    @Override
    public int getCount() {
        return mBackgroundItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mBackgroundItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ImageView i = null;

        if (convertView != null && convertView instanceof ImageView) {
            i = (ImageView) convertView;
        } else {
            i = new ImageView(mContext);
        }

        try {

            InputStream is = mBackgroundItems.get(position).res.getAssets().open(mBackgroundItems.get(position).assetUri.getPath());
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2;
            Bitmap bmp = BitmapFactory.decodeStream(is, null, options);

            i = new ImageView(mContext);
            i.setLayoutParams(new GridView.LayoutParams(256, 256));
            i.setScaleType(ImageView.ScaleType.FIT_CENTER);
            i.setImageBitmap(bmp);

        } catch (Exception e) {
            Log.e("grid", "problem rendering grid", e);
        }

        return i;
    }
}
