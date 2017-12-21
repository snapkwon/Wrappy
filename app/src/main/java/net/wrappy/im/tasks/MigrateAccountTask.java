package net.wrappy.im.tasks;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import net.wrappy.im.ImApp;
import net.wrappy.im.R;
import net.wrappy.im.crypto.otr.OtrAndroidKeyManagerImpl;
import net.wrappy.im.model.ImConnection;
import net.wrappy.im.model.ImErrorInfo;
import net.wrappy.im.provider.Imps;
import net.wrappy.im.service.IChatSession;
import net.wrappy.im.service.IChatSessionManager;
import net.wrappy.im.service.IContactList;
import net.wrappy.im.service.IContactListManager;
import net.wrappy.im.service.IImConnection;
import net.wrappy.im.ui.legacy.SignInHelper;
import net.wrappy.im.ui.onboarding.OnboardingAccount;
import net.wrappy.im.ui.onboarding.OnboardingManager;

import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by n8fr8 on 5/1/17.
 */

public class MigrateAccountTask extends AsyncTask<String, Void, OnboardingAccount> {

    Activity mContext;
    IImConnection mConn;
    long mAccountId;
    long mProviderId;
    ImApp mApp;
    IImConnection mNewConn;
    OnboardingAccount mNewAccount;

    MigrateAccountListener mListener;

    Handler mHandler = new Handler();

    ArrayList<String> mContacts;
    private WeakReference<Activity> weakReference;

    public MigrateAccountTask(Activity context, ImApp app, long providerId, long accountId, MigrateAccountListener listener) {
        mContext = context;
        mAccountId = accountId;
        mProviderId = providerId;
        mApp = app;

        mListener = listener;

        mConn = app.getConnection(providerId, accountId);

        mContacts = new ArrayList<>();
        weakReference = new WeakReference<>(mContext);
    }

    @Override
    protected OnboardingAccount doInBackground(String... newDomains) {

        //get existing account username
        String nickname = Imps.Account.getNickname(mContext.getContentResolver(), mAccountId);
        String username = Imps.Account.getUserName(mContext.getContentResolver(), mAccountId);
        String password = Imps.Account.getPassword(mContext.getContentResolver(), mAccountId);

        OtrAndroidKeyManagerImpl keyMan = OtrAndroidKeyManagerImpl.getInstance(mContext);
        KeyPair keyPair = keyMan.generateLocalKeyPair();
        String fingerprint = keyMan.getFingerprint(keyPair.getPublic());

        //find or use provided new server/domain
        String domain = newDomains[0];

        //register account on new domain with same password
        mNewAccount = registerNewAccount(nickname, username, password, domain, null);

        if (mNewAccount == null) {

            username = username + '.' + fingerprint.substring(fingerprint.length() - 8, fingerprint.length()).toLowerCase();
            mNewAccount = registerNewAccount(nickname, username, password, domain, null);

            if (mNewAccount == null)
                return null;
        }

        String newJabberId = mNewAccount.username + '@' + mNewAccount.domain;
        keyMan.storeKeyPair(newJabberId, keyPair);

        //send migration message to existing contacts and/or sessions
        try {

            boolean loggedInToOldAccount = mConn.getState() == ImConnection.LOGGED_IN;

            //login and set new default account
            SignInHelper signInHelper = new SignInHelper(mContext, mHandler);
            signInHelper.activateAccount(mNewAccount.providerId, mNewAccount.accountId);
            signInHelper.signIn(mNewAccount.password, mNewAccount.providerId, mNewAccount.accountId, true);

            mNewConn = mApp.getConnection(mNewAccount.providerId, mNewAccount.accountId);

            while (mNewConn.getState() != ImConnection.LOGGED_IN) {
                try {
                    Thread.sleep(500);
                } catch (Exception e) {
                }
            }

            String inviteLink = OnboardingManager.generateInviteLink(mContext, newJabberId, fingerprint, nickname, true);

            String migrateMessage = mContext.getString(R.string.migrate_message) + ' ' + inviteLink;
            IChatSessionManager sessionMgr = mConn.getChatSessionManager();
            IContactListManager clManager = mConn.getContactListManager();
            List<IContactList> listOfLists = clManager.getContactLists();

            if (loggedInToOldAccount) {

                for (IContactList contactList : listOfLists) {
                    String[] contacts = contactList.getContacts();

                    for (String contact : contacts) {
                        mContacts.add(contact);

                        IChatSession session = sessionMgr.getChatSession(contact);

                        if (session == null) {
                            session = sessionMgr.createChatSession(contact, true);
                        }

                        if (!session.isEncrypted()) {
                            //try to kick off some encryption here
                            session.getDefaultOtrChatSession().startChatEncryption();
                            try {
                                Thread.sleep(500);
                            } //just wait a half second here?
                            catch (Exception e) {
                            }
                        }

                        session.sendMessage(migrateMessage, false);

                        //archive existing contact
                        clManager.hideContact(contact, true);
                    }

                }
            } else {
                String[] offlineAddresses = clManager.getOfflineAddresses();

                for (String address : offlineAddresses) {
                    mContacts.add(address);
                    clManager.hideContact(address, true);
                }
            }

            for (String contact : mContacts) {
                addToContactList(mNewConn, contact, keyMan.getRemoteFingerprint(contact), null);
            }

            if (loggedInToOldAccount) {
                //archive existing conversations and contacts
                List<IChatSession> listSession = mConn.getChatSessionManager().getActiveChatSessions();
                for (IChatSession session : listSession) {
                    session.leave();
                }
                mConn.broadcastMigrationIdentity(newJabberId);
            }

            mApp.setDefaultAccount(mNewAccount.providerId, mNewAccount.accountId);

            //logout of existing account
            setKeepSignedIn(mAccountId, false);

            if (loggedInToOldAccount)
                mConn.logout();

            return mNewAccount;

        } catch (Exception e) {
            Log.e(ImApp.LOG_TAG, "error with migration", e);
        }

        //failed
        return null;
    }

