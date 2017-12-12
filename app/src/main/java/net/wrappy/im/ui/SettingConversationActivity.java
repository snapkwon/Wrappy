package net.wrappy.im.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
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

import net.wrappy.im.ImApp;
import net.wrappy.im.R;
import net.wrappy.im.ui.background.BackgroundGroup;
import net.wrappy.im.ui.background.BackgroundItem;
import net.wrappy.im.ui.background.BackgroundPagerAdapter;
import net.wrappy.im.ui.background.BackgroundSelectListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingConversationActivity extends AppCompatActivity implements View.OnClickListener {
    @BindView(R.id.layout_search_setting)
    LinearLayout mSearchLayout;
    @BindView(R.id.layout_change_background_setting)
    LinearLayout mChangeBackgroundLayout;

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
                mBackgroundFragment = BackgroundBottomSheetFragment.getInstance();
                mBackgroundFragment.show(getSupportFragmentManager(), "Dialog");
                break;
        }
    }

    /**
     * Showing bottom sheet to change background
     */
    public static class BackgroundBottomSheetFragment extends BottomSheetDialogFragment {
        @BindView(R.id.background_chat_view_pager)
        ViewPager mBackgroundViewPager;

        private TreeMap<String, BackgroundGroup> groups = new TreeMap<>();

        private final static String[][] backgroundGroups = new String[][]{
                {
                        "backgrounds/page_1",
                        "page_1"
                },
                {
                        "backgrounds/page_2",
                        "page_2"
                }
        };

        public static final BackgroundBottomSheetFragment getInstance() {

            BackgroundBottomSheetFragment backgroundFragment = new BackgroundBottomSheetFragment();

            return backgroundFragment;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.change_background_view_pager, container, false);

            ButterKnife.bind(this, view);

            initBackgrounds();

            BackgroundPagerAdapter adapter = new BackgroundPagerAdapter(getContext(), new ArrayList<>(groups.values()),
                    new BackgroundSelectListener() {
                        @Override
                        public void onBackgroundSelected(BackgroundItem item) {

                            // send image Uri to ConversationDetailActivity
                            Bundle bundle = new Bundle();
                            bundle.putParcelable("imageUri", item.assetUri);

                            Intent intent = new Intent();
                            intent.putExtras(bundle);

                            getActivity().setResult(Activity.RESULT_OK, intent);
                            getActivity().finish();
                        }
                    });

            mBackgroundViewPager.setAdapter(adapter);

            return view;
        }

        /**
         * looping for each pager
         */
        private void initBackgrounds() {
            try {

                for (String[] group : backgroundGroups) {
                    String basePath = group[0];
                    String groupName = group[1];
                    addBackground(groupName, basePath);
                }

            } catch (Exception e) {
                Log.e(ImApp.LOG_TAG, "could not load background definition", e);
            }
        }

        /**
         * adding background to each group pager
         *
         * @param groupName
         * @param basePath
         */
        private void addBackground(String groupName, String basePath) {
            try {
                AssetManager aMan = getActivity().getAssets();
                String[] fileList = aMan.list(basePath);

                BackgroundGroup group = new BackgroundGroup();

                for (int i = 0; i < fileList.length; i++) {
                    BackgroundItem item = new BackgroundItem();
                    item.assetUri = Uri.parse(basePath + '/' + fileList[i]);
                    item.res = getActivity().getResources();

                    group.getItems().add(item);
                }

                groups.put(groupName, group);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
