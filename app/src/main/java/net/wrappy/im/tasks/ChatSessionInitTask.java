package net.wrappy.im.tasks;

import android.os.AsyncTask;

import net.java.otr4j.OtrPolicy;
import net.wrappy.im.ImApp;
import net.wrappy.im.model.ImConnection;
import net.wrappy.im.provider.Imps;
import net.wrappy.im.service.IChatSession;
import net.wrappy.im.service.IImConnection;

import net.wrappy.im.Preferences;
import net.wrappy.im.model.Contact;

/**
 * Created by n8fr8 on 10/23/15.
 */
public class ChatSessionInitTask extends AsyncTask<Contact, Long, Long> {

    ImApp mApp;
    long mProviderId;
    long mAccountId;
    int mContactType;

    public ChatSessionInitTask (ImApp app, long providerId, long accountId, int contactType)
    {
        mApp = app;
        mProviderId = providerId;
        mAccountId = accountId;
        mContactType = contactType;
    }

    public Long doInBackground (Contact... contacts)
    {
        if (mProviderId != -1 && mAccountId != -1 && contacts != null) {
            try {
                IImConnection conn = mApp.getConnection(mProviderId, mAccountId);

                if (conn == null || conn.getState() != ImConnection.LOGGED_IN)
                    return -1L;

                for (Contact contact : contacts) {

                    IChatSession session = conn.getChatSessionManager().getChatSession(contact.getAddress().getAddress());

                    //always need to recreate the MUC after login
////                   if (mContactType == Imps.Contacts.TYPE_GROUP)
 //                     session = conn.getChatSessionManager().createMultiUserChatSession(contact.getAddress().getAddress(), contact.getName(), null, false);

                    if (session == null)
                    {
                        if (mContactType == Imps.Contacts.TYPE_GROUP)
                            session = conn.getChatSessionManager().createMultiUserChatSession(contact.getAddress().getAddress(), contact.getName(), null, false);
                        else {
                            session = conn.getChatSessionManager().createChatSession(contact.getAddress().getAddress(), false);
                        }

                    }
                    else if (session.isGroupChatSession())
                    {

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

    protected void onPostExecute(Long chatId) {


    }

    private int getOtrPolicy() {

        int otrPolicy = OtrPolicy.OPPORTUNISTIC;

        String otrModeSelect = Preferences.getOtrMode();

        if (otrModeSelect.equals("auto")) {
            otrPolicy = OtrPolicy.OPPORTUNISTIC;
        } else if (otrModeSelect.equals("disabled")) {
            otrPolicy = OtrPolicy.NEVER;

        } else if (otrModeSelect.equals("force")) {
            otrPolicy = OtrPolicy.OTRL_POLICY_ALWAYS;

        } else if (otrModeSelect.equals("requested")) {
            otrPolicy = OtrPolicy.OTRL_POLICY_MANUAL;
        }

        return otrPolicy;
    }

}
