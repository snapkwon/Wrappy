package net.wrappy.im.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
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

public class ForgetPasswordInputNewPassword extends BaseActivity {

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

    String password;
    String username;
    String hashpassword;

    public static void start(Activity activity,String password,String hashResetPassword) {
        Intent intent = new Intent(activity, ForgetPasswordInputNewPassword.class);
        intent.putExtra(PatternActivity.PASSWORD_INPUT,password);
        intent.putExtra(PatternActivity.HASHPASSWORD,hashResetPassword);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_password_register);

        password = getIntent().getStringExtra(PatternActivity.PASSWORD_INPUT);
        username = Store.getStringData(getApplicationContext(), Store.USERNAME);
        hashpassword = getIntent().getStringExtra(PatternActivity.HASHPASSWORD);

        ButterKnife.bind(this);
        initViews();

        mBtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String passcode = mEditPassword.getText().toString();
                if(password.isEmpty())
                {
                    PopupUtils.showCustomDialog(ForgetPasswordInputNewPassword.this,getString(R.string.warning),getString(R.string.password_is_empty), R.string.ok, null);
                }
                else
                {
                    if(passwordValidation(passcode)==true) {
                        //passwordValidation(mEditPassword.getText().toString());
                        if(passcode.equals(mEditConfirmPassword.getText().toString()))
                        {
                            {

                                RestAPI.PostDataWrappy(ForgetPasswordInputNewPassword.this, null, String.format(RestAPI.POST_RESET_PASSWORD,hashpassword, password,passcode), new RestAPIListener(ForgetPasswordInputNewPassword.this) {

                                    @Override
                                    public void OnComplete(String s) {
                                        try {
                                            login(password);
                                            //doExistingAccountRegister(wpkToken.getJid() + Constant.EMAIL_DOMAIN, wpkToken.getXmppPassword(), userName);
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
                            PopupUtils.showCustomDialog(ForgetPasswordInputNewPassword.this,getString(R.string.warning),getString(R.string.lock_screen_passphrases_not_matching), R.string.ok, null);
                        }
                    }
                    else
                    {
                        PopupUtils.showCustomDialog(ForgetPasswordInputNewPassword.this,getString(R.string.warning),getString(R.string.wrong_format_password), R.string.ok, null);
                    }
                }
            }
        });
    }


    private void login(String pass) {
        AppFuncs.showProgressWaiting(this);
        String url = RestAPI.loginUrl(Store.getStringData(getApplicationContext(), Store.USERNAME), pass);
        RestAPI.PostDataWrappy(this, new JsonObject(), url, new RestAPIListener(this) {

            @Override
            public void OnComplete(String s) {
                try {
                    JsonObject jsonObject = (new JsonParser()).parse(s).getAsJsonObject();
                    Gson gson = new Gson();
                    WpkToken wpkToken = gson.fromJson(jsonObject, WpkToken.class);
                    wpkToken.saveToken(getApplicationContext());

                      doExistingAccountRegister(wpkToken.getJid() + Constant.EMAIL_DOMAIN, wpkToken.getXmppPassword(), username);
                } catch (Exception ex) {
                    AppFuncs.dismissProgressWaiting();
                    ex.printStackTrace();
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
        initActionBarDefault(true, R.string.forget_password);

        mTitlePage.setText(R.string.input_password);
        mTitlePassword.setText(R.string.input_password);
        mEditPassword.setHint(R.string.input_new_password);
        mEditConfirmPassword.setHint(R.string.input_repeat_password);
        mBtnLogin.setText(R.string.login);

        mEditPassword.setText("");
        mEditConfirmPassword.setText("");

        showHidePassword(mEditPassword, false);
        showHidePassword(mEditConfirmPassword, false);
    }
}
