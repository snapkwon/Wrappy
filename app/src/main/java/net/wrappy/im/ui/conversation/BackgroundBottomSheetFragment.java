package net.wrappy.im.ui.conversation;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import net.wrappy.im.R;
import net.wrappy.im.ui.ConversationDetailActivity;
import net.wrappy.im.ui.background.BackgroundGridAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Showing bottom sheet to change background
 */
public class BackgroundBottomSheetFragment extends BottomSheetDialogFragment {
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
            "backgrounds/page_1/chat_bg_1.jpg",
            "backgrounds/page_1/chat_bg_2.jpg",
            "backgrounds/page_1/chat_bg_3.jpg",
            "backgrounds/page_1/chat_bg_4.jpg",
            "backgrounds/page_1/chat_bg_5.jpg",
            "backgrounds/page_1/chat_bg_6.jpg",
            "backgrounds/page_1/chat_bg_7.jpg",
            "backgrounds/page_1/chat_bg_8.jpg",
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
                bundle.putInt("type", ConversationDetailActivity.TYPE_REQUEST_CHANGE_BACKGROUND);
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
