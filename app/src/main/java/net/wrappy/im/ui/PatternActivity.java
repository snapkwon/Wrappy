/*
 * Copyright (c) 2015 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package net.wrappy.im.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
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
import net.wrappy.im.model.WpKAuthDto;
import net.wrappy.im.model.WpkToken;
import net.wrappy.im.provider.Store;
import net.wrappy.im.ui.onboarding.OnboardingAccount;
import net.wrappy.im.util.Constant;
import net.wrappy.im.util.PatternLockUtils;
import net.wrappy.im.util.PopupUtils;

import java.util.List;

import me.tornado.android.patternlock.PatternUtils;
import me.tornado.android.patternlock.PatternView;

import static net.wrappy.im.ui.LauncherActivity.REQUEST_CODE_INPUT_NEW_PASSWORD;
import static net.wrappy.im.ui.LauncherActivity.REQUEST_CODE_LOGIN;
import static net.wrappy.im.ui.LauncherActivity.REQUEST_CODE_REGISTER;

public class PatternActivity extends me.tornado.android.patternlock.SetPatternActivity {

    public static final String TAG = "PatternActivity";
    public static final String PASSWORD_INPUT = "password";
    public static final String USER_INFO = "userinfo";
    public static final String HASHPASSWORD = "hashpassword";

    public static Intent getStartIntent(Activity context) {
        return new Intent(context, PatternActivity.class);
    }

    String username;
    String password;
    int type_request;
    AppFuncs appFuncs;

    CountDownTimer cTimer = null;

    //    public static final int STATUS_SUCCESS = 1;
//
//    private SimpleAlertHandler mHandler;
//
//    ExistingAccountTask mExistingAccountTask;
    String hashResetPassword = "";


    void startTimer() {
        cTimer = new CountDownTimer(AppFuncs.TIMECOUNTDOWN, 1000) {
            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000);
                //int minutes = seconds / 60;
                seconds = seconds % 60;
                mMessageText.setText(String.format(PatternActivity.this.getString(R.string.pattern_coutdown), String.format("%02d", seconds)));
            }
            public void onFinish() {
                cancelTimer();
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
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);

        ActionBar actionbar = getSupportActionBar();
        actionbar.setHomeButtonEnabled(true);
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionbar.setCustomView(R.layout.actionbar_register);
        TextView title = (TextView) findViewById(getResources().getIdentifier("action_bar_title", "id", getPackageName()));
        ImageView backButton = (ImageView) findViewById(getResources().getIdentifier("action_bar_arrow_back", "id", getPackageName()));
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        appFuncs = AppFuncs.getInstance();
        //mHandler = new SimpleAlertHandler(this);

        Bundle arg = getIntent().getExtras();
        if (arg != null) {
            type_request = arg.getInt("type", 0);
            username = arg.getString("username", "");
            hashResetPassword = arg.getString(ForgetPasswordActivity.FORGET_PASSWORD, "");
        }

        Intent in = getIntent();
        Uri data = in.getData();
        if (data != null) {
            type_request = REQUEST_CODE_INPUT_NEW_PASSWORD;
            hashResetPassword = data.getLastPathSegment();
        }


        if (type_request == REQUEST_CODE_LOGIN) {
            this.setTypePattern(TYPE_NOCONFIRM);
            title.setText(R.string.login);
            startTimer();
           // mCountdownText.setVisibility(View.VISIBLE);

        } else {
            this.setTypePattern(TYPE_CONFIRM);

            if (hashResetPassword.isEmpty()) {
                title.setText(R.string.registration);
            } else {
                title.setText(R.string.forget_password);
            }
           // mCountdownText.setVisibility(View.GONE);
        }

        bottomText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelTimer();
                ForgetPasswordActivity.start(PatternActivity.this);
                finish();
            }
        });
    }

    @Override
    protected void onCanceled() {
        finish();
        //  finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onSetPattern(final List<PatternView.Cell> pattern) {
        PatternLockUtils.setPattern(pattern, this);

        password = PatternUtils.patternToString(pattern);
        if (type_request == REQUEST_CODE_REGISTER) {
            //showQuestionScreen(password);
            Intent intent = new Intent(PatternActivity.this , InputPasswordRegisterActivity.class);
            intent.putExtra(PASSWORD_INPUT,password);
            PatternActivity.this.startActivity(intent);
        } else if (type_request == REQUEST_CODE_LOGIN) {
            login(password);
            cancelTimer();
        } else if (type_request == REQUEST_CODE_INPUT_NEW_PASSWORD) {
            ForgetPasswordInputNewPassword.start(PatternActivity.this,password,hashResetPassword);
        }
        mPatternView.clearPattern();
    }
//
//    private void getUserInfo(final long accountId) {
//        mExistingAccountTask = null;
//        RestAPI.GetDataWrappy(ImApp.sImApp, RestAPI.GET_MEMBER_INFO, new RestAPI.RestAPIListener() {
//            @Override
//            public void OnComplete(int httpCode, String error, String s) {
//                Debug.d(s);
//                try {
//                    Registration registration = new Gson().fromJson(s, Registration.class);
//                    Imps.Account.updateAccountFromDataServer(ImApp.sImApp.getContentResolver(), registration, accountId);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AppFuncs.dismissProgressWaiting();
    }

    @Override
    public void onBackPressed() {
        LauncherActivity.start(PatternActivity.this);
    }

 /*   private void showQuestionScreen(String pass) {
        if (hashResetPassword.isEmpty()) {
            Intent intent = new Intent(this, RegistrationSecurityQuestionActivity.class);
            WpKAuthDto wpKAuthDto = new WpKAuthDto(password,);
            intent.putExtra(WpKAuthDto.class.getName(), wpKAuthDto);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            resetPassword(pass);
        }
    }*/

  /*  private void resetPassword(final String pass) {
        String url = RestAPI.resetPasswordUrl(hashResetPassword, pass);
        RestAPIListener listener = new RestAPIListener() {
            @Override
            public void OnComplete(String s) {
                resetPasscode();
            }
        };
        listener.setOnListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        RestAPI.PostDataWrappy(getApplicationContext(), new JsonObject(), url, listener);
    }*/

  /*  private int getResId(String resName) {
        try {
            return PatternActivity.this.getResources().getIdentifier(resName, "string", PatternActivity.this.getPackageName());
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }*/
  /*private void resetPasscode()
  {
      ForgetPasswordInputNewPassword.start(PatternActivity.this);
  }*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultIntent) {
        switch (requestCode) {
            case LauncherActivity.RESULT_ERROR:
                if(resultIntent!=null && !resultIntent.getStringExtra("error").isEmpty()) {
                    setResult(RESULT_OK, resultIntent);
                    finish();
                }
                else
                {
                    finish();
                }
                break;
        }
    }

    private void login(String pass) {
        AppFuncs.showProgressWaiting(this);
        String url = RestAPI.loginUrl(Store.getStringData(getApplicationContext(), Store.USERNAME), pass);
        RestAPI.PostDataWrappy(this, new JsonObject(), url, new RestAPIListener(this) {

            @Override
            public void onError(int errorCode)
            {
                int resId = getResId("error_" + errorCode);
                getIntent().putExtra("error", PatternActivity.this.getString(resId));
                setResult(RESULT_OK, getIntent());
                finish();
            }

            @Override
            public void OnComplete(String s) {
                try {
                    AppFuncs.log("PatternActivity: " + s);
                    JsonObject jsonObject = (new JsonParser()).parse(s).getAsJsonObject();
                    Gson gson = new Gson();
                    WpkToken wpkToken = gson.fromJson(jsonObject, WpkToken.class);
                    wpkToken.saveToken(getApplicationContext());
                    if(wpkToken.getHasPasscode() == true ) {
                        Intent intent = new Intent(PatternActivity.this, InputPasswordLoginActivity.class);
                        intent.putExtra(PatternActivity.USER_INFO, wpkToken);
                        intent.putExtra("username",username);
                        intent.putExtra("hashPasscode",wpkToken.getHasPasscode());
                        PatternActivity.this.startActivityForResult(intent,LauncherActivity.RESULT_ERROR);
                        AppFuncs.dismissProgressWaiting();
                    }
                    else
                    {
                        Intent intent = new Intent(PatternActivity.this, InputPasswordRegisterActivity.class);
                        intent.putExtra(PatternActivity.USER_INFO, wpkToken);
                        intent.putExtra("username",username);
                        intent.putExtra("hasPasscode",wpkToken.getHasPasscode());
                        PatternActivity.this.startActivity(intent);
                        AppFuncs.dismissProgressWaiting();
                        finish();
                    }
                  //  doExistingAccountRegister(wpkToken.getJid() + Constant.EMAIL_DOMAIN, wpkToken.getXmppPassword(), username);
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
//        if (mExistingAccountTask == null) {
//            mExistingAccountTask = new PatternActivity.ExistingAccountTask(this);
//            mExistingAccountTask.execute(username, password, accountName);
//        }
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
//
//
//    private static final class ExistingAccountTask extends AsyncTask<String, Void, Integer> {
//
//        WeakReference<PatternActivity> weakReference;
//
//        public ExistingAccountTask(PatternActivity activity) {
//            this.weakReference = new WeakReference<>(activity);
//        }
//
//        private PatternActivity getActivity() {
//            return weakReference != null && weakReference.get() != null ? weakReference.get() : null;
//        }
//
//        @Override
//        protected void onPreExecute() {
//        }
//
//        @Override
//        protected Integer doInBackground(String... account) {
//            int status = 404;
//            if (getActivity() != null) {
//                try {
//                    OtrAndroidKeyManagerImpl keyMan = OtrAndroidKeyManagerImpl.getInstance(getActivity());
//                    KeyPair keyPair = keyMan.generateLocalKeyPair();
//                    String nickname = new XmppAddress(account[0]).getUser();
//                    RegistrationAccount registrationAccount = new RegistrationAccount(account[0], account[1]);
//                    registrationAccount.setNickname(account[2]);
//                    OnboardingAccount result = OnboardingManager.addExistingAccount(getActivity(), getActivity().mHandler, registrationAccount);
//
//                    if (result != null) {
//                        String jabberId = result.username + '@' + result.domain;
//                        keyMan.storeKeyPair(jabberId, keyPair);
//                        getActivity().getUserInfo(result.accountId);
//                    }
//
//                    if (account != null) {
//                        XmppConnection t = new XmppConnection(getActivity());
//                        status = t.check_login(account[1], result.accountId, result.providerId);
//                        if (status == 200) {
//                            ImApp mApp = (ImApp) getActivity().getApplication();
//                            mApp.setDefaultAccount(result.providerId, result.accountId);
//
//                            SignInHelper signInHelper = new SignInHelper(getActivity(), getActivity().mHandler);
//                            signInHelper.activateAccount(result.providerId, result.accountId);
//                            signInHelper.signIn(result.password, result.providerId, result.accountId, true);
//
//                        }
//
//                    }
//                } catch (Exception e) {
//                    Log.e(ImApp.LOG_TAG, "auto onboarding fail", e);
//                    return 404;
//                }
//            }
//            return status;
//        }
//
//        @Override
//        protected void onPostExecute(Integer account) {
//            if (getActivity() != null) {
//                // mUsername = account.username + '@' + account.domain;
//                getActivity().appFuncs.dismissProgressWaiting();
//                if (account == 200) {
//                    Intent intent = new Intent(getActivity(), MainActivity.class);
//                    intent.putExtra(MainActivity.IS_FROM_PATTERN_ACTIVITY, true);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                    getActivity().startActivity(intent);
//                    getActivity().finish();
//                } else {
//                    getActivity().onLoginFailed();
//                }
//                getActivity().resetTask();
//            }
//        }
//    }

   /* @Override
    public void OnComplete(String error, String s) {

        JSONObject mainObject = null;
        try {
            mainObject = new JSONObject(s);
            if(mainObject.getInt("status") ==  STATUS_SUCCESS) {
                doExistingAccountRegister(username, password);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }*/
}
