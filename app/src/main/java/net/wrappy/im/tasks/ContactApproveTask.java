package net.wrappy.im.tasks;

import android.os.AsyncTask;
import android.os.RemoteException;

import net.wrappy.im.ImApp;
import net.wrappy.im.model.Contact;
import net.wrappy.im.model.ImConnection;
import net.wrappy.im.model.WpKChatRoster;
import net.wrappy.im.plugin.xmpp.XmppAddress;
import net.wrappy.im.provider.Imps;
import net.wrappy.im.service.IContactListManager;
import net.wrappy.im.service.IImConnection;
import net.wrappy.im.util.Constant;

/**
 * Created by Khoa.Nguyen on 10/23/15.
 */
public class ContactApproveTask extends AsyncTask<WpKChatRoster[], Long, String> {

    @Override
    protected String doInBackground(WpKChatRoster[]... params) {
        WpKChatRoster[] rosters = params[0];
        try {
            IImConnection connection = ImApp.getConnection(ImApp.sImApp.getDefaultProviderId(), ImApp.sImApp.getDefaultProviderId());
            if (connection != null && connection.getState() == ImConnection.LOGGED_IN) {
                for (WpKChatRoster roster : rosters) {
                    IContactListManager manager = connection.getContactListManager();
                    String address = roster.getContact().getXMPPAuthDto().getAccount() + Constant.EMAIL_DOMAIN;
                    ImApp.updateContact(address, roster.getContact(), connection);
                    Contact contact = new Contact(new XmppAddress(address), roster.getContact().getIdentifier(), Imps.Contacts.TYPE_NORMAL);
                    manager.approveSubscription(contact);
                }
            }
            return null;

        } catch (RemoteException e) {
            return e.toString();
        }
    }
}
