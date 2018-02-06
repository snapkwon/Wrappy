package net.wrappy.im.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.WindowManager;

import net.wrappy.im.R;
import net.wrappy.im.helper.NotificationCenter;
import net.wrappy.im.helper.layout.AppTextView;
import net.wrappy.im.provider.Imps;
import net.wrappy.im.util.Constant;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by ben on 14/12/2017.
 */

public class ProfileActivity extends FragmentActivity implements NotificationCenter.NotificationCenterDelegate {

    @BindView(R.id.headerbarTitle) AppTextView headerbarTitle;

    long mContactId = -1;
    String mNickname = null;
    String reference = "";
    String jid = "";
    boolean isRequestNotification;

    public static void start(Activity activity,String address) {
        Intent intent = new Intent(activity, ProfileActivity.class);
        intent.putExtra("address",address);
        activity.startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isRequestNotification) {
            isRequestNotification = true;
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.changeProfileName);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isRequestNotification) {
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.changeProfileName);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_activity);
        ButterKnife.bind(this);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        jid = getIntent().getStringExtra("address");
        if (jid.contains("@")) {
            String[] arr = jid.split("@");
            jid = arr[0];
        }
        //mContactId = getIntent().getLongExtra("contactId", -1);
        mNickname = Imps.Contacts.getNicknameFromAddress(getContentResolver(),jid+Constant.EMAIL_DOMAIN);
        reference = Imps.Avatars.getAvatar(getContentResolver(),jid+ Constant.EMAIL_DOMAIN);
        mContactId = Imps.Contacts.getContactIdFromAddress(getContentResolver(), jid+Constant.EMAIL_DOMAIN);
        headerbarTitle.setText(mNickname);
        getSupportFragmentManager().beginTransaction().replace(R.id.frProfileContainer,ProfileFragment.newInstance(mContactId,mNickname,reference,jid)).commit();
    }
    @OnClick(R.id.headerbarBack)
    public void onClick(View view) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void didReceivedNotification(int id, Object... args) {
        try {
            if (id == NotificationCenter.changeProfileName) {
                String name = (String) args[0];
                headerbarTitle.setText(name);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
