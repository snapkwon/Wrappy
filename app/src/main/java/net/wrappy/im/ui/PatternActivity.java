/*
 * Copyright (c) 2015 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package net.wrappy.im.ui;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.content.IntentCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.security.KeyPair;
import java.util.List;

import me.tornado.android.patternlock.PatternUtils;
import me.tornado.android.patternlock.PatternView;

import net.wrappy.im.ImApp;
import net.wrappy.im.MainActivity;
import net.wrappy.im.R;
import net.wrappy.im.crypto.otr.OtrAndroidKeyManagerImpl;
import net.wrappy.im.helper.RestAPI;
import net.wrappy.im.plugin.xmpp.XmppAddress;
import net.wrappy.im.plugin.xmpp.XmppConnection;
import net.wrappy.im.service.adapters.ImConnectionAdapter;
import net.wrappy.im.ui.legacy.SignInHelper;
import net.wrappy.im.ui.legacy.SimpleAlertHandler;
import net.wrappy.im.ui.onboarding.OnboardingAccount;
import net.wrappy.im.ui.onboarding.OnboardingActivity;
import net.wrappy.im.ui.onboarding.OnboardingManager;
import net.wrappy.im.util.PatternLockUtils;

import org.json.JSONException;
import org.json.JSONObject;

import static net.wrappy.im.ui.LauncherActivity.REQUEST_CODE_LOGIN;
import static net.wrappy.im.ui.LauncherActivity.REQUEST_CODE_REGISTER;

public class PatternActivity extends me.tornado.android.patternlock.SetPatternActivity{

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

        mHandler = new SimpleAlertHandler(this);

        Bundle arg= getIntent().getExtras();
        type_request = arg.getInt("type");
        username = arg.getString("username");
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
      //  username = arg.getString("username");
       // password = arg.getString("password");
        dialog = new ProgressDialog(PatternActivity.this);
        dialog.setCancelable(false);
        if(type_request == REQUEST_CODE_REGISTER) {
            new RestAPI.PostDataUrl(null, new RestAPI.RestAPIListenner() {
                @Override
                public void OnInit() {
                    dialog.setMessage("waiting");
                    dialog.show();
                }

                @Override
                public void OnComplete(String error, String s) {
                    JSONObject mainObject = null;
                    try {
                        if (dialog != null && dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        mainObject = new JSONObject(s);
                        JSONObject uniObject = mainObject.getJSONObject("data");
                        int status = mainObject.getInt("status");
                        if (status == 1) {
                            username = uniObject.getString("jid");
                            password = uniObject.getString("xmppPass");
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }).execute(RestAPI.POST_REGISTER);
        }
    }
    @Override
    protected void onCanceled() {
        finish();
        //  finish();
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

    @Override
    protected void onSetPattern(List<PatternView.Cell> pattern) {
        PatternLockUtils.setPattern(pattern, this);

        password = PatternUtils.patternToString(pattern);
        if(type_request == REQUEST_CODE_REGISTER) {

            // password = PatternUtils.patternToString(pattern);

            JsonArray jsonArray = new JsonArray();

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("UserName", username);
            jsonObject.addProperty("PassWord", password);


            jsonArray.add(jsonObject);

            new RestAPI.PutDataUrl(jsonArray.toString(), new RestAPI.RestAPIListenner() {
                @Override
                public void OnInit() {
                    dialog.setMessage("waiting");
                    dialog.show();
                }

                @Override
                public void OnComplete(String error, String s) {
                    JSONObject mainObject = null;
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    try {
                        mainObject = new JSONObject(s);
                        if (mainObject.getInt("status") == STATUS_SUCCESS) {
                            //  doExistingAccountRegister(username, password);
                            showQuestionScreen();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }).execute(RestAPI.PUT_UPDATEPASS);
        }

        else if(type_request == REQUEST_CODE_LOGIN)
        {

            doExistingAccountRegister(username,password);
        }

     //   Intent returnIntent = new Intent();
      //  returnIntent.putExtra("result", PatternUtils.patternToString(pattern));
      //  setResult(this.RESULT_OK,returnIntent);
       // finish();
    }


    private void showQuestionScreen ()
    {

        Intent intent = new Intent(this, RegistrationSecurityQuestionActivity.class);
        Bundle arg = new Bundle();
        arg.putString("username",username);
        arg.putString("password",password);
        intent.putExtras(arg);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void doExistingAccountRegister (String username , String password)
    {

        if (mExistingAccountTask == null) {
            mExistingAccountTask = new PatternActivity.ExistingAccountTask();
            mExistingAccountTask.execute(username, password);
            dialog.setMessage("Wating...");
            dialog.show();
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
                OnboardingAccount result = OnboardingManager.addExistingAccount(PatternActivity.this, mHandler, nickname, account[0], account[1]);

                if (result != null) {
                    String jabberId = result.username + '@' + result.domain;
                    keyMan.storeKeyPair(jabberId,keyPair);
                }

                if(account!=null) {
                    XmppConnection t = new XmppConnection(PatternActivity.this);
                    status =  t.check_login(password,result.accountId,result.providerId);
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
