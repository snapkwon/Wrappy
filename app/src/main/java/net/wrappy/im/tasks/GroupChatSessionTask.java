package net.wrappy.im.tasks;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.RemoteException;

import net.wrappy.im.R;
import net.wrappy.im.model.WpKChatGroupDto;
import net.wrappy.im.service.IChatSession;
import net.wrappy.im.service.IChatSessionManager;
import net.wrappy.im.service.IImConnection;
import net.wrappy.im.ui.ConversationDetailActivity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by Khoa.Nguyen on 10/23/15.
 */
public class GroupChatSessionTask extends AsyncTask<String, Long, String> {
    private ProgressDialog dialog;
    private WeakReference<Activity> weakReference;
    private IImConnection mLastConnGroup;
    private ArrayList<String> invitees;
    private WpKChatGroupDto group;
    boolean needToStartChat = true;

    public GroupChatSessionTask(Activity activity, WpKChatGroupDto group, ArrayList<String> invitees, IImConnection conn) {
        super();
        weakReference = new WeakReference<>(activity);
        mLastConnGroup = conn;
        this.invitees = invitees;
        this.group = group;
    }

    public GroupChatSessionTask(Activity activity, WpKChatGroupDto group, IImConnection conn) {
        super();
        weakReference = new WeakReference<>(activity);
        mLastConnGroup = conn;
        this.group = group;
        needToStartChat = false;
    }

    private boolean isStable() {
        return weakReference != null && weakReference.get() != null;
    }

    private Activity getActivity() {
        //need to check stable before get the instance
        return weakReference.get();
    }

    @Override
    protected void onPreExecute() {
        if (isStable() && needToStartChat) {
            dialog = new ProgressDialog(getActivity());

            dialog.setMessage(getActivity().getString(R.string.connecting_to_group_chat_));
            dialog.setCancelable(true);
            dialog.show();
        }
    }

    @Override
    protected String doInBackground(String... params) {

        String subject = group.getName();
        String chatRoom = group.getXmppGroup();
        String server = params[0];


        try {

            IChatSessionManager manager = mLastConnGroup.getChatSessionManager();

            String roomAddress = (chatRoom + '@' + server).toLowerCase(Locale.US);
            String nickname = params[1];

            IChatSession session = manager.getChatSession(roomAddress);

            long mRequestedChatId = -1;
            if (session == null) {
                session = manager.createMultiUserChatSession(roomAddress, subject, nickname, true);
                if (session != null && needToStartChat) {
                    mRequestedChatId = session.getId();
                    publishProgress(mRequestedChatId);

                } else {
                    if (isStable()) {
                        return getActivity().getString(R.string.unable_to_create_or_join_group_chat);
                    } else {
                        return null;
                    }
                }
            } else {
                mRequestedChatId = session.getId();
                publishProgress(mRequestedChatId);
            }
//
            if (invitees != null && invitees.size() > 0) {
                //wait a second for the server to sort itself out
                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                }

                for (String invitee : invitees)
                    session.inviteContact(invitee);
            }

            return null;

        } catch (RemoteException e) {
            return e.toString();
        }

    }

    @Override
    protected void onProgressUpdate(Long... showChatId) {
        showChat(showChatId[0]);
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if (isStable()) {
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
        }
    }

    private void showChat(long chatId) {
        if (isStable()) {
            Intent intent = ConversationDetailActivity.getStartIntent(getActivity());
            intent.putExtra("id", chatId);
            getActivity().startActivity(intent);
        }
    }

}
