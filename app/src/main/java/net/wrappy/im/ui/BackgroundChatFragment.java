package net.wrappy.im.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
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

import java.io.ByteArrayOutputStream;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by CuongDuong on 12/10/2017.
 */

public class BackgroundChatFragment extends Fragment {
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

        onGridViewClickListener();

        return view;
    }

    private void onGridViewClickListener() {
        int childCount = mBackgroundGridLayout.getChildCount();

        for (int i = 0; i < childCount; i++) {

            final int position = i;

            final CircleImageView imageView = (CircleImageView) mBackgroundGridLayout.getChildAt(position);

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(getContext(), "" + position, Toast.LENGTH_SHORT).show();
                    Drawable drawable = imageView.getDrawable();
                    if (drawable != null) {
                        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), imageView.getId());
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                        byte[] b = baos.toByteArray();

                        Intent intent = new Intent();
                        intent.putExtra("picture", b);
                        getActivity().setResult(Activity.RESULT_OK, intent);
                        getActivity().finish();
                    }
                }
            });
        }
    }
}
