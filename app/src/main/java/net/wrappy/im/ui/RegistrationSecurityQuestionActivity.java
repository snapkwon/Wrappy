package net.wrappy.im.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.wrappy.im.ImApp;
import net.wrappy.im.R;
import net.wrappy.im.crypto.otr.OtrAndroidKeyManagerImpl;
import net.wrappy.im.helper.AppFuncs;
import net.wrappy.im.helper.RestAPI;
import net.wrappy.im.helper.layout.AppButton;
import net.wrappy.im.helper.layout.AppEditTextView;
import net.wrappy.im.helper.layout.AppTextView;
import net.wrappy.im.plugin.xmpp.XmppAddress;
import net.wrappy.im.ui.legacy.SignInHelper;
import net.wrappy.im.ui.legacy.SimpleAlertHandler;
import net.wrappy.im.ui.onboarding.OnboardingAccount;
import net.wrappy.im.ui.onboarding.OnboardingManager;

import java.security.KeyPair;
import java.util.ArrayList;

/**
 * Created by ben on 13/11/2017.
 */

public class RegistrationSecurityQuestionActivity extends AppCompatActivity implements View.OnClickListener {

    LinearLayout securityQuestionLayout;
    ExistingAccountTask mExistingAccountTask;
    String[] questions;
    ImageButton headerbarBack;
    AppTextView headerbarTitle;
    ArrayAdapter<String> questionsAdapter;
    AppButton btnQuestionComplete;
    ArrayList<Spinner> spinnersQuestion = new ArrayList<>();
    ArrayList<AppEditTextView> appEditTextViewsAnswers = new ArrayList<>();
    boolean isFlag;
    private String mFingerprint;
    private OnboardingAccount mNewAccount;

    private String username;
    private String password;

