package net.wrappy.im.ui;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import net.wrappy.im.ImApp;
import net.wrappy.im.MainActivity;
import net.wrappy.im.R;
import net.wrappy.im.crypto.IOtrChatSession;
import net.wrappy.im.crypto.otr.OtrChatManager;
import net.wrappy.im.model.Contact;
import net.wrappy.im.model.ImErrorInfo;
import net.wrappy.im.plugin.xmpp.XmppAddress;
import net.wrappy.im.provider.Imps;
import net.wrappy.im.service.IChatSession;
import net.wrappy.im.service.IChatSessionManager;
import net.wrappy.im.service.IContactListManager;
import net.wrappy.im.service.IImConnection;
import net.wrappy.im.tasks.ChatSessionInitTask;
import net.wrappy.im.ui.legacy.DatabaseUtils;
import net.wrappy.im.util.PopupUtils;

import java.util.List;


public class ContactDisplayActivity extends BaseActivity {

    private long mContactId = -1;
    private String mNickname = null;
    private String mUsername = null;
    private long mProviderId = -1;
    private long mAccountId = -1;
    private IImConnection mConn;

    private String mRemoteOtrFingerprint;
    private List<String> mRemoteOmemoFingerprints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.awesome_activity_contact);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mContactId = getIntent().getLongExtra("contactId", -1);

        mNickname = getIntent().getStringExtra("nickname");
        mUsername = getIntent().getStringExtra("address");
        mProviderId = getIntent().getLongExtra("provider", -1);
        mAccountId = getIntent().getLongExtra("account", -1);
        String email = ImApp.getEmail(mUsername);

        String remoteFingerprint = getIntent().getStringExtra("fingerprint");
        try {// TungNP: finish activity to avoid crash
            mConn = ((ImApp) getApplication()).getConnection(mProviderId, mAccountId);
        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }

        if (TextUtils.isEmpty(mNickname)) {
            mNickname = mUsername;
            String[] splitNickname = mNickname.split("@")[0].split("\\.");
            if (splitNickname.length > 0)
                mNickname = splitNickname[0];
        }
        setTitle("");

        TextView tv = (TextView) findViewById(R.id.tvNickname);
        tv.setText(mNickname);
        tv = (TextView) findViewById(R.id.tvUsername);
        tv.setText(email);

        if (!TextUtils.isEmpty(mUsername)) {
            try {
                Drawable avatar = DatabaseUtils.getAvatarFromAddress(getContentResolver(), mUsername, ImApp.DEFAULT_AVATAR_WIDTH, ImApp.DEFAULT_AVATAR_HEIGHT, false);
                if (avatar != null) {
                    ImageView iv = (ImageView) findViewById(R.id.imageAvatar);
                    iv.setImageDrawable(avatar);
                    iv.setVisibility(View.VISIBLE);
                    findViewById(R.id.imageSpacer).setVisibility(View.GONE);
                }
            } catch (Exception e) {
            }
        }

        View btn = findViewById(R.id.btnStartChat);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startChat();
            }
        });

        if (mConn != null) {
            new AsyncTask<String, Void, Boolean>() {
                @Override
                protected Boolean doInBackground(String... strings) {
                    mRemoteOtrFingerprint = strings[0];

                    if (mRemoteOtrFingerprint == null) {
                        mRemoteOtrFingerprint = OtrChatManager.getInstance().getRemoteKeyFingerprint(mUsername);
                    }

                    try {
                        mRemoteOmemoFingerprints = mConn.getFingerprints(mUsername);
                    } catch (RemoteException re) {

                    }
                    return true;
                }
            }.execute(remoteFingerprint);
        }

    }

    public void verifyClicked(View view) {
        verifyRemoteFingerprint();
        findViewById(R.id.btnVerify).setVisibility(View.GONE);
    }

    private void showGallery(int contactId) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        GalleryListFragment fragment = new GalleryListFragment();
        Bundle args = new Bundle();
        args.putInt("contactId", contactId);
        fragment.setArguments(args);
        fragmentTransaction.add(R.id.fragment_container, fragment, "MyActivity");
        fragmentTransaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_contact_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.menu_verify_or_view:
                verifyRemoteFingerprint();
                return true;
            /**
             case R.id.menu_verify_question:
             initSmpUI();
             return true;**/
            case R.id.menu_remove_contact:
                deleteContact();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private String prettyPrintFingerprint(String fingerprint) {
        StringBuffer spacedFingerprint = new StringBuffer();

        for (int i = 0; i + 8 <= fingerprint.length(); i += 8) {
            spacedFingerprint.append(fingerprint.subSequence(i, i + 8));
            spacedFingerprint.append(' ');
        }

        return spacedFingerprint.toString();
    }

    void deleteContact() {
        PopupUtils.showCustomDialog(this, getString(R.string.menu_remove_contact), getString(R.string.confirm_delete_contact, mNickname), R.string.yes, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doDeleteContact();
                finish();
                startActivity(new Intent(ContactDisplayActivity.this, MainActivity.class));
            }
        }, false);
    }

    void doDeleteContact() {
        try {
            IImConnection mConn;
            mConn = ((ImApp) getApplication()).getConnection(mProviderId, mAccountId);

            IContactListManager manager = mConn.getContactListManager();

            int res = manager.removeContact(mUsername);
            if (res != ImErrorInfo.NO_ERROR) {
                //mHandler.showAlert(R.string.error,
                //      ErrorResUtils.getErrorRes(getResources(), res, address));
            }
        } catch (RemoteException re) {

        }
    }

    public void startChat() {
        if (mConn == null || mContactId == -1)
            return;

        try {
            IChatSessionManager manager = mConn.getChatSessionManager();
            if (manager != null) {
                IChatSession session = manager.getChatSession(mUsername);
                if (session == null) {
                    new ChatSessionInitTask(this, mProviderId, mAccountId, Imps.Contacts.TYPE_NORMAL)
                            .executeOnExecutor(ImApp.sThreadPoolExecutor, new Contact(new XmppAddress(mUsername)));
                    Toast.makeText(this, getString(R.string.message_waiting_for_friend), Toast.LENGTH_LONG).show();
                }
            }
        } catch (RemoteException re) {
        }

        Intent intent = ConversationDetailActivity.getStartIntent(this);
        intent.putExtra("id", mContactId);
        startActivity(intent);
        finish();
    }

    private void initSmp(String question, String answer) {
        try {
            IChatSessionManager manager = mConn.getChatSessionManager();
            IChatSession session = manager.getChatSession(mUsername);
            IOtrChatSession iOtrSession = session.getDefaultOtrChatSession();
            iOtrSession.initSmpVerification(question, answer);

        } catch (RemoteException e) {
            Log.e(ImApp.LOG_TAG, "error init SMP", e);
        }
    }

    private void verifyRemoteFingerprint() {
        new AsyncTask<String, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(String... strings) {


                try {
                    if (mConn != null) {
                        IContactListManager listManager = mConn.getContactListManager();

                        if (listManager != null)
                            listManager.approveSubscription(new Contact(new XmppAddress(mUsername), mNickname, Imps.Contacts.TYPE_NORMAL));

                        IChatSessionManager manager = mConn.getChatSessionManager();

                        if (manager != null) {
                            IChatSession session = manager.getChatSession(mUsername);

                            if (session != null) {

                                IOtrChatSession otrChatSession = session.getDefaultOtrChatSession();

                                if (otrChatSession != null) {
                                    otrChatSession.verifyKey(otrChatSession.getRemoteUserId());
                                    Snackbar.make(findViewById(R.id.main_content), getString(R.string.action_verified), Snackbar.LENGTH_LONG).show();
                                }
                            }
                        }
                    }
                } catch (RemoteException e) {
                    Log.e(ImApp.LOG_TAG, "error init otr", e);
                }
                return true;
            }
        }.execute();
    }
}
