/*
 * Copyright (c) 2015 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package net.wrappy.im.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.IntentCompat;
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

public class PatternActivity extends me.tornado.android.patternlock.SetPatternActivity{

    String username;
    String password;
    ProgressDialog dialog;

    public static final int STATUS_SUCCESS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
       // Bundle arg= getIntent().getExtras();
      //  username = arg.getString("username");
       // password = arg.getString("password");
        dialog = new ProgressDialog(PatternActivity.this);
        dialog.setCancelable(false);
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
                    int  status = mainObject.getInt("status");
                    if(status == 1) {
                         username = uniObject.getString("jid");
                         password = uniObject.getString("xmppPass");
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }).execute(RestAPI.POST_REGISTER);

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

       // password = PatternUtils.patternToString(pattern);

        JsonArray jsonArray = new JsonArray();

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("UserName",username);
        jsonObject.addProperty("PassWord",password);


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
                    if(mainObject.getInt("status") ==  STATUS_SUCCESS) {
                      //  doExistingAccountRegister(username, password);
                        showQuestionScreen();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).execute(RestAPI.PUT_UPDATEPASS);

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
