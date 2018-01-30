package net.wrappy.im.ui.conversation;

import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.gson.JsonObject;

import net.ironrabbit.type.CustomTypefaceTextView;
import net.wrappy.im.ImApp;
import net.wrappy.im.MainActivity;
import net.wrappy.im.R;
import net.wrappy.im.helper.AppFuncs;
import net.wrappy.im.helper.RestAPI;
import net.wrappy.im.helper.RestAPIListener;
import net.wrappy.im.model.ImConnection;
import net.wrappy.im.provider.Imps;
import net.wrappy.im.service.IChatSession;
import net.wrappy.im.service.IImConnection;
import net.wrappy.im.ui.ConversationListFragment;
import net.wrappy.im.ui.SettingConversationActivity;
import net.wrappy.im.util.PopupUtils;

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

    private IChatSession mSession;
    private String mAddress = null;
    IImConnection connection;

    public void getConnection(IImConnection connection) {
        this.connection = connection;
    }

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
                        public void OnComplete(int httpCode, String error, String s) {

                        }
                    });

                } else {
                    // unpin from top
                    mConversationListFragment.unpinConversation(chatId, getContext());

                    RestAPI.DeleteDataWrappy(getContext(), null, String.format(RestAPI.PIN_CONVERSATION, account), new RestAPIListener() {
                        @Override
                        public void OnComplete(int httpCode, String error, String s) {

                        }
                    });
                }
                dismiss();
                break;
            case R.id.layout_delete_and_exit:
                confirmLeaveGroup((int) chatId);
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

    private void confirmLeaveGroup(int chatId) {
        leaveGroup(chatId);
    }

    private void leaveGroup(int chatId) {
        RestAPI.DeleteDataWrappy(getContext(), new JsonObject(), String.format(RestAPI.DELETE_MEMBER_GROUP, chatId,
                Imps.Account.getAccountName(getContext().getContentResolver(), ImApp.sImApp.getDefaultAccountId())), new RestAPIListener(getContext()) {
            @Override
            public void OnComplete(int httpCode, String error, String s) {
                AppFuncs.log(s != null ? s : "");
                leaveXmppGroup();
            }
        });
    }

    private void leaveXmppGroup() {
        try {
            getSession().leave();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public IChatSession getSession() {
        net.wrappy.im.util.Debug.d("mSession " + mSession);
        if (mSession == null)
            try {
                if (connection.getState() == ImConnection.LOGGED_IN) {
                    mSession = connection.getChatSessionManager().getChatSession(mAddress);
                    if (mSession == null)
                        mSession = connection.getChatSessionManager().createChatSession(mAddress, true);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        return mSession;
    }
}
