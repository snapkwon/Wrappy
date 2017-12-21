package net.wrappy.im.tasks;

import android.app.Activity;
import android.os.AsyncTask;

import net.wrappy.im.ImApp;
import net.wrappy.im.model.Contact;
import net.wrappy.im.model.ImConnection;
import net.wrappy.im.provider.Imps;
import net.wrappy.im.service.IChatSession;
import net.wrappy.im.service.IImConnection;

import java.lang.ref.WeakReference;

/**
 * Created by n8fr8 on 10/23/15.
 */
public class ChatSessionInitTask extends AsyncTask<Contact, Long, Long> {

    private long mProviderId;
    private long mAccountId;
    private int mContactType;
    private WeakReference<Activity> weakReference;

    public ChatSessionInitTask(long providerId, long accountId, int contactType) {
        init(providerId, accountId, contactType);
    }

    public ChatSessionInitTask(Activity activity, long providerId, long accountId, int contactType) {
        weakReference = new WeakReference<>(activity);
        init(providerId, accountId, contactType);
    }

    private void init(long providerId, long accountId, int contactType) {
        mProviderId = providerId;
        mAccountId = accountId;
        mContactType = contactType;
    }

    public Long doInBackground(Contact... contacts) {
        if (mProviderId != -1 && mAccountId != -1 && contacts != null) {
            try {
                IImConnection conn = ImApp.getConnection(mProviderId, mAccountId);

                if (conn == null || conn.getState() != ImConnection.LOGGED_IN)
                    return -1L;

                for (Contact contact : contacts) {

                    IChatSession session = conn.getChatSessionManager().getChatSession(contact.getAddress().getAddress());

                    //always need to recreate the MUC after login
////                   if (mContactType == Imps.Contacts.TYPE_GROUP)
                    //                     session = conn.getChatSessionManager().createMultiUserChatSession(contact.getAddress().getAddress(), contact.getName(), null, false);

                    if (session == null) {
                        if (mContactType == Imps.Contacts.TYPE_GROUP)
                            session = conn.getChatSessionManager().createMultiUserChatSession(contact.getAddress().getAddress(), contact.getName(), null, false);
                        else {
                            session = conn.getChatSessionManager().createChatSession(contact.getAddress().getAddress(), false);
                        }
                    }
                    if (session != null)
                        return (session.getId());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return -1L;
    }

    public boolean isStable() {
        return weakReference != null && weakReference.get() != null;
    }
}
