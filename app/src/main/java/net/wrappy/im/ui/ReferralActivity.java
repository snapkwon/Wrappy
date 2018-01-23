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
import net.wrappy.im.helper.RestAPIListenner;
import net.wrappy.im.helper.layout.AppEditTextView;
import net.wrappy.im.util.PopupUtils;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by ben on 19/01/2018.
 */

public class ReferralActivity extends BaseActivity {

    @BindView(R.id.edReferralCode) AppEditTextView edReferralCode;

    AppFuncs appFuncs;

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
        appFuncs = AppFuncs.getInstance();
    }

    @OnClick(R.id.btnReferralCheck)
    public void onClick(View view) {
        String referral = edReferralCode.getText().toString().trim();
        if (!TextUtils.isEmpty(referral)) {
            appFuncs.showProgressWaiting(this);
            RestAPI.PutDataWrappy(this, new JsonObject(), String.format(RestAPI.REFERRAL, referral), new RestAPIListenner() {
                @Override
                protected void OnComplete(int httpCode, String error, String s) {
                    appFuncs.dismissProgressWaiting();
                    if (RestAPI.checkHttpCode(httpCode)) {
                        MainActivity.start();
                    } else {
                        PopupUtils.showCustomDialog(ReferralActivity.this, getString(R.string.error), getString(R.string.input_referral_error), R.string.cancel, null);
                    }

                }
            });
        } else {
            MainActivity.start();
        }
    }
}