    @Override
    protected void onPostExecute(OnboardingAccount account) {
        super.onPostExecute(account);

        if (weakReference.get() != null) {
            if (account == null) {
                if (mListener != null)
                    mListener.migrateFailed(mProviderId, mAccountId);
            } else {
                if (mListener != null)
                    mListener.migrateComplete(account);
            }
        }
    }

    private OnboardingAccount registerNewAccount(String nickname, String username, String password, String domain, String server) {
        try {
            OnboardingAccount result = OnboardingManager.registerAccount(mContext, nickname, username, password, domain, server, 5222);
            return result;
        } catch (JSONException jse) {

        }

        return null;
    }

    private int addToContactList(IImConnection conn, String address, String otrFingperint, String nickname) {
        int res = -1;

        try {

            IContactList list = getContactList(conn);

            if (list != null) {

                res = list.addContact(address, nickname);
                if (res != ImErrorInfo.NO_ERROR) {

                    //what to do here?
                }

                if (!TextUtils.isEmpty(otrFingperint)) {
                    OtrAndroidKeyManagerImpl.getInstance(mApp).verifyUser(address, otrFingperint);
                }

                //Contact contact = new Contact(new XmppAddress(address),address);
                //IContactListManager contactListMgr = conn.getContactListManager();
                //contactListMgr.approveSubscription(contact);
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

    public interface MigrateAccountListener {
        void migrateComplete(OnboardingAccount account);

        void migrateFailed(long providerId, long accountId);
    }

    private void setKeepSignedIn(final long accountId, boolean signin) {
        Uri mAccountUri = ContentUris.withAppendedId(Imps.Account.CONTENT_URI, accountId);
        ContentValues values = new ContentValues();
        values.put(Imps.Account.KEEP_SIGNED_IN, signin);
        mContext.getContentResolver().update(mAccountUri, values, null, null);
    }
}
