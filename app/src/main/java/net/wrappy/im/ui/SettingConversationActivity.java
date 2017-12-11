package net.wrappy.im.ui;

import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import net.wrappy.im.R;

import java.util.ArrayList;
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

            List<Fragment> fragments = getFragments();
            mPageAdapter = new BackgroundChatPageAdapter(getChildFragmentManager(), fragments);
            mBackgroundViewPager.setAdapter(mPageAdapter);

            return view;
        }

        private List<Fragment> getFragments() {
            List<Fragment> fList = new ArrayList<>();

            fList.add(BackgroundChatFragment.newInstance("Fragment 1"));
            fList.add(BackgroundChatFragment.newInstance("Fragment 2"));

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
