package net.wrappy.im.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import net.wrappy.im.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by CuongDuong on 12/7/2017.
 */

public class CustomBottomSheetDialogFragment extends BottomSheetDialogFragment implements View.OnClickListener{
    @BindView(R.id.bottom_sheet_layout) View mBottomSheetLayout;
    @BindView(R.id.layout_pin_to_top) LinearLayout mPinToTopLayout;
    @BindView(R.id.layout_delete_and_exit) LinearLayout mDeleteAndExitLayout;
    @BindView(R.id.layout_clean_history) LinearLayout mCleanHistoryLayout;

    private BottomSheetBehavior mBottomSheetBehavior;

    public CustomBottomSheetDialogFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_bottom_sheet_conversation, container, false);

        ButterKnife.bind(this, view);

        mBottomSheetBehavior = BottomSheetBehavior.from(mBottomSheetLayout);

        mPinToTopLayout.setOnClickListener(this);
        mDeleteAndExitLayout.setOnClickListener(this);
        mCleanHistoryLayout.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.layout_pin_to_top:
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                Toast.makeText(getContext(), "Pin to top", Toast.LENGTH_SHORT).show();
                break;
            case R.id.layout_delete_and_exit:
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                Toast.makeText(getContext(), "Delete and exit", Toast.LENGTH_SHORT).show();
                break;
            case R.id.layout_clean_history:
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                Toast.makeText(getContext(), "Clean history", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
