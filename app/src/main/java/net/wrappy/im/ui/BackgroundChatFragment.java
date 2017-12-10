package net.wrappy.im.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.wrappy.im.R;
import net.wrappy.im.helper.layout.CircleImageView;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by CuongDuong on 12/10/2017.
 */

public class BackgroundChatFragment extends Fragment {
    @BindView(R.id.text_chat_background)
    TextView textView;
    @BindView(R.id.background_grid_view)
    GridLayout mBackgroundGridLayout;


    public static final String EXTRA_MESSAGE = "EXTRA_MESSAGE";

    public static final BackgroundChatFragment newInstance(String message) {
        BackgroundChatFragment backgroundFragment = new BackgroundChatFragment();

        Bundle args = new Bundle();
        args.putString(EXTRA_MESSAGE, message);

        backgroundFragment.setArguments(args);

        return backgroundFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.background_chat_fragment, container, false);

        ButterKnife.bind(this, view);

        String message = getArguments().getString(EXTRA_MESSAGE);
        textView.setText(message);

        onGridViewClickListener();

        return view;
    }

    private void onGridViewClickListener() {
        int childCount = mBackgroundGridLayout.getChildCount();

        for (int i = 0; i < childCount; i++) {

            final int position = i;

            CircleImageView imageView = (CircleImageView) mBackgroundGridLayout.getChildAt(position);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(getContext(), "" + position, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
