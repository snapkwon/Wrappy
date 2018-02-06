package net.wrappy.im.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import net.wrappy.im.R;
import net.wrappy.im.helper.AppDelegate;
import net.wrappy.im.util.PopupUtils;

/**
 * Created by ben on 02/01/2018.
 */

public class VerifyEmailOrPhoneActivity extends BaseActivity implements AppDelegate {

    public static void start(Activity activity, Bundle bundle, int requestCode) {
        Intent intent = new Intent(activity, VerifyEmailOrPhoneActivity.class);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        setContentView(R.layout.verify_email_or_phone_activity);
        super.onCreate(savedInstanceState);
        initActionBarDefault(true,R.string.page_verify);
        if (getIntent().getExtras() != null) {
            getFragmentManager().beginTransaction().replace(R.id.frVerifyContainer, VerifyCodeFragment.newInstance(getIntent().getExtras())).commit();
        }
    }

    @Override
    public void onChangeInApp(int id, String data) {

    }

    @Override
    public void onBackPressed() {
        PopupUtils.showCustomDialog(VerifyEmailOrPhoneActivity.this, getString(R.string.warning), getString(R.string.cancel_and_home), R.string.yes, R.string.cancel, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LauncherActivity.start(VerifyEmailOrPhoneActivity.this);
            }
        },null);
    }
}
