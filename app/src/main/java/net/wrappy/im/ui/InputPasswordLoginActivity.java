package net.wrappy.im.ui;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.wrappy.im.MainActivity;
import net.wrappy.im.R;
import net.wrappy.im.helper.AppFuncs;
import net.wrappy.im.helper.ErrorCode;
import net.wrappy.im.helper.LoginTask;
import net.wrappy.im.helper.RestAPI;
import net.wrappy.im.helper.RestAPIListener;
import net.wrappy.im.model.RegistrationAccount;
import net.wrappy.im.model.WpkToken;
import net.wrappy.im.provider.Store;
import net.wrappy.im.ui.onboarding.OnboardingAccount;
import net.wrappy.im.util.Constant;
import net.wrappy.im.util.PopupUtils;

import javax.annotation.Nullable;

import butterknife.BindView;
import butterknife.ButterKnife;

public class InputPasswordLoginActivity extends BaseActivity {



    TextView mBtnForgetPass;
    WpkToken wpkToken;
    Button btnLogin;
    EditText edtPassword;
    TextView mCountdownText;

    String userName;
    CountDownTimer cTimer = null;

    private long lastClickTime = 0;

    void startTimer() {
        cTimer = new CountDownTimer(AppFuncs.TIMECOUNTDOWN, 1000) {
            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000);
                //int minutes = seconds / 60;
                seconds = seconds % 60;
                mCountdownText.setText(String.format(InputPasswordLoginActivity.this.getString(R.string.passcode_coutdown), String.format("%02d", seconds)));
            }
            public void onFinish() {
                cancelTimer();
                setResult(RESULT_OK, null);
                finish();
            }
        };
        cTimer.start();
    }

    //cancel timer
    void cancelTimer() {
        if(cTimer!=null)
            cTimer.cancel();
    }

    //resume timer
    void resume() {
        if(cTimer!=null)
            cTimer.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AppFuncs.dismissProgressWaiting();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_password_login);

        wpkToken = getIntent().getParcelableExtra(PatternActivity.USER_INFO);
        initViews();

        startTimer();

        userName = getIntent().getStringExtra("username");

        showHidePassword(edtPassword, true);

        mBtnForgetPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ForgetPasswordActivity.start(InputPasswordLoginActivity.this);
                finish();
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (SystemClock.elapsedRealtime() - lastClickTime < 1000){
                    return;
                }
                lastClickTime = SystemClock.elapsedRealtime();
                if(edtPassword.getText().toString().isEmpty())
                {
                    PopupUtils.showCustomDialog(InputPasswordLoginActivity.this,getString(R.string.warning),getString(R.string.please_input_the_password), R.string.ok, null);
                }
                else {
                    AppFuncs.showProgressWaiting(InputPasswordLoginActivity.this);
                    RestAPI.PostDataWrappy(InputPasswordLoginActivity.this, null, String.format(RestAPI.CHECK_PASSCODE, edtPassword.getText().toString()), new RestAPIListener(InputPasswordLoginActivity.this) {

                        @Override
                        public void onError(int errorCode) {
                            int resId = getResId("error_" + errorCode);
                            getIntent().putExtra("error", InputPasswordLoginActivity.this.getString(resId));
                            setResult(RESULT_OK, getIntent());
                            finish();
                        }

                        @Override
                        public void OnComplete(String s) {
                            try {
                                boolean ischeck = (new JsonParser()).parse(s).getAsBoolean();
                                if (ischeck == true) {
                                    doExistingAccountRegister(wpkToken.getJid() + Constant.EMAIL_DOMAIN, wpkToken.getXmppPassword(), userName);
                                } else {
                                    onLoginFailed();
                                }

                            } catch (Exception ex) {
                                AppFuncs.dismissProgressWaiting();
                                ex.printStackTrace();
                            }
                        }
                    });
                }
             //   doExistingAccountRegister(wpkToken.getJid() + Constant.EMAIL_DOMAIN, wpkToken.getXmppPassword(), userName);
            }
        });

    }

    private void onLoginFailed() {
        PopupUtils.showCustomDialog(this, getString(R.string.error), getString(R.string.network_error), R.string.yes, null, false);
        finish();
    }

    private void doExistingAccountRegister(String username, String password, String accountName) {
        RegistrationAccount account = new RegistrationAccount(username, password);
        account.setNickname(accountName);

        new LoginTask(this, new LoginTask.EventListenner() {
            @Override
            public void OnComplete(boolean isSuccess, OnboardingAccount onboardingAccount) {
                AppFuncs.dismissProgressWaiting();
                if (!isSuccess) {
                    onLoginFailed();
                } else {
                    AppFuncs.getSyncUserInfo(onboardingAccount.accountId);
                    MainActivity.start();
                    finish();
                }
            }
        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, account);
    }

    private void initViews() {
        initActionBarDefault(true, R.string.login);

        mBtnForgetPass = (TextView)this.findViewById(R.id.btnforgetpass);

        btnLogin = (Button)this.findViewById(R.id.btnlogin);

        edtPassword = (EditText)this.findViewById(R.id.edtpassword);

        mCountdownText = (TextView)this.findViewById(R.id.tvCountdownTime);

    }

}
