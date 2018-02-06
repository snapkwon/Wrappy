package net.wrappy.im.tasks;

import android.os.AsyncTask;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import net.wrappy.im.ImApp;
import net.wrappy.im.crypto.otr.OtrAndroidKeyManagerImpl;
import net.wrappy.im.model.ImErrorInfo;
import net.wrappy.im.service.IContactList;
import net.wrappy.im.service.IContactListManager;
import net.wrappy.im.service.IImConnection;

import java.util.List;

/**
 * Created by n8fr8 on 6/9/15.
 */
public class AddContactAsyncTask extends AsyncTask<String, Void, Integer> {

    private long mProviderId;
    private long mAccountId;
    private AddContactCallback listenner;

    public AddContactAsyncTask(long providerId, long accountId) {
        mProviderId = providerId;
        mAccountId = accountId;
    }

    public AddContactAsyncTask setCallback(AddContactCallback listenner) {
        this.listenner = listenner;
        return this;
    }

    @Override
    public Integer doInBackground(String... strings) {

        String address = strings[0];
        String fingerprint = strings[1];
        String nickname = null;

        if (strings.length > 2)
            nickname = strings[2];

        return addToContactList(address, fingerprint, nickname);
    }

    @Override
    protected void onPostExecute(Integer response) {
        super.onPostExecute(response);
        if (listenner != null) {
            if (response == ImErrorInfo.NO_ERROR)
                listenner.onFinished(response);
            else
                listenner.onError();
        }
    }

    private int addToContactList(String address, String otrFingperint, String nickname) {
        int res = -1;

        try {
            IImConnection conn = ImApp.getConnection(mProviderId, mAccountId);
            if (conn == null)
                conn = ImApp.createConnection(mProviderId, mAccountId);

            IContactList list = getContactList(conn);

            if (list != null) {

                res = list.addContact(address, nickname);
//                if (res != ImErrorInfo.NO_ERROR) {
//
//                    //what to do here?
//                }

                if (!TextUtils.isEmpty(otrFingperint)) {
                    OtrAndroidKeyManagerImpl.getInstance(ImApp.sImApp).verifyUser(address, otrFingperint);
                }

            }

        } catch (RemoteException re) {
            Log.e(ImApp.LOG_TAG, "error adding contact", re);
        }

        return res;
    }

    private IContactList getContactList(IImConnection conn) {
        if (conn == null) {
            return null;
        }

        try {
            IContactListManager contactListMgr = conn.getContactListManager();

            // Use the default list
            List<IBinder> lists = contactListMgr.getContactLists();
            for (IBinder binder : lists) {
                IContactList list = IContactList.Stub.asInterface(binder);
                if (list.isDefault()) {
                    return list;
                }
            }

            // No default list, use the first one as default list
            if (!lists.isEmpty()) {
                return IContactList.Stub.asInterface(lists.get(0));
            }

            return null;

        } catch (RemoteException e) {
            // If the service has died, there is no list for now.
            return null;
        }
    }

    public interface AddContactCallback {
        void onFinished(Integer code);

        void onError();
    }
}
