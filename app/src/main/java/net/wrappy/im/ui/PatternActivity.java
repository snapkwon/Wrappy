/*
 * Copyright (c) 2015 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package net.wrappy.im.ui;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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
import net.wrappy.im.ui.legacy.SignInHelper;
import net.wrappy.im.ui.legacy.SimpleAlertHandler;
import net.wrappy.im.ui.onboarding.OnboardingAccount;
import net.wrappy.im.ui.onboarding.OnboardingActivity;
import net.wrappy.im.ui.onboarding.OnboardingManager;
import net.wrappy.im.util.PatternLockUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class PatternActivity extends me.tornado.android.patternlock.SetPatternActivity implements RestAPI.RectAPIListenner{

    String username;
    String password;
    ExistingAccountTask mExistingAccountTask;
    private String mFingerprint;
    private OnboardingAccount mNewAccount;

    private SimpleAlertHandler mHandler;

    public static final int STATUS_SUCCESS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        Bundle arg= getIntent().getExtras();
        username = arg.getString("username");
        password = arg.getString("password");
        mHandler = new SimpleAlertHandler(this);

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

       // password = PatternUtils.patternToString(pattern);

        JsonArray jsonArray = new JsonArray();

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("UserName",username);
        jsonObject.addProperty("PassWord",password);


        jsonArray.add(jsonObject);

        new RestAPI.PutDataUrl(jsonArray.toString(), this).execute(RestAPI.PUT_UPDATEPASS);

     //   Intent returnIntent = new Intent();
      //  returnIntent.putExtra("result", PatternUtils.patternToString(pattern));
      //  setResult(this.RESULT_OK,returnIntent);
       // finish();
    }

    private void doExistingAccountRegister (String username , String password)
    {

        if (mExistingAccountTask == null) {
            mExistingAccountTask = new PatternActivity.ExistingAccountTask();
            mExistingAccountTask.execute(username, password);
        }
    }

    private class ExistingAccountTask extends AsyncTask<String, Void, OnboardingAccount> {
        @Override
        protected OnboardingAccount doInBackground(String... account) {
            try {

                OtrAndroidKeyManagerImpl keyMan = OtrAndroidKeyManagerImpl.getInstance(PatternActivity.this);
                KeyPair keyPair = keyMan.generateLocalKeyPair();
                mFingerprint = keyMan.getFingerprint(keyPair.getPublic());

                String nickname = new XmppAddress(account[0]).getUser();
                OnboardingAccount result = OnboardingManager.addExistingAccount(PatternActivity.this, mHandler, nickname, account[0], account[1]);

                if (result != null) {
                    String jabberId = result.username + '@' + result.domain;
                    keyMan.storeKeyPair(jabberId,keyPair);
                }

                return result;
            }
            catch (Exception e)
            {
                Log.e(ImApp.LOG_TAG, "auto onboarding fail", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(OnboardingAccount account) {

           // mUsername = account.username + '@' + account.domain;

            ImApp mApp = (ImApp)getApplication();
            mApp.setDefaultAccount(account.providerId,account.accountId);

            SignInHelper signInHelper = new SignInHelper(PatternActivity.this, mHandler);
            signInHelper.activateAccount(account.providerId,account.accountId);
            signInHelper.signIn(account.password, account.providerId, account.accountId, true);

            showInviteScreen();

            mExistingAccountTask = null;
        }
    }

    private void showInviteScreen ()
    {

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
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
    }
}
