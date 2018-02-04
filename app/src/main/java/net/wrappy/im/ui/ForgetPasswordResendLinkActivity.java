package net.wrappy.im.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import net.wrappy.im.R;
import net.wrappy.im.helper.AppDelegate;
import net.wrappy.im.helper.AppFuncs;
import net.wrappy.im.helper.RestAPI;
import net.wrappy.im.helper.RestAPIListener;
import net.wrappy.im.helper.layout.AppButton;
import net.wrappy.im.provider.Store;

import javax.annotation.Nullable;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;


/**
 * Created by CuongDuong on 2/2/2018.
 */

public class ForgetPasswordResendLinkActivity extends BaseActivity{

    public static final String EMAIL_KEY = "email";

    private String email = null;

    @Nullable
    @BindView(R.id.btn_back_to_login)
    AppButton mBackLoginBtn;
    @Nullable
    @BindView(R.id.btn_resend_link)
    AppButton mResendBtn;

    public static void start(Activity activity, String email) {
        Intent intent = new Intent(activity, ForgetPasswordResendLinkActivity.class);
        intent.putExtra(EMAIL_KEY, email);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forget_password_resend_link);
        ButterKnife.bind(this);

        initActionBarDefault(false, R.string.forget_password);

        if (getIntent() != null) {
            email = getIntent().getStringExtra(EMAIL_KEY);
        }

    }
    @Optional
    @OnClick({R.id.btn_back_to_login, R.id.btn_resend_link})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_back_to_login:
                LauncherActivity.start(ForgetPasswordResendLinkActivity.this);
                finish();
                break;
            case R.id.btn_resend_link:
                AppFuncs.showProgressWaiting(this);
                RestAPI.GetDataWrappy(this, RestAPI.sendEmailAndUsernameToGetPassUrl(Store.getStringData(this, Store.USERNAME), email), new RestAPIListener(this) {
                    @Override
                    public void OnComplete(String s) {
                        AppFuncs.dismissProgressWaiting();
                        AppFuncs.alert(getApplicationContext(), getString(R.string.request_send_email_success), true);
                    }
                });
                break;
        }
    }
}
