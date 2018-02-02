package net.wrappy.im.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.google.gson.JsonObject;

import net.wrappy.im.ImApp;
import net.wrappy.im.R;
import net.wrappy.im.helper.AppFuncs;
import net.wrappy.im.helper.RestAPI;
import net.wrappy.im.helper.RestAPIListener;
import net.wrappy.im.helper.layout.AppEditTextView;
import net.wrappy.im.provider.Store;
import net.wrappy.im.util.PopupUtils;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by ben on 19/01/2018.
 */

public class ReferralActivity extends BaseActivity {

    @BindView(R.id.edReferralCode)
    AppEditTextView edReferralCode;

    public static void start() {
        Intent intent = new Intent(ImApp.sImApp, ReferralActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ImApp.sImApp.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.referral_activity);
        super.onCreate(savedInstanceState);
        initActionBarDefault(false, R.string.Referral);
        Store.putBooleanData(getApplicationContext(), Store.REFERRAL, true);
    }

    @OnClick(R.id.btnReferralCheck)
    public void onClick(View view) {
        String referral = edReferralCode.getText().toString().trim();
        AppFuncs.dismissKeyboard(this);
        if (!TextUtils.isEmpty(referral)) {
            AppFuncs.showProgressWaiting(this);
            RestAPI.PutDataWrappy(this, new JsonObject(), String.format(RestAPI.REFERRAL, referral), new RestAPIListener(this) {
                @Override
                protected void OnComplete(String s) {
                    Store.putBooleanData(getApplicationContext(), Store.REFERRAL, false);
                    finish();
                }
            });
        } else {
            PopupUtils.showCustomDialog(this, getString(R.string.warning), getString(R.string.skip_referral), R.string.no, R.string.yes, null, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Store.putBooleanData(getApplicationContext(), Store.REFERRAL, false);
                    finish();
                }
            });
        }
    }

    @Override
    public void onBackPressed() {

    }
}
