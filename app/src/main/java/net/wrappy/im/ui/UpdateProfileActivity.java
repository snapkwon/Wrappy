package net.wrappy.im.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.AppCompatSpinner;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

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
import net.wrappy.im.helper.layout.CircleImageView;
import net.wrappy.im.model.Avatar;
import net.wrappy.im.model.Banner;
import net.wrappy.im.model.Registration;
import net.wrappy.im.model.RegistrationAccount;
import net.wrappy.im.model.SecurityQuestions;
import net.wrappy.im.model.WpErrors;
import net.wrappy.im.model.WpKAuthDto;
import net.wrappy.im.model.WpKMemberDto;
import net.wrappy.im.model.WpkCountry;
import net.wrappy.im.model.WpkToken;
import net.wrappy.im.provider.Imps;
import net.wrappy.im.provider.Store;
import net.wrappy.im.ui.legacy.DatabaseUtils;
import net.wrappy.im.ui.legacy.SignInHelper;
import net.wrappy.im.ui.legacy.SimpleAlertHandler;
import net.wrappy.im.ui.onboarding.OnboardingAccount;
import net.wrappy.im.ui.onboarding.OnboardingManager;
import net.wrappy.im.util.Constant;

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
    @BindView(R.id.imgPhotoAvatar) CircleImageView imgAvatar;
    @BindView(R.id.imgProfileHeader) ImageView imgHeader;
    @BindView(R.id.edProfileUsername) EditText edUsername;
    @BindView(R.id.edProfileEmail) EditText edEmail;
    @BindView(R.id.edProfilePhone) EditText edPhone;
    @BindView(R.id.spinnerProfileGender) AppCompatSpinner spinnerProfileGender;
    @BindView(R.id.btnProfileCameraHeader) ImageButton btnCameraHeader;
    @BindView(R.id.btnPhotoCameraAvatar) ImageButton btnCameraAvatar;

    ArrayAdapter<String> countryAdapter;
    ArrayAdapter<CharSequence> adapterGender;
    String avatarReference, bannerReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.update_profile_activity);
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_ab_arrow_back);
        getSupportActionBar().setTitle("Update Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        appFuncs = AppFuncs.getInstance();
        btnCameraAvatar.setVisibility(View.VISIBLE);
        btnCameraHeader.setVisibility(View.VISIBLE);
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
        adapterGender = ArrayAdapter.createFromResource(this,
                R.array.profile_gender, R.layout.update_profile_textview);
        spinnerProfileGender.setAdapter(adapterGender);
        getCountryCodesFromServer();
    }

    private void getCountryCodesFromServer() {
        RestAPI.GetDataWrappy(getApplicationContext(), RestAPI.GET_COUNTRY_CODES, new RestAPI.RestAPIListenner() {
            @Override
            public void OnComplete(int httpCode, String error, String s) {
                try {
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
                }catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
    Handler handler = new Handler();
    Runnable runnablePostData = new Runnable() {
        @Override
        public void run() {
            appFuncs.dismissProgressWaiting();
            boolean isFileFail = false;

             if (photoAvatar!=null && TextUtils.isEmpty(avatarReference)) {
                 AppFuncs.log("avatar null");
                 isFileFail = true;
             }
             if (photoHeader!=null && TextUtils.isEmpty(bannerReference)) {
                 AppFuncs.log("banner null");
                 isFileFail = true;
             }
            AppFuncs.log("check");
             if (!isFileFail) {
                 postDataToServer();
             } else {
                 AppFuncs.log("update success");
                 AppFuncs.alert(getApplicationContext(),"Upload File Fail", true);
             }
        }
    };

    @Optional
    @OnClick({R.id.btnProfileComplete, R.id.btnProfileCameraHeader, R.id.btnPhotoCameraAvatar, R.id.btnProfileSkip})
    @Override
    public void onClick(View view) {
        if (isFlag) {
            return;
        } isFlag = true;
        try {
            if (view.getId() == R.id.btnProfileComplete) {
                String error = validateData();
                if (!TextUtils.isEmpty(error)) {
                    AppFuncs.alert(getApplicationContext(),error,true);
                    return;
                }
                appFuncs.showProgressWaiting(this);
                boolean isFileExist = false;
                if (photoAvatar!=null) {
                    isFileExist = true;
                    AppFuncs.log("Upload Avatar");
                    uploadFileProfile(photoAvatar, RestAPI.PHOTO_AVATAR);
                }
                if (photoHeader!=null) {
                    isFileExist = true;
                    AppFuncs.log("Upload Banner");
                    uploadFileProfile(photoHeader, RestAPI.PHOTO_BRAND);
                }
                if (!isFileExist){
                    postDataToServer();
                } else {
                    handler.postDelayed(runnablePostData,10000);
                }
            }
            if (view.getId() == R.id.btnProfileSkip) {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            }
            if (view.getId() == R.id.btnPhotoCameraAvatar) {
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

    private void uploadFileProfile(final Bitmap bitmap, final String type) {
        RestAPI.uploadFile(getApplicationContext(),AppFuncs.convertBitmapToFile(getApplicationContext(),bitmap),RestAPI.PHOTO_AVATAR).setCallback(new FutureCallback<Response<String>>() {
            @Override
            public void onCompleted(Exception e, Response<String> result) {

                try {
                    String reference = RestAPI.getPhotoReference(result.getResult());
                    AppFuncs.log("Upload " + reference);
                    if (result!=null) {
                        if (type==RestAPI.PHOTO_AVATAR) {
                            avatarReference = reference;
                        } else {
                            bannerReference = reference;
                        }
                    }
                }catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private void postDataToServer(){
        AppFuncs.log("postDataToServer");
        WpKMemberDto wpKMemberDto = new WpKMemberDto(user,email,phone,adapterGender.getItem(spinnerProfileGender.getSelectedItemPosition()).toString().toUpperCase());
        wpKMemberDto.setIdentifier(user);
        wpKMemberDto.setEmail(email);
        wpKMemberDto.setMobile(phone);
        wpKMemberDto.setGender(adapterGender.getItem(spinnerProfileGender.getSelectedItemPosition()).toString().toUpperCase());

        if (!TextUtils.isEmpty(avatarReference)) {
            Avatar avatar = new Avatar(avatarReference);
            wpKMemberDto.setAvatar(avatar);
        }
        if (!TextUtils.isEmpty(bannerReference)) {
            Banner banner = new Banner(bannerReference);
            wpKMemberDto.setBanner(banner);
        }

        registrationData.setWpKMemberDto(wpKMemberDto);

        Gson gson = new Gson();
        JsonObject dataJson = gson.toJsonTree(registrationData).getAsJsonObject();
        AppFuncs.log(dataJson.toString());
        RestAPI.PostDataWrappy(getApplicationContext(), dataJson, RestAPI.POST_REGISTER_DEV, new RestAPI.RestAPIListenner() {

            @Override
            public void OnComplete(int httpCode, String error, String s) {
                if (!RestAPI.checkHttpCode(httpCode)) {
                    if (s!=null) {
                        String er = WpErrors.getErrorMessage(s);
                        if (!TextUtils.isEmpty(er)) {
                            AppFuncs.alert(getApplicationContext(),er,true);
                        } else {
                            AppFuncs.alert(getApplicationContext(),"Registration fail. Try Again!",true);
                        }
                    }
                    appFuncs.dismissProgressWaiting();
                    return;
                }
                Store.putStringData(getApplicationContext(),Store.USERNAME,user);
                String url = RestAPI.loginUrl(user,password);
                AppFuncs.log(url);
                RestAPI.PostDataWrappy(getApplicationContext(), null, url, new RestAPI.RestAPIListenner() {

                    @Override
                    public void OnComplete(int httpCode, String error, String s) {
                        try {
                            if (!RestAPI.checkHttpCode(httpCode)) {
                                String er = WpErrors.getErrorMessage(s);
                                if (!TextUtils.isEmpty(er)) {
                                    AppFuncs.alert(getApplicationContext(),er,true);
                                }
                                appFuncs.dismissProgressWaiting();
                                return;
                            }
                            AppFuncs.log("login");
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

    private String validateData() {
        String error = "";
        try {
            user = edUsername.getText().toString().trim();
            email = edEmail.getText().toString().trim();
            phone = edPhone.getText().toString().trim();

            if (TextUtils.isEmpty(user)) {
                error = "Username is empty";
            } else if (user.length() < 6) {
                error = "Length of username must be more than 6 characters";
            } else if (AppFuncs.detectSpecialCharacters(user)) {
                error = "Username contains special characters";
            }
            else if (!TextUtils.isEmpty(email) && !AppFuncs.isEmailValid(email)) {
                error = "Invalid email format";
            } else {}

            if (TextUtils.isEmpty(email)) {
                email = null;
            }
            if (TextUtils.isEmpty(phone)) {
                phone = null;
            } else {
                phone = countryAdapter.getItem(spnProfileCountryCodes.getSelectedItemPosition()) + phone;
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

            try {
                DatabaseUtils.insertAvatarBlob(getContentResolver(), Imps.Avatars.CONTENT_URI, account.providerId, account.accountId, avatarReference, bannerReference, account.username);
            } catch (Exception e) {
                e.printStackTrace();
            }

            mExistingAccountTask = null;

            Intent intent = new Intent(UpdateProfileActivity.this, MainActivity.class);
            startActivity(intent);
        }
    }
    Bitmap photoAvatar, photoHeader;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (data!=null) {
                if (requestCode==IMAGE_HEADER) {
                    photoHeader = AppFuncs.getBitmapFromIntentResult(UpdateProfileActivity.this,data);
                    imgHeader.setImageBitmap(photoHeader);
                } else if (requestCode == IMAGE_AVATAR) {
                    photoAvatar = AppFuncs.getBitmapFromIntentResult(UpdateProfileActivity.this,data);
                    imgAvatar.setImageBitmap(photoAvatar);
                }
            }
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==android.R.id.home) {
            LauncherActivity.start(UpdateProfileActivity.this);
            finish();
        }
        return true;
    }
}
