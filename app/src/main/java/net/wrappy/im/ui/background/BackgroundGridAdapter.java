package net.wrappy.im.ui.background;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;

import net.wrappy.im.R;
import net.wrappy.im.helper.layout.CircleImageView;

import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by CuongDuong on 12/11/2017.
 */

public class BackgroundGridAdapter extends BaseAdapter {

    private Context mContext;
    private final int[] imageId;

    public BackgroundGridAdapter(Context mContext, int[] imageId) {
        this.mContext = mContext;
        this.imageId = imageId;
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

        View grid;
        LayoutInflater inflater = (LayoutInflater)
                        mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {

            grid = new View(mContext);
            grid = inflater.inflate(R.layout.chat_background_grid_item, null);
            CircleImageView circleImageView = (CircleImageView)
                            grid.findViewById(R.id.image_thumb_background);
            circleImageView.setImageResource(imageId[position]);
        } else {
            grid = convertView;
        }

        return grid;
    }
}
