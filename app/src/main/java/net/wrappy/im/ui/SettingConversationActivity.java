package net.wrappy.im.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import net.wrappy.im.R;
import net.wrappy.im.ui.background.BackgroundItem;
import net.wrappy.im.ui.background.BackgroundPagerAdapter;
import net.wrappy.im.ui.background.BackgroundSelectListener;
import net.wrappy.im.ui.stickers.Sticker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingConversationActivity extends AppCompatActivity implements View.OnClickListener {
    @BindView(R.id.layout_search_setting) LinearLayout mSearchLayout;
    @BindView(R.id.layout_change_background_setting) LinearLayout mChangeBackgroundLayout;

    private BackgroundBottomSheetFragment mBackgroundFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_conversation);

        ButterKnife.bind(this);

        mChangeBackgroundLayout.setOnClickListener(this);

        // back button at action bar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getResources().getString(R.string.setting_screen));
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_action_arrow_back);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.layout_change_background_setting:
                Toast.makeText(getApplicationContext(), "Change background",
                                Toast.LENGTH_SHORT).show();
                mBackgroundFragment = BackgroundBottomSheetFragment.getInstance();
                mBackgroundFragment.show(getSupportFragmentManager(), "Dialog");
                break;
        }
    }

    public static class BackgroundBottomSheetFragment extends BottomSheetDialogFragment
            implements View.OnClickListener {
        @BindView(R.id.background_chat_view_pager)
        ViewPager mBackgroundViewPager;

        private BackgroundChatPageAdapter mPageAdapter;

        public static final BackgroundBottomSheetFragment getInstance() {

            BackgroundBottomSheetFragment backgroundFragment = new BackgroundBottomSheetFragment();

            return  backgroundFragment;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.change_background_view_pager, container, false);

            ButterKnife.bind(this, view);

            ArrayList<BackgroundItem> listItem = new ArrayList<>();
            listItem = getBackgroundItemList();

            BackgroundPagerAdapter adapter = new BackgroundPagerAdapter(getContext(), listItem,
                    new BackgroundSelectListener() {
                        @Override
                        public void onBackgroundSelected(BackgroundItem item) {
                            Toast.makeText(getContext(), "item: " + item.getAssetUri(),
                                            Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent();
                            intent.putExtra("imagePath", item.getAssetUri());

                            getActivity().setResult(Activity.RESULT_OK, intent);
                            getActivity().finish();
                        }
                    });

            mBackgroundViewPager.setAdapter(adapter);
            mBackgroundViewPager.setOffscreenPageLimit(1);

            return view;
        }

        private ArrayList<BackgroundItem> getBackgroundItemList() {

            ArrayList<BackgroundItem> listItem = new ArrayList<>();

            try {
                String basePath = "backgrounds";
                AssetManager aMan = getActivity().getAssets();
                String[] filelist = aMan.list(basePath);

                for (int i = 0; i < filelist.length; i++) {
                    BackgroundItem item = new BackgroundItem();
                    item.assetUri = Uri.parse(basePath + '/' + filelist[i]);
                    item.res = getActivity().getResources();

                    listItem.add(item);
                }

                Log.d("Cuong", "size: " + listItem.size());

                return listItem;

            } catch (IOException e) {
                e.printStackTrace();
            }

            return listItem;

        }

        private List<Fragment> getFragments() {
            List<Fragment> fList = new ArrayList<>();

            fList.add(BackgroundChatFragment.newInstance());
            fList.add(BackgroundChatFragment.newInstance());

            return fList;
        }

        @Override
        public void onClick(View view) {

        }

        private class BackgroundChatPageAdapter extends FragmentPagerAdapter {
            private List<Fragment> fragments;

            public BackgroundChatPageAdapter(FragmentManager fm, List<Fragment> fragments) {
                super(fm);
                this.fragments = fragments;
            }

            @Override
            public Fragment getItem(int position) {
                return this.fragments.get(position);
            }

            @Override
            public int getCount() {
                return this.fragments.size();
            }
        }
    }


}
