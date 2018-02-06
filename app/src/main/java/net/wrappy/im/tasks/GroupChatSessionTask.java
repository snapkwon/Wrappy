package net.wrappy.im.tasks;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.text.TextUtils;

import net.wrappy.im.R;
import net.wrappy.im.helper.AppFuncs;
import net.wrappy.im.model.WpKChatGroupDto;
import net.wrappy.im.model.WpKMemberDto;
import net.wrappy.im.provider.Imps;
import net.wrappy.im.service.IChatSession;
import net.wrappy.im.service.IChatSessionManager;
import net.wrappy.im.service.IImConnection;
import net.wrappy.im.ui.ConversationDetailActivity;
import net.wrappy.im.ui.legacy.DatabaseUtils;
import net.wrappy.im.util.Constant;

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
    OnTaskFinish callback;
    boolean isCreateNewChat = true;

    public GroupChatSessionTask(Activity activity, WpKChatGroupDto group, ArrayList<String> invitees, IImConnection conn) {
        super();
        weakReference = new WeakReference<>(activity);
        mLastConnGroup = conn;
        this.invitees = invitees;
        this.group = group;
    }

    public GroupChatSessionTask(Activity activity, WpKChatGroupDto group, ArrayList<String> invitees, IImConnection conn, boolean iscreatenewchat) {
        super();
        weakReference = new WeakReference<>(activity);
        mLastConnGroup = conn;
        this.invitees = invitees;
        this.group = group;
        this.isCreateNewChat = iscreatenewchat;
    }

    public GroupChatSessionTask(Activity activity, WpKChatGroupDto group, IImConnection conn) {
        super();
        weakReference = new WeakReference<>(activity);
        mLastConnGroup = conn;
        this.group = group;
        needToStartChat = false;
        this.isCreateNewChat = false;
    }

    public void setCallback(OnTaskFinish callback) {
        this.callback = callback;
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

    private void updateInfoInGroup() throws RemoteException {
        AppFuncs.log("updateInfoInGroup");
        if (group != null) {
            //update list member for group
            for (WpKMemberDto member : group.getParticipators()) {
                String address = member.getXMPPAuthDto().getAccount() + Constant.EMAIL_DOMAIN;
                AppFuncs.log(address);
                Imps.GroupMembers.updateNicknameFromGroup(getActivity().getContentResolver(), address, member.getIdentifier());
            }

            //update avatar for group
            if (group.getIcon() != null && !TextUtils.isEmpty(group.getIcon().getReference())) {
                String avatar = group.getIcon().getReference();
                String address = group.getXmppGroup() + "@" + Constant.DEFAULT_CONFERENCE_SERVER;
                String avatarHash = DatabaseUtils.generateHashFromAvatar(avatar);
                DatabaseUtils.insertAvatarHash(getActivity().getContentResolver(), Imps.Avatars.CONTENT_URI, mLastConnGroup.getProviderId(), mLastConnGroup.getAccountId(), avatar, avatarHash, address);
            }
        }
    }

    @Override
    protected String doInBackground(String... params) {
        String subject = group.getName();
        String chatRoom = group.getXmppGroup();
        //String server = params[0];
        String server = Constant.DEFAULT_CONFERENCE_SERVER;
        try {
            IChatSessionManager manager = mLastConnGroup.getChatSessionManager();

            String roomAddress = (chatRoom + '@' + server).toLowerCase(Locale.US);
            String nickname = params[1];

            IChatSession session = manager.getChatSession(roomAddress);

            long mRequestedChatId = -1;

            if (session == null) {
                session = manager.createMultiUserChatSession(roomAddress, subject, nickname, true);
                if (session != null) {
                    if (needToStartChat) {
                        mRequestedChatId = session.getId();
                        publishProgress(mRequestedChatId);
                    } else if (isStable()) {
                        updateInfoInGroup();
                    }
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
                try {
                    dialog.dismiss();
                } catch (Exception e) {

                }
            }
        }

        if (callback != null) {
            callback.onFinished();
        }
    }

    private void showChat(long chatId) {
        if (isStable()) {
            if (isCreateNewChat == true) {
                String reference = "";
                if (group != null && group.getIcon() != null) {
                    reference = group.getIcon().getReference();
                }
                getActivity().startActivity(ConversationDetailActivity.getStartIntent(getActivity(), chatId, group.getName(), reference));
            } else {
                if(needToStartChat)// cheat
                getActivity().finish();
            }
        }
    }

    public interface OnTaskFinish {
        void onFinished();
    }

}
