/*
 * Copyright (c) 2015 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package net.wrappy.im.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.MenuItem;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.wrappy.im.ImApp;
import net.wrappy.im.MainActivity;
import net.wrappy.im.R;
import net.wrappy.im.crypto.otr.OtrAndroidKeyManagerImpl;
import net.wrappy.im.helper.AppFuncs;
import net.wrappy.im.helper.RestAPI;
import net.wrappy.im.model.WpKAuthDto;
import net.wrappy.im.model.WpkToken;
import net.wrappy.im.plugin.xmpp.XmppAddress;
import net.wrappy.im.plugin.xmpp.XmppConnection;
import net.wrappy.im.ui.legacy.SignInHelper;
import net.wrappy.im.ui.legacy.SimpleAlertHandler;
import net.wrappy.im.ui.onboarding.OnboardingAccount;
import net.wrappy.im.ui.onboarding.OnboardingManager;
import net.wrappy.im.util.Constant;
import net.wrappy.im.util.PatternLockUtils;

import java.security.KeyPair;
import java.util.List;

import me.tornado.android.patternlock.PatternUtils;
import me.tornado.android.patternlock.PatternView;

import static net.wrappy.im.ui.LauncherActivity.REQUEST_CODE_LOGIN;
import static net.wrappy.im.ui.LauncherActivity.REQUEST_CODE_REGISTER;

public class PatternActivity extends me.tornado.android.patternlock.SetPatternActivity{

    public static Intent getStartIntent(Activity context){
        return new Intent(context, PatternActivity.class);
    }

    String username;
    String password;
    ProgressDialog dialog;
    int type_request;

    public static final int STATUS_SUCCESS = 1;

    private SimpleAlertHandler mHandler;

    ExistingAccountTask mExistingAccountTask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);

        ActionBar actionbar = getSupportActionBar();
        actionbar.setHomeButtonEnabled(true);
        actionbar.setDisplayHomeAsUpEnabled(true);

        mHandler = new SimpleAlertHandler(this);

        Bundle arg= getIntent().getExtras();
        if (arg!=null) {
            type_request = arg.getInt("type", 0);
            username = arg.getString("username", "");
        }

        if(type_request == REQUEST_CODE_LOGIN)
        {
            this.setTypePattern(TYPE_NOCONFIRM);
            actionbar.setTitle("Login");

        }
        else
        {
            this.setTypePattern(TYPE_CONFIRM);
            actionbar.setTitle("Registration");
        }
    }
    @Override
    protected void onCanceled() {
        finish();
        //  finish();
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    @Override
    protected void onSetPattern(List<PatternView.Cell> pattern) {
        PatternLockUtils.setPattern(pattern, this);

        password = PatternUtils.patternToString(pattern);
        if(type_request == REQUEST_CODE_REGISTER) {

            showQuestionScreen();
        }

        else if(type_request == REQUEST_CODE_LOGIN)
        {
            dialog = new ProgressDialog(PatternActivity.this);
            dialog.setMessage(getString(R.string.waiting_dialog));
            dialog.show();
            String url = RestAPI.loginUrl(username,password);
            RestAPI.PostDataWrappy(getApplicationContext(),new JsonObject(), url, new RestAPI.RestAPIListenner() {

                @Override
                public void OnComplete(int httpCode, String error, String s) {
                    try {
                        if (!RestAPI.checkHttpCode(httpCode)) {
                            AppFuncs.alert(getApplicationContext(),s,true);
                            if (dialog != null && dialog.isShowing()) {
                                dialog.dismiss();
                            }
                            return;
                        }
                        JsonObject jsonObject = (new JsonParser()).parse(s).getAsJsonObject();
                        Gson gson = new Gson();
                        WpkToken wpkToken = gson.fromJson(jsonObject, WpkToken.class);
                        wpkToken.saveToken(getApplicationContext());
                        doExistingAccountRegister(wpkToken.getJid()+ Constant.EMAIL_DOMAIN,wpkToken.getXmppPassword(), username);
                    }catch (Exception ex) {
                        if (dialog != null && dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        ex.printStackTrace();
                    }

                }
            });
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showQuestionScreen ()
    {

        Intent intent = new Intent(this, RegistrationSecurityQuestionActivity.class);
        WpKAuthDto wpKAuthDto = new WpKAuthDto(password);
        intent.putExtra(WpKAuthDto.class.getName(),wpKAuthDto);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void doExistingAccountRegister (String username , String password, String accountName)
    {

        if (mExistingAccountTask == null) {
            mExistingAccountTask = new PatternActivity.ExistingAccountTask();
            mExistingAccountTask.execute(username, password, accountName);
        }
    }



    private class ExistingAccountTask extends AsyncTask<String, Void, Integer> {

        @Override
        protected void onPreExecute()
        {
        }

        @Override
        protected Integer doInBackground(String... account) {
            try {
                int status = 404;
                OtrAndroidKeyManagerImpl keyMan = OtrAndroidKeyManagerImpl.getInstance(PatternActivity.this);
                KeyPair keyPair = keyMan.generateLocalKeyPair();
                String nickname = new XmppAddress(account[0]).getUser();
                OnboardingAccount result = OnboardingManager.addExistingAccount(PatternActivity.this, mHandler, nickname, account[0], account[1], account[2]);

                if (result != null) {
                    String jabberId = result.username + '@' + result.domain;
                    keyMan.storeKeyPair(jabberId,keyPair);
                }

                if(account!=null) {
                    XmppConnection t = new XmppConnection(PatternActivity.this);
                    status =  t.check_login(account[1],result.accountId,result.providerId);
                    if(status == 200)
                    {
                        ImApp mApp = (ImApp)getApplication();
                        mApp.setDefaultAccount(result.providerId,result.accountId);

                        SignInHelper signInHelper = new SignInHelper(PatternActivity.this, mHandler);
                        signInHelper.activateAccount(result.providerId,result.accountId);
                        signInHelper.signIn(result.password, result.providerId, result.accountId, true);

                    }

                }

                return status;
            }
            catch (Exception e)
            {
                Log.e(ImApp.LOG_TAG, "auto onboarding fail", e);
                return 404;
            }
        }

        @Override
        protected void onPostExecute(Integer account) {

            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
            // mUsername = account.username + '@' + account.domain;
            if(account==200) {
                    Intent intent = new Intent(PatternActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
            }
            else
            {
                AlertDialog alertDialog = new AlertDialog.Builder(PatternActivity.this).create();
                alertDialog.setTitle("Warning");
                alertDialog.setMessage("The user name or password is incorrect");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            }
            mExistingAccountTask = null;
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
