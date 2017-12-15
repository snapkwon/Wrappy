package net.wrappy.im.ui;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;

import net.wrappy.im.R;
import net.wrappy.im.ui.background.BackgroundGridAdapter;

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
        @BindView(R.id.backgroundGridView)
        GridView mGridView;

        public int[] mThumbIds = {
                R.drawable.chat_bg_thumb_1,
                R.drawable.chat_bg_thumb_2,
                R.drawable.chat_bg_thumb_3,
                R.drawable.chat_bg_thumb_4,
                R.drawable.chat_bg_thumb_5,
                R.drawable.chat_bg_thumb_6,
                R.drawable.chat_bg_thumb_7,
                R.drawable.chat_bg_thumb_8
        };

        public String[] mImagePath = {
                "backgrounds/page_1/chat_bg_thumb_1.png",
                "backgrounds/page_1/chat_bg_thumb_2.png",
                "backgrounds/page_1/chat_bg_thumb_3.png",
                "backgrounds/page_1/chat_bg_thumb_4.png",
                "backgrounds/page_1/chat_bg_thumb_5.png",
                "backgrounds/page_1/chat_bg_thumb_6.png",
                "backgrounds/page_1/chat_bg_thumb_7.png",
                "backgrounds/page_1/chat_bg_thumb_8.png",
        };

        public static final BackgroundBottomSheetFragment getInstance() {

            BackgroundBottomSheetFragment backgroundFragment = new BackgroundBottomSheetFragment();

            return backgroundFragment;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.background_grid, container, false);

            ButterKnife.bind(this, view);

            mGridView.setAdapter(new BackgroundGridAdapter(getContext(), mThumbIds));

            mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    Bundle bundle = new Bundle();
                    bundle.putString("imagePath", mImagePath[i]);

                    Intent intent = new Intent();
                    intent.putExtras(bundle);

                    getActivity().setResult(Activity.RESULT_OK, intent);
                    getActivity().finish();
                }
            });

            return view;
        }
    }
}
