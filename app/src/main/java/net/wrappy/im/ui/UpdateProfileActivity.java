package net.wrappy.im.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.AppCompatSpinner;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import net.wrappy.im.ImApp;
import net.wrappy.im.MainActivity;
import net.wrappy.im.R;
import net.wrappy.im.crypto.otr.OtrAndroidKeyManagerImpl;
import net.wrappy.im.helper.AppFuncs;
import net.wrappy.im.helper.RestAPI;
import net.wrappy.im.helper.layout.AppTextView;
import net.wrappy.im.helper.layout.CircleImageView;
import net.wrappy.im.model.Registration;
import net.wrappy.im.model.RegistrationAccount;
import net.wrappy.im.model.SecurityQuestions;
import net.wrappy.im.model.WpErrors;
import net.wrappy.im.model.WpKAuthDto;
import net.wrappy.im.model.WpKMemberDto;
import net.wrappy.im.model.WpkCountry;
import net.wrappy.im.model.WpkToken;
import net.wrappy.im.ui.legacy.SignInHelper;
import net.wrappy.im.ui.legacy.SimpleAlertHandler;
import net.wrappy.im.ui.onboarding.OnboardingAccount;
import net.wrappy.im.ui.onboarding.OnboardingManager;
import net.wrappy.im.util.Constant;
import net.wrappy.im.util.Debug;
import net.wrappy.im.util.SecureMediaStore;

import java.lang.reflect.Type;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.Optional;

/**
 * Created by ben on 15/11/2017.
 */

public class UpdateProfileActivity extends BaseActivity implements View.OnClickListener {

    private final int IMAGE_HEADER = 100;
    private final int IMAGE_AVATAR = 101;


    boolean isFlag;
    String user,email,phone,gender,password;
    Registration registrationData;
    AppFuncs appFuncs;

    @BindView(R.id.spnProfileCountryCodes) AppCompatSpinner spnProfileCountryCodes;
    @BindView(R.id.headerbarTitle) AppTextView headerbarTitle;
    @BindView(R.id.imgProfileAvatar) CircleImageView imgAvatar;
    @BindView(R.id.imgProfileHeader) ImageView imgHeader;
    @BindView(R.id.edProfileUsername) EditText edUsername;
    @BindView(R.id.edProfileEmail) EditText edEmail;
    @BindView(R.id.edProfilePhone) EditText edPhone;
    @BindView(R.id.spinnerProfileGender) AppCompatSpinner spinnerProfileGender;

