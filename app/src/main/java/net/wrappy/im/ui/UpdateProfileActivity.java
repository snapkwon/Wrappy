package net.wrappy.im.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatSpinner;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.wrappy.im.ImApp;
import net.wrappy.im.MainActivity;
import net.wrappy.im.R;
import net.wrappy.im.crypto.otr.OtrAndroidKeyManagerImpl;
import net.wrappy.im.helper.AppFuncs;
import net.wrappy.im.helper.RestAPI;
import net.wrappy.im.helper.layout.AppButton;
import net.wrappy.im.helper.layout.AppTextView;
import net.wrappy.im.helper.layout.CircleImageView;
import net.wrappy.im.plugin.xmpp.XmppAddress;
import net.wrappy.im.ui.legacy.SignInHelper;
import net.wrappy.im.ui.legacy.SimpleAlertHandler;
import net.wrappy.im.ui.onboarding.OnboardingAccount;
import net.wrappy.im.ui.onboarding.OnboardingManager;
import net.wrappy.im.util.SecureMediaStore;

import java.security.KeyPair;
import java.util.ArrayList;

/**
 * Created by ben on 15/11/2017.
 */

public class UpdateProfileActivity extends BaseActivity implements View.OnClickListener {

    private final int IMAGE_HEADER = 100;
    private final int IMAGE_AVATAR = 101;

