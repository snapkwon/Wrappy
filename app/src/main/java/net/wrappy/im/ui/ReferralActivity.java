package net.wrappy.im.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.google.gson.JsonObject;

import net.wrappy.im.ImApp;
import net.wrappy.im.MainActivity;
import net.wrappy.im.R;
import net.wrappy.im.helper.AppFuncs;
import net.wrappy.im.helper.RestAPI;
import net.wrappy.im.helper.RestAPIListener;
import net.wrappy.im.helper.layout.AppEditTextView;

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
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        ImApp.sImApp.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.referral_activity);
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle(getString(R.string.Referral));
    }

    @OnClick(R.id.btnReferralCheck)
    public void onClick(View view) {
        String referral = edReferralCode.getText().toString().trim();
        if (!TextUtils.isEmpty(referral)) {
            AppFuncs.showProgressWaiting(this);
            RestAPI.PutDataWrappy(this, new JsonObject(), String.format(RestAPI.REFERRAL, referral), new RestAPIListener(this) {
                @Override
                protected void OnComplete(int httpCode, String error, String s) {
                    MainActivity.start();
                }
            });
        } else {
            MainActivity.start();
        }
    }
}
