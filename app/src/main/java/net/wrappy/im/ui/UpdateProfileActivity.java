package net.wrappy.im.ui;

import android.Manifest;
import android.app.ProgressDialog;
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

import com.google.gson.Gson;
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
import net.wrappy.im.model.Registration;
import net.wrappy.im.model.SecurityQuestions;
import net.wrappy.im.model.WpKAuthDto;
import net.wrappy.im.model.WpKMemberDto;
import net.wrappy.im.model.WpkToken;
import net.wrappy.im.plugin.xmpp.XmppAddress;
import net.wrappy.im.ui.legacy.SignInHelper;
import net.wrappy.im.ui.legacy.SimpleAlertHandler;
import net.wrappy.im.ui.onboarding.OnboardingAccount;
import net.wrappy.im.ui.onboarding.OnboardingManager;
import net.wrappy.im.util.Constant;
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
    String user,email,phone,gender,password;
    ProgressDialog dialog;
    Registration registrationData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.update_profile_activity);
        getSecurityQuestions();
        mHandler = new SimpleAlertHandler(this);
        preferenceView();
    }

    private void getSecurityQuestions() {
        registrationData = new Registration();
        WpKAuthDto wpKAuthDto = getIntent().getParcelableExtra(WpKAuthDto.class.getName());
        ArrayList<SecurityQuestions> securityQuestions = getIntent().getParcelableArrayListExtra(SecurityQuestions.class.getName());
        registrationData.setWpKAuthDto(wpKAuthDto);
        password = wpKAuthDto.getSecret();
        registrationData.setSecurityQuestions(securityQuestions);
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
                dialog = new ProgressDialog(UpdateProfileActivity.this);
                dialog.setMessage(getString(R.string.waiting_dialog));
                dialog.show();
                WpKMemberDto wpKMemberDto = new WpKMemberDto(user,email,phone);

                registrationData.setWpKMemberDto(wpKMemberDto);

                Gson gson = new Gson();
                JsonObject dataJson = gson.toJsonTree(registrationData).getAsJsonObject();


                RestAPI.PostDataWrappy(getApplicationContext(), dataJson, RestAPI.POST_REGISTER, new RestAPI.RestAPIListenner() {

                    @Override
                    public void OnComplete(int httpCode, String error, String s) {
                        String url = RestAPI.loginUrl(user,password);
                        RestAPI.PostDataWrappy(getApplicationContext(), null, url, new RestAPI.RestAPIListenner() {

                            @Override
                            public void OnComplete(int httpCode, String error, String s) {
                                try {
                                    if (error!=null && !error.isEmpty()) {
                                        AppFuncs.alert(getApplicationContext(),error,true);
                                        if (dialog != null && dialog.isShowing()) {
                                            dialog.dismiss();
                                        }
                                        return;
                                    }
                                    JsonObject jsonObject = (new JsonParser()).parse(s).getAsJsonObject();
                                    Gson gson = new Gson();
                                    WpkToken wpkToken = gson.fromJson(jsonObject, WpkToken.class);
                                    wpkToken.saveToken(getApplicationContext());
                                    doExistingAccountRegister(wpkToken.getJid()+ Constant.EMAIL_DOMAIN,wpkToken.getXmppPassword());
                                }catch (Exception ex) {
                                    if (dialog != null && dialog.isShowing()) {
                                        dialog.dismiss();
                                    }
                                    ex.printStackTrace();
                                }
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
                        Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED) {

                        AppFuncs.getImageFromDevice(this,IMAGE_AVATAR);

                    } else {
                        ActivityCompat.requestPermissions(UpdateProfileActivity.this,
                                new String[]{Manifest.permission.CAMERA},
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
            else if (!email.isEmpty() && !AppFuncs.isEmailValid(email)) {
                error = "Invalid email format";
            } else {}

            if (email.isEmpty()) {
                email = null;
            }
            if (phone.isEmpty()) {
                phone = null;
            }
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
            mExistingAccountTask.execute(username, password, edUsername.getText().toString());
        }
    }

    private class ExistingAccountTask extends AsyncTask<String, Void, OnboardingAccount> {
        @Override
        protected OnboardingAccount doInBackground(String... account) {
            try {

                OtrAndroidKeyManagerImpl keyMan = OtrAndroidKeyManagerImpl.getInstance(UpdateProfileActivity.this);
                KeyPair keyPair = keyMan.generateLocalKeyPair();

                String nickname = new XmppAddress(account[0]).getUser();
                OnboardingAccount result = OnboardingManager.addExistingAccount(UpdateProfileActivity.this, mHandler, nickname, account[0], account[1], account[2]);

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
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
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
                Bitmap photo;
                if (requestCode==IMAGE_HEADER) {
                    if (data.getData()!=null) {
                        photo = SecureMediaStore.getThumbnailFile(UpdateProfileActivity.this, data.getData(), 512);
                    } else {
                        photo = (Bitmap) data.getExtras().get("data");
                    }
                    imgHeader.setImageBitmap(photo);
                } else if (requestCode == IMAGE_AVATAR) {
                    if (data.getData()!=null) {
                        photo = SecureMediaStore.getThumbnailFile(UpdateProfileActivity.this, data.getData(), 512);
                    } else {
                        photo = (Bitmap) data.getExtras().get("data");
                    }
                    imgAvatar.setImageBitmap(photo);
                }
            }
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
