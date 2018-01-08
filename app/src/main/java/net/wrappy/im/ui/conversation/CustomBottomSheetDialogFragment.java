package net.wrappy.im.ui.conversation;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import net.ironrabbit.type.CustomTypefaceTextView;
import net.wrappy.im.R;
import net.wrappy.im.helper.RestAPI;
import net.wrappy.im.provider.Imps;
import net.wrappy.im.ui.ConversationListFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * This class handles bottom sheet at conversation list
 */
public class CustomBottomSheetDialogFragment extends BottomSheetDialogFragment implements View.OnClickListener {
    @BindView(R.id.bottom_sheet_layout)
    View mBottomSheetLayout;
    @BindView(R.id.layout_pin_to_top)
    LinearLayout mPinToTopLayout;
    @BindView(R.id.layout_delete_and_exit)
    LinearLayout mDeleteAndExitLayout;
    @BindView(R.id.layout_clean_history)
    LinearLayout mCleanHistoryLayout;
    @BindView(R.id.txt_pin_top)
    CustomTypefaceTextView mTextPin;

    private BottomSheetBehavior mBottomSheetBehavior;
    private ConversationListFragment mConversationListFragment;

    public static CustomBottomSheetDialogFragment getInstance(long chatId, int chatFavorite, String account) {
        CustomBottomSheetDialogFragment dialogFragment = new CustomBottomSheetDialogFragment();

        Bundle args = new Bundle();
        args.putLong("chatId", chatId);
        args.putInt("chatFavorite", chatFavorite);
        args.putString("account", account);

        dialogFragment.setArguments(args);

        return dialogFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_bottom_sheet_conversation, container, false);

        ButterKnife.bind(this, view);

        mConversationListFragment = new ConversationListFragment();

        mBottomSheetBehavior = BottomSheetBehavior.from(mBottomSheetLayout);

        mPinToTopLayout.setOnClickListener(this);
        mDeleteAndExitLayout.setOnClickListener(this);
        mCleanHistoryLayout.setOnClickListener(this);

        if (getArguments() != null) {
            int chatFavorite = getArguments().getInt("chatFavorite");

            if (chatFavorite == Imps.Chats.CHAT_UNPIN) {
                mTextPin.setText(getResources().getString(R.string.pin_to_top));
            } else {
                mTextPin.setText(getResources().getString(R.string.unpin_from_top));
            }
        }

        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.layout_pin_to_top:

                if (getArguments() != null) {

                    long chatId = getArguments().getLong("chatId");
                    int chatFavorite = getArguments().getInt("chatFavorite");
                    String account = getArguments().getString("account");

                    if (chatFavorite == Imps.Chats.CHAT_UNPIN) {
                        // pin to top
                        mConversationListFragment.pinConversation(chatId, getContext());

                        RestAPI.PostDataWrappy(getContext(), null, String.format(RestAPI.PIN_CONVERSATION, account), new RestAPI.RestAPIListenner() {
                            @Override
                            public void OnComplete(int httpCode, String error, String s) {

                            }
                        });

                    } else {
                        // unpin from top
                        mConversationListFragment.unpinConversation(chatId, getContext());

                        RestAPI.DeleteDataWrappy(getContext(), null, String.format(RestAPI.PIN_CONVERSATION, account), new RestAPI.RestAPIListenner() {
                            @Override
                            public void OnComplete(int httpCode, String error, String s) {

                            }
                        });
                    }
                    dismiss();
                }
                break;
            case R.id.layout_delete_and_exit:
                break;
            case R.id.layout_clean_history:
                break;
        }
    }
}
