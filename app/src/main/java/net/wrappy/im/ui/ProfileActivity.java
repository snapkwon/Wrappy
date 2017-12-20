package net.wrappy.im.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.WindowManager;

import net.wrappy.im.R;
import net.wrappy.im.helper.layout.AppTextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by ben on 14/12/2017.
 */

public class ProfileActivity extends FragmentActivity {




    @BindView(R.id.headerbarTitle) AppTextView headerbarTitle;

    long mContactId = -1;
    String mNickname = null;
    String reference = "";
    String jid = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_activity);
        ButterKnife.bind(this);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        jid = getIntent().getStringExtra("address");
        String[] arr = jid.split("@");
        jid = arr[0];
        mContactId = getIntent().getLongExtra("contactId", -1);
        mNickname = getIntent().getStringExtra("nickname");
        reference = getIntent().getStringExtra("reference");
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
}
