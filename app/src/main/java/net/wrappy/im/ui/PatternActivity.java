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
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Response;

import net.wrappy.im.ImApp;
import net.wrappy.im.MainActivity;
import net.wrappy.im.R;
import net.wrappy.im.crypto.otr.OtrAndroidKeyManagerImpl;
import net.wrappy.im.helper.AppFuncs;
import net.wrappy.im.helper.RestAPI;
import net.wrappy.im.model.Registration;
import net.wrappy.im.model.RegistrationAccount;
import net.wrappy.im.model.WpErrors;
import net.wrappy.im.model.WpKAuthDto;
import net.wrappy.im.model.WpkToken;
import net.wrappy.im.plugin.xmpp.XmppAddress;
import net.wrappy.im.plugin.xmpp.XmppConnection;
import net.wrappy.im.provider.Imps;
import net.wrappy.im.provider.Store;
import net.wrappy.im.ui.legacy.SignInHelper;
import net.wrappy.im.ui.legacy.SimpleAlertHandler;
import net.wrappy.im.ui.onboarding.OnboardingAccount;
import net.wrappy.im.ui.onboarding.OnboardingManager;
import net.wrappy.im.util.Constant;
import net.wrappy.im.util.Debug;
import net.wrappy.im.util.PatternLockUtils;
import net.wrappy.im.util.PopupUtils;

import java.lang.ref.WeakReference;
import java.security.KeyPair;
import java.util.List;

import me.tornado.android.patternlock.PatternUtils;
import me.tornado.android.patternlock.PatternView;

import static net.wrappy.im.ui.LauncherActivity.REQUEST_CODE_LOGIN;
import static net.wrappy.im.ui.LauncherActivity.REQUEST_CODE_REGISTER;

public class PatternActivity extends me.tornado.android.patternlock.SetPatternActivity {

    public static final String TAG = "PatternActivity";

    public static Intent getStartIntent(Activity context) {
        return new Intent(context, PatternActivity.class);
    }

    String username;
    String password;
    int type_request;
    AppFuncs appFuncs;

    public static final int STATUS_SUCCESS = 1;

    private SimpleAlertHandler mHandler;