    ImageButton headerbarBack;
    AppTextView headerbarTitle;
    EditText edUsername, edEmail, edPhone;
    AppCompatSpinner spinnerProfileGender;
    AppButton btnComplete, btnSkip;
    ImageButton btnCameraHeader,btnCameraAvatar;
    CircleImageView imgAvatar;
    ImageView imgHeader;
    boolean isFlag;
    String user,email,phone,other,password;
    JsonObject dataJson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.update_profile_activity);
        getSecurityQuestions();
        if (dataJson==null) {
            finish();
            return;
        }
        mHandler = new SimpleAlertHandler(this);
        preferenceView();
    }

    private void getSecurityQuestions() {
        try {
            password = getIntent().getStringExtra("password");
            String data = getIntent().getStringExtra("data");
            dataJson = new JsonObject();
            dataJson = (new JsonParser()).parse(data).getAsJsonObject();
        }catch (Exception ex) {
            dataJson = null;
            ex.printStackTrace();
        }
    }

    private void preferenceView() {
        headerbarBack = (ImageButton) findViewById(R.id.headerbarBack);
        headerbarBack.setOnClickListener(this);
        headerbarTitle = (AppTextView) findViewById(R.id.headerbarTitle);
        headerbarTitle.setText("Update Profile");
        //
        edUsername = (EditText) findViewById(R.id.edProfileUsername);
        edEmail = (EditText) findViewById(R.id.edProfileEmail);
        edPhone = (EditText) findViewById(R.id.edProfilePhone);
        spinnerProfileGender = (AppCompatSpinner) findViewById(R.id.spinnerProfileGender);
        btnComplete = (AppButton) findViewById(R.id.btnProfileComplete);
        btnComplete.setOnClickListener(this);
        btnSkip = (AppButton) findViewById(R.id.btnProfileSkip);
        btnSkip.setOnClickListener(this);
        btnCameraAvatar = (ImageButton) findViewById(R.id.btnProfileCameraAvatar);
        btnCameraAvatar.setOnClickListener(this);
        btnCameraHeader = (ImageButton) findViewById(R.id.btnProfileCameraHeader);
        btnCameraHeader.setOnClickListener(this);
        imgAvatar = (CircleImageView) findViewById(R.id.imgProfileAvatar);
        imgHeader = (ImageView) findViewById(R.id.imgProfileHeader);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.profile_gender, R.layout.update_profile_textview);
        spinnerProfileGender.setAdapter(adapter);
    }

    @Override
    public void onClick(View view) {
        if (isFlag) {
            return;
        } isFlag = true;
        try {
            if (view.getId() == headerbarBack.getId()) {
                finish();
            }
            if (view.getId() == btnComplete.getId()) {
                String error = validateData();
                if (!error.isEmpty()) {
                    AppFuncs.alert(getApplicationContext(),error,true);
                    return;
                }
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("identifier",user);
                if (!email.isEmpty()) {
                    jsonObject.addProperty("email",email);
                }
                if (!phone.isEmpty()) {
                    jsonObject.addProperty("mobile",phone);
                }


                dataJson.add("wpKMemberDto",jsonObject);


                RestAPI.PostDataWrappy(getApplicationContext(), dataJson, RestAPI.POST_REGISTER, new RestAPI.RestAPIListenner() {
                    @Override
                    public void OnInit() {

                    }

                    @Override
                    public void OnComplete(int httpCode, String error, String s) {
                        if (!RestAPI.checkHttpCode(httpCode)) {
                            AppFuncs.alert(getApplicationContext(),s,true);
                            return;
                        }
                        String url = RestAPI.loginUrl(user,password);
                        RestAPI.PostDataWrappy(getApplicationContext(), null, RestAPI.loginUrl(user,password), new RestAPI.RestAPIListenner() {
                            @Override
                            public void OnInit() {

                            }

                            @Override
                            public void OnComplete(int httpCode, String error, String s) {
                                if (!RestAPI.checkHttpCode(httpCode)) {
                                    AppFuncs.alert(getApplicationContext(),s,true);
                                    return;
                                }
                                String jID =  (new JsonParser()).parse(s).getAsJsonObject().get("jid").getAsString()+"@im.proteusiondev.com";
                                String jPass = (new JsonParser()).parse(s).getAsJsonObject().get("xmppPassword").getAsString();
                                doExistingAccountRegister(jID,jPass);
                            }
                        });


                    }
                });

            }
            if (view.getId() == btnSkip.getId()) {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();

            }
            if (view.getId() == btnCameraAvatar.getId()) {
                if (ContextCompat.checkSelfPermission(UpdateProfileActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {

                        AppFuncs.getImageFromDevice(this,IMAGE_AVATAR);

                    } else {
                        ActivityCompat.requestPermissions(UpdateProfileActivity.this,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                199);
                }
            }
            if (view.getId() == btnCameraHeader.getId()) {
                if (ContextCompat.checkSelfPermission(UpdateProfileActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {

                    AppFuncs.getImageFromDevice(this,IMAGE_HEADER);

                } else {
                    ActivityCompat.requestPermissions(UpdateProfileActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            199);
                }
            }
        }catch (Exception ex) {
            ex.printStackTrace();
        }finally {
            isFlag = false;
        }
    }

    private String validateData() {
        String error = "";
        try {
            user = edUsername.getText().toString().trim();
            email = edEmail.getText().toString().trim();
            phone = edPhone.getText().toString().trim();
            if (user.isEmpty()) {
                error = "Username is empty";
            } else if (user.length() < 6) {
                error = "Length of username must be more than 6 characters";
            } else if (AppFuncs.detectSpecialCharacters(user)) {
                error = "Username contains special characters";
            }
//            else if (email.isEmpty()) {
//                error = "Email is empty";
//            } else if (!AppFuncs.isEmailValid(email)) {
//                error = "Invalid email format";
//            } else {}
        }catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            return error;
        }
    }

    ExistingAccountTask mExistingAccountTask;
    SimpleAlertHandler mHandler;

    private void doExistingAccountRegister (String username , String password)
    {

        if (mExistingAccountTask == null) {
            mExistingAccountTask = new ExistingAccountTask();
            mExistingAccountTask.execute(username, password);
        }
    }

    private class ExistingAccountTask extends AsyncTask<String, Void, OnboardingAccount> {
        @Override
        protected OnboardingAccount doInBackground(String... account) {
            try {

                OtrAndroidKeyManagerImpl keyMan = OtrAndroidKeyManagerImpl.getInstance(UpdateProfileActivity.this);
                KeyPair keyPair = keyMan.generateLocalKeyPair();

                String nickname = new XmppAddress(account[0]).getUser();
                OnboardingAccount result = OnboardingManager.addExistingAccount(UpdateProfileActivity.this, mHandler, nickname, account[0], account[1]);

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

            SignInHelper signInHelper = new SignInHelper(UpdateProfileActivity.this, mHandler);
            signInHelper.activateAccount(account.providerId,account.accountId);
            signInHelper.signIn(account.password, account.providerId, account.accountId, true);

            mExistingAccountTask = null;

            Intent intent = new Intent(UpdateProfileActivity.this, MainActivity.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (data!=null) {
                if (requestCode==IMAGE_HEADER) {
                    Bitmap bmpThumbnail = SecureMediaStore.getThumbnailFile(UpdateProfileActivity.this, data.getData(), 512);
                    imgHeader.setImageBitmap(bmpThumbnail);
                } else if (requestCode == IMAGE_AVATAR) {
                    Bitmap bmpThumbnail = SecureMediaStore.getThumbnailFile(UpdateProfileActivity.this, data.getData(), 512);
                    imgAvatar.setImageBitmap(bmpThumbnail);
                }
            }
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }
}
