package net.wrappy.im.ui.background;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import net.wrappy.im.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by CuongDuong on 12/11/2017.
 */

public class BackgroundPagerAdapter extends PagerAdapter {

    BackgroundGridAdapter[] backgroundGridAdapters;
    ArrayList<BackgroundItem> mBackgroundItems;

    Context mContext;
    BackgroundSelectListener mListener;

    public BackgroundPagerAdapter(Context mContext, ArrayList<BackgroundItem> mBackgroundItems, BackgroundSelectListener mListener) {
        this.mBackgroundItems = mBackgroundItems;
        this.mContext = mContext;
        this.mListener = mListener;

        backgroundGridAdapters = new BackgroundGridAdapter[mBackgroundItems.size()];
    }

    @Override
    public Object instantiateItem(View collection, int position) {

        backgroundGridAdapters[position] = new BackgroundGridAdapter(mContext, mBackgroundItems);

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        GridView imageGrid = (GridView) inflater.inflate(R.layout.background_grid, null);

        imageGrid.setAdapter(backgroundGridAdapters[position]);

        imageGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                GridView gv = (GridView) adapterView;

                BackgroundItem b = (BackgroundItem) ((BackgroundGridAdapter)gv.getAdapter()).getItem(i);

                if (mListener != null) {
                    mListener.onBackgroundSelected(b);

                }
            }
        });

        ((ViewPager)collection).addView(imageGrid);

        return imageGrid;
    }

    @Override
    public int getCount() {
        return mBackgroundItems.size();
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == arg1;
    }

    @Override
    public void destroyItem(ViewGroup collection, int position, Object object) {
        ((ViewPager) collection).removeView((ViewGroup) object);
    }
}