    ArrayAdapter<String> countryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.update_profile_activity);
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        appFuncs = AppFuncs.getInstance();
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
        headerbarTitle.setText("Update Profile");
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.profile_gender, R.layout.update_profile_textview);
        spinnerProfileGender.setAdapter(adapter);
        getCountryCodesFromServer();
    }

    private void getCountryCodesFromServer() {
        RestAPI.GetDataWrappy(getApplicationContext(), RestAPI.GET_COUNTRY_CODES, new RestAPI.RestAPIListenner() {
            @Override
            public void OnComplete(int httpCode, String error, String s) {
                if (s!=null) {
                    Type listType = new TypeToken<ArrayList<WpkCountry>>(){}.getType();
                    List<WpkCountry> wpkCountry = new Gson().fromJson(s, listType);
                    wpkCountry.get(0).getCode();
                    ArrayList<String> strings = new ArrayList<>();
                    for (int i=0; i < wpkCountry.size(); i++) {
                        strings.add(wpkCountry.get(i).getPrefix());
                    }
                    countryAdapter = new ArrayAdapter<String>(UpdateProfileActivity.this, R.layout.update_profile_textview, strings);
                    spnProfileCountryCodes.setAdapter(countryAdapter);
                }
            }
        });
    }


    @Optional
    @OnClick({R.id.headerbarBack, R.id.btnProfileComplete, R.id.btnProfileCameraHeader, R.id.btnProfileCameraAvatar, R.id.btnProfileSkip})
    @Override
    public void onClick(View view) {
        if (isFlag) {
            return;
        } isFlag = true;
        try {
            if (view.getId() == R.id.headerbarBack) {
                LauncherActivity.start(UpdateProfileActivity.this);
                finish();
            }
            if (view.getId() == R.id.btnProfileComplete) {
                String error = validateData();
                if (!error.isEmpty()) {
                    AppFuncs.alert(getApplicationContext(),error,true);
                    return;
                }
                phone = countryAdapter.getItem(spnProfileCountryCodes.getSelectedItemPosition()) + phone;
                appFuncs.showProgressWaiting(this);
                WpKMemberDto wpKMemberDto = new WpKMemberDto(user,email,phone);

                registrationData.setWpKMemberDto(wpKMemberDto);

                Gson gson = new Gson();
                JsonObject dataJson = gson.toJsonTree(registrationData).getAsJsonObject();
                Debug.d(dataJson.toString());

                RestAPI.PostDataWrappy(getApplicationContext(), dataJson, RestAPI.POST_REGISTER_DEV, new RestAPI.RestAPIListenner() {

                    @Override
                    public void OnComplete(int httpCode, String error, String s) {
                        Debug.d(s);
                        if (error!=null && !error.isEmpty()) {
                            AppFuncs.alert(getApplicationContext(),error,true);
                            appFuncs.dismissProgressWaiting();
                            return;
                        }
                        if (!RestAPI.checkHttpCode(httpCode)) {
                            if (s!=null) {
                                String er = WpErrors.getErrorMessage(s);
                                if (!er.isEmpty()) {
                                    AppFuncs.alert(getApplicationContext(),er,true);
                                } else {
                                    AppFuncs.alert(getApplicationContext(),"Registration fail. Try Again!",true);
                                }
                            }
                            appFuncs.dismissProgressWaiting();
                            return;
                        }
                        String url = RestAPI.loginUrl(user,password);
                        RestAPI.PostDataWrappy(getApplicationContext(), null, url, new RestAPI.RestAPIListenner() {

                            @Override
                            public void OnComplete(int httpCode, String error, String s) {
                                try {

                                    if (error!=null && !error.isEmpty()) {
                                        AppFuncs.alert(getApplicationContext(),error,true);
                                        appFuncs.dismissProgressWaiting();
                                        return;
                                    }
                                    if (!RestAPI.checkHttpCode(httpCode)) {
                                        String er = WpErrors.getErrorMessage(s);
                                        if (!er.isEmpty()) {
                                            AppFuncs.alert(getApplicationContext(),er,true);
                                        }
                                        appFuncs.dismissProgressWaiting();
                                        return;
                                    }
                                    JsonObject jsonObject = (new JsonParser()).parse(s).getAsJsonObject();
                                    Gson gson = new Gson();
                                    WpkToken wpkToken = gson.fromJson(jsonObject, WpkToken.class);
                                    wpkToken.saveToken(getApplicationContext());
                                    doExistingAccountRegister(wpkToken.getJid()+ Constant.EMAIL_DOMAIN,wpkToken.getXmppPassword());
                                }catch (Exception ex) {
                                    appFuncs.dismissProgressWaiting();
                                    ex.printStackTrace();
                                }
                            }
                        });
                    }
                });

            }
            if (view.getId() == R.id.btnProfileSkip) {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            }
            if (view.getId() == R.id.btnProfileCameraAvatar) {
                AppFuncs.getImageFromDevice(this,IMAGE_AVATAR);
            }
            if (view.getId() == R.id.btnProfileCameraHeader) {
                AppFuncs.getImageFromDevice(this,IMAGE_HEADER);
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
        RegistrationAccount account = new RegistrationAccount(username, password);
        account.setNickname(edUsername.getText().toString());
        account.setEmail(edEmail.getText().toString());
        account.setPhone(edPhone.getText().toString());
        account.setGender(gender);

        if (mExistingAccountTask == null) {
            mExistingAccountTask = new ExistingAccountTask();
            mExistingAccountTask.execute(account);
        }
    }

    private class ExistingAccountTask extends AsyncTask<RegistrationAccount, Void, OnboardingAccount> {
        @Override
        protected OnboardingAccount doInBackground(RegistrationAccount... accounts) {
            try {

                OtrAndroidKeyManagerImpl keyMan = OtrAndroidKeyManagerImpl.getInstance(UpdateProfileActivity.this);
                KeyPair keyPair = keyMan.generateLocalKeyPair();

                RegistrationAccount account = accounts[0];
                OnboardingAccount result = OnboardingManager.addExistingAccount(UpdateProfileActivity.this, mHandler,account);

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
            appFuncs.dismissProgressWaiting();
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
