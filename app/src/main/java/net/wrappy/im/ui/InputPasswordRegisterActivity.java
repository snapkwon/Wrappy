package net.wrappy.im.ui;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.JsonParser;

import net.wrappy.im.MainActivity;
import net.wrappy.im.R;
import net.wrappy.im.helper.AppFuncs;
import net.wrappy.im.helper.LoginTask;
import net.wrappy.im.helper.RestAPI;
import net.wrappy.im.helper.RestAPIListener;
import net.wrappy.im.model.RegistrationAccount;
import net.wrappy.im.model.WpKAuthDto;
import net.wrappy.im.model.WpkToken;
import net.wrappy.im.ui.onboarding.OnboardingAccount;
import net.wrappy.im.util.Constant;
import net.wrappy.im.util.PopupUtils;

import javax.annotation.Nullable;

import butterknife.BindView;
import butterknife.ButterKnife;

public class InputPasswordRegisterActivity extends BaseActivity {


    @Nullable
    @BindView(R.id.tile_input_password_page)
    TextView mTitlePage;
    @Nullable
    @BindView(R.id.title_password)
    TextView mTitlePassword;
    @Nullable
    @BindView(R.id.edtpassword)
    EditText mEditPassword;
    @Nullable
    @BindView(R.id.edtconfirmpassword)
    EditText mEditConfirmPassword;
    @Nullable
    @BindView(R.id.btcreatepassword)
    Button mBtnLogin;

    private String patternPassword;
    WpkToken wpkToken;
    String userName;
    private long lastClickTime = 0;

    private void showQuestionScreen(String pass ,String passcode) {
            Intent intent = new Intent(this, RegistrationSecurityQuestionActivity.class);
            WpKAuthDto wpKAuthDto = new WpKAuthDto(pass,passcode);
            wpKAuthDto.setPasscode(passcode);
            intent.putExtra(UpdateProfileActivity.PASSCODE,passcode);
            intent.putExtra(WpKAuthDto.class.getName(), wpKAuthDto);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_password_register);

        patternPassword = getIntent().getStringExtra(PatternActivity.PASSWORD_INPUT);
        wpkToken = getIntent().getParcelableExtra(PatternActivity.USER_INFO);
        userName = getIntent().getStringExtra("username");
        ButterKnife.bind(this);
        initViews();

        mBtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = mEditPassword.getText().toString();
                if (SystemClock.elapsedRealtime() - lastClickTime < 1000){
                    return;
                }
                lastClickTime = SystemClock.elapsedRealtime();
                if(password.isEmpty())
                {
                    PopupUtils.showCustomDialog(InputPasswordRegisterActivity.this,getString(R.string.warning),getString(R.string.please_input_the_password), R.string.ok, null);
                }
                else
                {
                    if(passwordValidation(password)==true) {
                        //passwordValidation(mEditPassword.getText().toString());
                        if(password.equals(mEditConfirmPassword.getText().toString()))
                        {
                            if(wpkToken==null) {
                                showQuestionScreen(patternPassword, password);
                            }
                            else
                            {

                                RestAPI.PostDataWrappy(InputPasswordRegisterActivity.this, null, String.format(RestAPI.CREATE_PASSCODE, password), new RestAPIListener(InputPasswordRegisterActivity.this) {

                                    @Override
                                    public void onError(int errorCode)
                                    {
                                        int resId = getResId("error_" + errorCode);
                                        getIntent().putExtra("error", InputPasswordRegisterActivity.this.getString(resId));
                                        setResult(RESULT_OK, getIntent());
                                        finish();
                                    }


                                    @Override
                                    public void OnComplete(String s) {
                                        try {
                                            AppFuncs.showProgressWaiting(InputPasswordRegisterActivity.this);
                                            doExistingAccountRegister(wpkToken.getJid() + Constant.EMAIL_DOMAIN, wpkToken.getXmppPassword(), userName);
                                        } catch (Exception ex) {
                                            AppFuncs.dismissProgressWaiting();
                                            ex.printStackTrace();
                                        }
                                    }
                                });
                            }
                        }
                        else
                        {
                            PopupUtils.showCustomDialog(InputPasswordRegisterActivity.this,getString(R.string.warning),getString(R.string.lock_screen_passphrases_not_matching), R.string.ok, null);
                        }
                    }
                    else
                    {
                        PopupUtils.showCustomDialog(InputPasswordRegisterActivity.this,getString(R.string.warning),getString(R.string.wrong_format_password), R.string.ok, null);
                    }
                }
            }
        });
    }



    private void onLoginFailed() {
        PopupUtils.showCustomDialog(this, getString(R.string.error), getString(R.string.network_error), R.string.yes, null, false);
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

    boolean passwordValidation (String password){
        String pattern = "(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}";
        return password.matches(pattern);
    }

    private void initViews() {

        if(getIntent().getBooleanExtra("hasPasscode",true) == true )
        {
            mTitlePage.setText(R.string.create_new_password);
            initActionBarDefault(true, R.string.registration);
        }
        else
        {
            initActionBarDefault(true, R.string.update_password);
            mTitlePage.setText(R.string.update_password_title);
        }
        mTitlePassword.setText(R.string.create_new_password);
        mEditPassword.setHint(R.string.input_your_password);
        mEditConfirmPassword.setHint(R.string.input_repeat_password);
        mBtnLogin.setText(R.string.btn_next);

        mEditPassword.setText("");
        mEditConfirmPassword.setText("");

        showHidePassword(mEditPassword, false);
        showHidePassword(mEditConfirmPassword, false);
    }
}