    ExistingAccountTask mExistingAccountTask;
    String hashResetPassword = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);

        ActionBar actionbar = getSupportActionBar();
        actionbar.setHomeButtonEnabled(true);
        actionbar.setDisplayHomeAsUpEnabled(true);
        appFuncs = AppFuncs.getInstance();
        mHandler = new SimpleAlertHandler(this);

        Bundle arg = getIntent().getExtras();
        if (arg != null) {
            type_request = arg.getInt("type", 0);
            username = arg.getString("username", "");
            hashResetPassword = arg.getString(ForgetPasswordActivity.FORGET_PASSWORD, "");
        }

        Intent in = getIntent();
        Uri data = in.getData();
        if (data != null) {
            type_request = REQUEST_CODE_REGISTER;
            hashResetPassword = data.getLastPathSegment();
        }


        if (type_request == REQUEST_CODE_LOGIN) {
            this.setTypePattern(TYPE_NOCONFIRM);
            actionbar.setTitle(R.string.login);

        } else {
            this.setTypePattern(TYPE_CONFIRM);

            if (hashResetPassword.isEmpty()) {
                actionbar.setTitle(R.string.registration);
            } else {
                actionbar.setTitle(R.string.forget_password);
            }
        }

        bottomText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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

            showQuestionScreen(password);
        } else if (type_request == REQUEST_CODE_LOGIN) {
            login(password);
        }
        mPatternView.clearPattern();
    }

    private void getUserInfo(final long accountId) {
        mExistingAccountTask = null;
        RestAPI.GetDataWrappy(ImApp.sImApp, RestAPI.GET_MEMBER_INFO, new RestAPI.RestAPIListenner() {
            @Override
            public void OnComplete(int httpCode, String error, String s) {
                Debug.d(s);
                try {
                    Registration registration = new Gson().fromJson(s, new TypeToken<Registration>() {
                    }.getType());
                    Imps.Account.updateAccountFromDataServer(ImApp.sImApp.getContentResolver(), registration, accountId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                LauncherActivity.start(PatternActivity.this);
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showQuestionScreen(String pass) {
        if (hashResetPassword.isEmpty()) {
            Intent intent = new Intent(this, RegistrationSecurityQuestionActivity.class);
            WpKAuthDto wpKAuthDto = new WpKAuthDto(password);
            intent.putExtra(WpKAuthDto.class.getName(), wpKAuthDto);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            resetPassword(pass);
        }


    }

    private void resetPassword(final String pass) {
        String url = RestAPI.resetPasswordUrl(hashResetPassword,pass);
        RestAPI.apiPOST(getApplicationContext(),url,new JsonObject()).setCallback(new FutureCallback<Response<String>>() {
            @Override
            public void onCompleted(Exception e, Response<String> result) {
                if (result!=null) {
                    if (RestAPI.checkHttpCode(result.getHeaders().code())) {
                        login(pass);
                    } else {
                        Log.i(TAG,WpErrors.getErrorMessage(result.getResult()));
                        AppFuncs.alert(getApplicationContext(), getString(R.string.error_reset_password),true);
                        finish();
                    }
                }
            }
        });
    }

    private void login(String pass) {
        appFuncs.showProgressWaiting(this);
        String url = RestAPI.loginUrl(Store.getStringData(getApplicationContext(), Store.USERNAME), pass);
        RestAPI.PostDataWrappy(this, new JsonObject(), url, new RestAPI.RestAPIListenner() {

            @Override
            public void OnComplete(int httpCode, String error, String s) {
                try {
                    if (!RestAPI.checkHttpCode(httpCode)) {
                        appFuncs.dismissProgressWaiting();
                        PopupUtils.showCustomDialog(PatternActivity.this, getString(R.string.error), getString(R.string.error_username_or_password), R.string.yes, null, false);
                        mPatternView.clearPattern();
                        //AppFuncs.alert(getApplicationContext(),s,true);

                        return;
                    }
                    JsonObject jsonObject = (new JsonParser()).parse(s).getAsJsonObject();
                    Gson gson = new Gson();
                    WpkToken wpkToken = gson.fromJson(jsonObject, WpkToken.class);
                    wpkToken.saveToken(getApplicationContext());
                    doExistingAccountRegister(wpkToken.getJid() + Constant.EMAIL_DOMAIN, wpkToken.getXmppPassword(), username);
                } catch (Exception ex) {
                    appFuncs.dismissProgressWaiting();
                    ex.printStackTrace();
                }

            }
        });
    }

    private void resetTask() {
        mExistingAccountTask = null;
    }

    private void onLoginFailed() {
        PopupUtils.showCustomDialog(this, "Warning", "The username or password is incorrect", R.string.yes, null, false);
    }

    private void doExistingAccountRegister(String username, String password, String accountName) {
        if (mExistingAccountTask == null) {
            mExistingAccountTask = new PatternActivity.ExistingAccountTask(this);
            mExistingAccountTask.execute(username, password, accountName);
        }
    }


    private static final class ExistingAccountTask extends AsyncTask<String, Void, Integer> {

        WeakReference<PatternActivity> weakReference;

        public ExistingAccountTask(PatternActivity activity) {
            this.weakReference = new WeakReference<>(activity);
        }

        private PatternActivity getActivity() {
            return weakReference != null && weakReference.get() != null ? weakReference.get() : null;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Integer doInBackground(String... account) {
            int status = 404;
            if (getActivity() != null) {
                try {
                    OtrAndroidKeyManagerImpl keyMan = OtrAndroidKeyManagerImpl.getInstance(getActivity());
                    KeyPair keyPair = keyMan.generateLocalKeyPair();
                    String nickname = new XmppAddress(account[0]).getUser();
                    RegistrationAccount registrationAccount = new RegistrationAccount(account[0], account[1]);
                    registrationAccount.setNickname(account[2]);
                    OnboardingAccount result = OnboardingManager.addExistingAccount(getActivity(), getActivity().mHandler, registrationAccount);

                    if (result != null) {
                        String jabberId = result.username + '@' + result.domain;
                        keyMan.storeKeyPair(jabberId, keyPair);
                        getActivity().getUserInfo(result.accountId);
                    }

                    if (account != null) {
                        XmppConnection t = new XmppConnection(getActivity());
                        status = t.check_login(account[1], result.accountId, result.providerId);
                        if (status == 200) {
                            ImApp mApp = (ImApp) getActivity().getApplication();
                            mApp.setDefaultAccount(result.providerId, result.accountId);

                            SignInHelper signInHelper = new SignInHelper(getActivity(), getActivity().mHandler);
                            signInHelper.activateAccount(result.providerId, result.accountId);
                            signInHelper.signIn(result.password, result.providerId, result.accountId, true);

                        }

                    }
                } catch (Exception e) {
                    Log.e(ImApp.LOG_TAG, "auto onboarding fail", e);
                    return 404;
                }
            }
            return status;
        }

        @Override
        protected void onPostExecute(Integer account) {
            if (getActivity() != null) {
                // mUsername = account.username + '@' + account.domain;
                getActivity().appFuncs.dismissProgressWaiting();
                if (account == 200) {
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    intent.putExtra(MainActivity.IS_FROM_PATTERN_ACTIVITY, true);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    getActivity().startActivity(intent);
                    getActivity().finish();
                } else {
                    getActivity().onLoginFailed();
                }
                getActivity().resetTask();
            }
        }
    }

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
