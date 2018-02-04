package net.wrappy.im.ui.conversation;

import android.content.ContentUris;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import net.ironrabbit.type.CustomTypefaceTextView;
import net.wrappy.im.ImApp;
import net.wrappy.im.R;
import net.wrappy.im.helper.AppFuncs;
import net.wrappy.im.helper.RestAPI;
import net.wrappy.im.helper.RestAPIListener;
import net.wrappy.im.model.WpKChatGroupDto;
import net.wrappy.im.provider.Imps;
import net.wrappy.im.tasks.ChatSessionTask;
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

    private String mAddress = null;
    private String groupXmppId = null;
    private long mAccountId = -1;

    WpKChatGroupDto wpKChatGroupDto;

    public static CustomBottomSheetDialogFragment getInstance(long chatId, int chatFavorite, String account, int type, long providerId, long accountId, String address) {
        CustomBottomSheetDialogFragment dialogFragment = new CustomBottomSheetDialogFragment();

        Bundle args = new Bundle();
        args.putLong("chatId", chatId);
        args.putInt("chatFavorite", chatFavorite);
        args.putString("account", account);
        args.putInt("type", type);
        args.putLong("providerId", providerId);
        args.putLong("accountId", accountId);
        args.putString("address", address);

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

            mAddress = getArguments().getString("address");
            groupXmppId = mAddress;

            mAccountId = getArguments().getLong("accountId");

            if (mAddress.contains("@")) {
                groupXmppId = groupXmppId.split("@")[0];
            }

            RestAPI.GetDataWrappy(getContext(), RestAPI.getGroupByXmppId(groupXmppId), new RestAPIListener() {
                @Override
                protected void OnComplete(String s) {
                    Gson gson = new Gson();
                    wpKChatGroupDto = gson.fromJson(s, new TypeToken<WpKChatGroupDto>(){
                    }.getType());
                }
            });

            int chatFavorite = getArguments().getInt("chatFavorite");
            int type = getArguments().getInt("type");

            if (chatFavorite == Imps.Chats.CHAT_UNPIN) {
                mTextPin.setText(getResources().getString(R.string.pin_to_top));
            } else {
                mTextPin.setText(getResources().getString(R.string.unpin_from_top));
            }

            if (type == Imps.Contacts.TYPE_GROUP) {
                mDeleteAndExitLayout.setVisibility(View.VISIBLE);
            } else {
                mCleanHistoryLayout.setVisibility(View.VISIBLE);
            }
        }

        return view;
    }

    @Override
    public void onClick(View view) {

        long chatId = -1;
        int chatFavorite = -1;
        String account = "";

        if (getArguments() != null) {
            chatId = getArguments().getLong("chatId");
            chatFavorite = getArguments().getInt("chatFavorite");
            account = getArguments().getString("account");
        }

        switch (view.getId()) {
            case R.id.layout_pin_to_top:

                if (chatFavorite == Imps.Chats.CHAT_UNPIN) {
                    // pin to top
                    mConversationListFragment.pinConversation(chatId, getContext());

                    RestAPI.PostDataWrappy(getContext(), null, String.format(RestAPI.PIN_CONVERSATION, account), new RestAPIListener() {
                        @Override
                        public void OnComplete(String s) {

                        }
                    });

                } else {
                    // unpin from top
                    mConversationListFragment.unpinConversation(chatId, getContext());

                    RestAPI.DeleteDataWrappy(getContext(), null, String.format(RestAPI.PIN_CONVERSATION, account), new RestAPIListener() {
                        @Override
                        public void OnComplete(String s) {

                        }
                    });
                }
                dismiss();
                break;
            case R.id.layout_delete_and_exit:
                confirmLeaveGroup();
                dismiss();
                break;
            case R.id.layout_clean_history:
                clearHistory((int) chatId);
                dismiss();
                break;
        }
    }

    private void clearHistory(int chatId) {
        Imps.Messages.deleteOtrMessagesByThreadId(getContext().getContentResolver(), chatId);
        Uri chatURI = ContentUris.withAppendedId(Imps.Chats.CONTENT_URI, chatId);
        Imps.Chats.insertOrUpdateChat(getContext().getContentResolver(), chatURI, "", false);
    }

    private void confirmLeaveGroup() {
        leaveGroup(wpKChatGroupDto.getId());
    }

    private void leaveGroup(int groupXmppId) {
        RestAPI.DeleteDataWrappy(getContext(), new JsonObject(), String.format(RestAPI.DELETE_MEMBER_GROUP, groupXmppId,
                Imps.Account.getUserName(getContext().getContentResolver(), mAccountId)), new RestAPIListener(getContext()) {
            @Override
            public void OnComplete(String s) {
                AppFuncs.log(s != null ? s : "");
                leaveXmppGroup();
            }
        });
    }

    private void leaveXmppGroup() {
        new ChatSessionTask().leave().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mAddress);
    }
}