    private SimpleAlertHandler mHandler;

    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration_activity_security_question);

        Bundle arg = getIntent().getExtras();
        username = arg.getString("username");
        password = arg.getString("password");
        referenceView();
        mHandler = new SimpleAlertHandler(this);
        dialog = new ProgressDialog(RegistrationSecurityQuestionActivity.this);
        dialog.setCancelable(false);
    }

    private void referenceView() {
        securityQuestionLayout = (LinearLayout) findViewById(R.id.securityQuestionLayout);
        headerbarBack = (ImageButton) findViewById(R.id.headerbarBack);
        headerbarBack.setOnClickListener(this);
        headerbarTitle = (AppTextView) findViewById(R.id.headerbarTitle);
        headerbarTitle.setText("Question");
        btnQuestionComplete = (AppButton) findViewById(R.id.btnQuestionComplete);
        btnQuestionComplete.setOnClickListener(this);

        getListQuestion();

    }

    private void getListQuestion() {
        new RestAPI.GetDataUrl(new RestAPI.RestAPIListenner() {
            @Override
            public void OnInit() {
                dialog.setMessage("Wating...");
                dialog.show();
            }

            @Override
            public void OnComplete(String error, String s) {
                try {
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    //TODO get data question list from url
                    if (error!=null) {
                        AppFuncs.alert(getApplicationContext(),error,true);
                        return;
                    }
                    AppFuncs.log(s);
                    JsonObject jsonObject = RestAPI.parseStringToJsonElement(s).getAsJsonObject();
                    if (RestAPI.getStatus(jsonObject)==1) {
                        JsonArray jsonArrayQuestions = RestAPI.getData(jsonObject).getAsJsonArray();
                        questions = new String[jsonArrayQuestions.size()];
                        for (int i=0 ; i<jsonArrayQuestions.size();i++) {
                            questions[i] = jsonArrayQuestions.get(i).getAsJsonObject().get("question").getAsString();
                        }
                        questionsAdapter = new ArrayAdapter<String>(RegistrationSecurityQuestionActivity.this,R.layout.registration_activity_security_question_item_textview,questions);
                        securityQuestionLayout.removeAllViews();
                        if (questions.length > 2) {
                            for (int i=0 ; i < 3; i++) {
                                View questionLayoutView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.registration_activity_security_question_item,null);
                                securityQuestionLayout.addView(questionLayoutView);
                                AppTextView txtQuestionTitle = (AppTextView) questionLayoutView.findViewById(R.id.txtQuestionTitle);
                                txtQuestionTitle.setText("Question " + String.valueOf(i+1));
                                Spinner questionSpinner = (Spinner) questionLayoutView.findViewById(R.id.spinnerQuestion);
                                spinnersQuestion.add(questionSpinner);
                                questionSpinner.setAdapter(questionsAdapter);
                                questionSpinner.setSelection(i);
                                AppEditTextView editTextView = (AppEditTextView) questionLayoutView.findViewById(R.id.edQuestionAnswer);
                                appEditTextViewsAnswers.add(editTextView);
                            }
                            btnQuestionComplete.setEnabled(true);
                        }
                    }
                }catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).execute(RestAPI.GET_QUESTIONS_SECURITY);
    }

    private void doExistingAccountRegister (String username , String password)
    {

        if (mExistingAccountTask == null) {
            mExistingAccountTask = new RegistrationSecurityQuestionActivity.ExistingAccountTask();
            mExistingAccountTask.execute(username, password);
        }
    }

    private class ExistingAccountTask extends AsyncTask<String, Void, OnboardingAccount> {
        @Override
        protected OnboardingAccount doInBackground(String... account) {
            try {

                OtrAndroidKeyManagerImpl keyMan = OtrAndroidKeyManagerImpl.getInstance(RegistrationSecurityQuestionActivity.this);
                KeyPair keyPair = keyMan.generateLocalKeyPair();
                mFingerprint = keyMan.getFingerprint(keyPair.getPublic());

                String nickname = new XmppAddress(account[0]).getUser();
                OnboardingAccount result = OnboardingManager.addExistingAccount(RegistrationSecurityQuestionActivity.this, mHandler, nickname, account[0], account[1]);

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

            SignInHelper signInHelper = new SignInHelper(RegistrationSecurityQuestionActivity.this, mHandler);
            signInHelper.activateAccount(account.providerId,account.accountId);
            signInHelper.signIn(account.password, account.providerId, account.accountId, true);

            mExistingAccountTask = null;
        }
    }

    @Override
    public void onClick(View view) {
        if (isFlag) {
            return;
        }isFlag = true;
        try {
            if (view.getId() == headerbarBack.getId()) {
                finish();
            }
            if (view.getId() == btnQuestionComplete.getId()) {
                if (spinnersQuestion.size() > 0) {
                    JsonArray jsonArray = new JsonArray();
                    String errorString = "";
                    ArrayList<String> strings = new ArrayList<>();
                    for (int i=0; i < spinnersQuestion.size(); i++) {
                        Spinner spinner = spinnersQuestion.get(i);
                        String question = spinner.getSelectedItem().toString();
                        AppEditTextView editTextView = appEditTextViewsAnswers.get(i);
                        String answer = editTextView.getText().toString().trim();
                        // validate answer text
                        if (answer.isEmpty()) {
                            errorString = String.format("Your answer of question %d is empty", (i+1));
                            break;
                        }

                        if (strings.size() > 0) {
                            if (strings.contains(answer)) {
                                errorString = "Your answer have duplicated";
                                break;
                            }
                        }
                        strings.add(answer);

                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("question",question);
                        jsonObject.addProperty("answer",answer);


                        jsonArray.add(jsonObject);
                    }
                    if (!errorString.isEmpty()) {
                        AppFuncs.alert(getApplicationContext(),errorString,true);
                        return;
                    }
                    String s = new RestAPI.PostDataUrl(jsonArray.toString(), null).execute(RestAPI.POST_QUESTION_ANSWERS).get();
                    AppFuncs.log(s);
                    if (RestAPI.getStatus(RestAPI.parseStringToJsonElement(s).getAsJsonObject())==1) {
                        doExistingAccountRegister(username, password);
                        Intent intent = new Intent(RegistrationSecurityQuestionActivity.this,UpdateProfileActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }

            }
        }catch (Exception ex) {
            ex.printStackTrace();
        }finally {
            isFlag = false;
        }

    }
}
