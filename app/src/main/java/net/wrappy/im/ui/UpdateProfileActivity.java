package net.wrappy.im.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatSpinner;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Response;
import com.yalantis.ucrop.UCrop;

import net.wrappy.im.ImApp;
import net.wrappy.im.MainActivity;
import net.wrappy.im.R;
import net.wrappy.im.crypto.otr.OtrAndroidKeyManagerImpl;
import net.wrappy.im.helper.AppFuncs;
import net.wrappy.im.helper.RestAPI;
import net.wrappy.im.helper.layout.CircleImageView;
import net.wrappy.im.model.Avatar;
import net.wrappy.im.model.Banner;
import net.wrappy.im.model.BottomSheetCell;
import net.wrappy.im.model.BottomSheetListener;
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
import net.wrappy.im.util.PopupUtils;

import java.io.File;
import java.lang.reflect.Type;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;

/**
 * Created by ben on 15/11/2017.
 */

public class UpdateProfileActivity extends BaseActivity implements View.OnClickListener {

    private final int IMAGE_HEADER = 100;
    private final int IMAGE_AVATAR = 101;
    private final int IMAGE_HEADER_UCROP = 102;
    private final int IMAGE_AVATAR_UCROP = 103;


    boolean isFlag;
    String user, email, phone, gender, password;
    Registration registrationData;
    AppFuncs appFuncs;

    @BindView(R.id.spnProfileCountryCodes)
    AppCompatSpinner spnProfileCountryCodes;
    @BindView(R.id.imgPhotoAvatar)
    CircleImageView imgAvatar;
    @BindView(R.id.imgProfileHeader)
    ImageView imgHeader;
    @BindView(R.id.edProfileUsername)
    EditText edUsername;
    @BindView(R.id.edProfileEmail)
    EditText edEmail;
    @BindView(R.id.edProfilePhone)
    EditText edPhone;
    @BindView(R.id.spinnerProfileGender)
    AppCompatSpinner spinnerProfileGender;
    @BindView(R.id.btnProfileCameraHeader)
    ImageButton btnCameraHeader;
    @BindView(R.id.btnPhotoCameraAvatar)
    ImageButton btnCameraAvatar;

    MySpinnerAdapter countryAdapter;
    ArrayAdapter<CharSequence> adapterGender;
    String avatarReference, bannerReference;
    List<WpkCountry> wpkCountry;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.update_profile_activity);
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
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
                    if (s != null) {
                        Type listType = new TypeToken<ArrayList<WpkCountry>>() {
                        }.getType();
                        wpkCountry = new Gson().fromJson(s, listType);
                        wpkCountry.get(0).getCode();
                        ArrayList<String> strings = new ArrayList<>();
                        for (int i = 0; i < wpkCountry.size(); i++) {
                            strings.add(wpkCountry.get(i).getL10N().get(WpkCountry.country_en_US) + " " + wpkCountry.get(i).getPrefix());
                        }
                        countryAdapter = new ArrayAdapter<String>(UpdateProfileActivity.this, R.layout.update_profile_textview, strings) {
                            @NonNull
                            @Override
                            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                final View v = vi.inflate(android.R.layout.simple_spinner_item, null);
                                final TextView t = (TextView)v.findViewById(android.R.id.text1);
                                t.setText(wpkCountry.get(position).getPrefix());
                                return v;
                            }
                        };
                        spnProfileCountryCodes.setAdapter(countryAdapter);
                    }
                } catch (Exception ex) {
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

            if (uriAvatar != null && TextUtils.isEmpty(avatarReference)) {
                AppFuncs.log("avatar null");
                isFileFail = true;
            }
            if (uriHeader != null && TextUtils.isEmpty(bannerReference)) {
                AppFuncs.log("banner null");
                isFileFail = true;
            }
            AppFuncs.log("check");
            if (!isFileFail) {
                postDataToServer();
            } else {
                AppFuncs.log("update success");
                AppFuncs.alert(getApplicationContext(), "Upload File Fail", true);
            }
        }
    };

    @Optional
    @OnClick({R.id.btnProfileComplete, R.id.btnProfileCameraHeader, R.id.btnPhotoCameraAvatar})
    @Override
    public void onClick(View view) {
        if (isFlag) {
            return;
        }
        isFlag = true;
        try {
            if (view.getId() == R.id.btnProfileComplete) {
                String error = validateData();
                if (!TextUtils.isEmpty(error)) {
                    AppFuncs.alert(getApplicationContext(), error, true);
                    return;
                }
                appFuncs.showProgressWaiting(this);
                boolean isFileExist = false;
                if (uriAvatar != null) {
                    isFileExist = true;
                    AppFuncs.log("Upload Avatar");
                    uploadFileProfile(uriAvatar, RestAPI.PHOTO_AVATAR);
                }
                if (uriHeader != null) {
                    isFileExist = true;
                    AppFuncs.log("Upload Banner");
                    uploadFileProfile(uriHeader, RestAPI.PHOTO_BRAND);
                }
                if (!isFileExist) {
                    postDataToServer();
                } else {
                    handler.postDelayed(runnablePostData, 10000);
                }
            }
            if (view.getId() == R.id.btnPhotoCameraAvatar) {
                ArrayList<BottomSheetCell> sheetCells = new ArrayList<>();
                BottomSheetCell sheetCell = new BottomSheetCell(1, R.drawable.ic_choose_camera, getString(R.string.popup_take_photo));
                sheetCells.add(sheetCell);
                sheetCell = new BottomSheetCell(2, R.drawable.ic_choose_gallery, getString(R.string.popup_choose_gallery));
                sheetCells.add(sheetCell);
                if (uriAvatar != null) {
                    sheetCell = new BottomSheetCell(3, R.drawable.setting_delete, getString(R.string.popup_delete_photo));
                    sheetCells.add(sheetCell);
                }
                PopupUtils.createBottomSheet(this, sheetCells, new BottomSheetListener() {
                    @Override
                    public void onSelectBottomSheetCell(int index) {
                        switch (index) {
                            case 1:
                                AppFuncs.openCamera(UpdateProfileActivity.this, IMAGE_AVATAR);
                                break;
                            case 2:
                                AppFuncs.openGallery(UpdateProfileActivity.this, IMAGE_AVATAR);
                                break;
                            case 3:
                                uriAvatar = null;
                                imgAvatar.setImageResource(R.drawable.avatar);
                                break;
                            default:
                        }
                    }
                }).show();
            }
            if (view.getId() == R.id.btnProfileCameraHeader) {
                ArrayList<BottomSheetCell> sheetCells = new ArrayList<>();
                BottomSheetCell sheetCell = new BottomSheetCell(1, R.drawable.ic_choose_camera, "Take Photo");
                sheetCells.add(sheetCell);
                sheetCell = new BottomSheetCell(2, R.drawable.ic_choose_gallery, "Choose from Gallery");
                sheetCells.add(sheetCell);
                if (uriHeader != null) {
                    sheetCell = new BottomSheetCell(3, R.drawable.setting_delete, "Delete Photo");
                    sheetCells.add(sheetCell);
                }
                PopupUtils.createBottomSheet(this, sheetCells, new BottomSheetListener() {
                    @Override
                    public void onSelectBottomSheetCell(int index) {
                        switch (index) {
                            case 1:
                                AppFuncs.openCamera(UpdateProfileActivity.this, IMAGE_HEADER);
                                break;
                            case 2:
                                AppFuncs.openGallery(UpdateProfileActivity.this, IMAGE_HEADER);
                                break;
                            case 3:
                                uriHeader = null;
                                imgHeader.setImageBitmap(null);
                                break;
                            default:
                        }
                    }
                }).show();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            isFlag = false;
        }
    }

    private void uploadFileProfile(final Uri resultUri, final String type) {
        RestAPI.uploadFile(getApplicationContext(), new File(resultUri.getPath()), RestAPI.PHOTO_AVATAR).setCallback(new FutureCallback<Response<String>>() {
            @Override
            public void onCompleted(Exception e, Response<String> result) {

                try {
                    String reference = RestAPI.getPhotoReference(result.getResult());
                    AppFuncs.log("Upload " + reference);
                    if (result != null) {
                        if (type == RestAPI.PHOTO_AVATAR) {
                            avatarReference = reference;
                        } else {
                            bannerReference = reference;
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private void postDataToServer() {
        AppFuncs.log("postDataToServer");
        WpKMemberDto wpKMemberDto = new WpKMemberDto(user, email, phone, adapterGender.getItem(spinnerProfileGender.getSelectedItemPosition()).toString().toUpperCase());
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
                    if (s != null) {
                        String er = WpErrors.getErrorMessage(s);
                        if (!TextUtils.isEmpty(er)) {
                            AppFuncs.alert(getApplicationContext(), er, true);
                        } else {
                            AppFuncs.alert(getApplicationContext(), "Registration fail. Try Again!", true);
                        }
                    }
                    appFuncs.dismissProgressWaiting();
                    return;
                }
                Store.putStringData(getApplicationContext(), Store.USERNAME, user);
                String url = RestAPI.loginUrl(user, password);
                AppFuncs.log(url);
                RestAPI.PostDataWrappy(getApplicationContext(), null, url, new RestAPI.RestAPIListenner() {

                    @Override
                    public void OnComplete(int httpCode, String error, String s) {
                        try {
                            if (!RestAPI.checkHttpCode(httpCode)) {
                                String er = WpErrors.getErrorMessage(s);
                                if (!TextUtils.isEmpty(er)) {
                                    AppFuncs.alert(getApplicationContext(), er, true);
                                }
                                appFuncs.dismissProgressWaiting();
                                return;
                            }
                            AppFuncs.log("login");
                            JsonObject jsonObject = (new JsonParser()).parse(s).getAsJsonObject();
                            Gson gson = new Gson();
                            WpkToken wpkToken = gson.fromJson(jsonObject, WpkToken.class);
                            wpkToken.saveToken(getApplicationContext());
                            doExistingAccountRegister(wpkToken.getJid() + Constant.EMAIL_DOMAIN, wpkToken.getXmppPassword());
                        } catch (Exception ex) {
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
            } else if (!TextUtils.isEmpty(email) && !AppFuncs.isEmailValid(email)) {
                error = "Invalid email format";
            } else {
            }

            if (TextUtils.isEmpty(email)) {
                email = null;
            }
            if (TextUtils.isEmpty(phone)) {
                phone = null;
            } else {
                if (wpkCountry!=null) {
                    String countryName = wpkCountry.get(spnProfileCountryCodes.getSelectedItemPosition()).getPrefix();
                    phone = countryName + phone;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            return error;
        }
    }

    ExistingAccountTask mExistingAccountTask;
    SimpleAlertHandler mHandler;

    private void doExistingAccountRegister(String username, String password) {
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
                OnboardingAccount result = OnboardingManager.addExistingAccount(UpdateProfileActivity.this, mHandler, account);

                if (result != null) {
                    String jabberId = result.username + '@' + result.domain;
                    keyMan.storeKeyPair(jabberId, keyPair);
                }

                return result;
            } catch (Exception e) {
                Log.e(ImApp.LOG_TAG, "auto onboarding fail", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(OnboardingAccount account) {

            // mUsername = account.username + '@' + account.domain;
            appFuncs.dismissProgressWaiting();
            ImApp mApp = (ImApp) getApplication();
            mApp.setDefaultAccount(account.providerId, account.accountId);

            SignInHelper signInHelper = new SignInHelper(UpdateProfileActivity.this, mHandler);
            signInHelper.activateAccount(account.providerId, account.accountId);
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

    Uri uriHeader, uriAvatar;
    boolean isAvatarRequest = false;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {

        try {
            if (data != null) {
                if (requestCode == IMAGE_HEADER) {
                    isAvatarRequest = false;
                    AppFuncs.cropImage(this, data.getData(), false);
                } else if (requestCode == IMAGE_AVATAR) {
                    isAvatarRequest = true;
                    AppFuncs.cropImage(this, data.getData(), true);
                } else if (requestCode == UCrop.REQUEST_CROP) {
                    if (resultCode == UCrop.RESULT_ERROR) {
                        final Throwable cropError = UCrop.getError(data);
                        AppFuncs.log(cropError.getLocalizedMessage());
                        return;
                    }
                    if (isAvatarRequest) {
                        uriAvatar = UCrop.getOutput(data);
                        imgAvatar.setImageURI(uriAvatar);
                    } else {
                        uriHeader = UCrop.getOutput(data);
                        imgHeader.setImageURI(uriHeader);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            LauncherActivity.start(UpdateProfileActivity.this);
            finish();
        }
        return true;
    }

    //custom adapter
    public class MySpinnerAdapter extends ArrayAdapter<WpkCountry>{

        private Context context;
        private List<WpkCountry> myObjs;

        public MySpinnerAdapter(Context context, int textViewResourceId,
                                List<WpkCountry> myObjs) {
            super(context,textViewResourceId,myObjs);
            this.context = context;
            this.myObjs = myObjs;
        }

        public int getCount(){
            return myObjs.size();
        }

        public WpkCountry getItem(int position){
            return myObjs.get(position);
        }

        public long getItemId(int position){
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView label = new TextView(context);
            label.setText(myObjs.get(position).getPrefix());
            return label;
        }

        @Override
        public View getDropDownView(int position, View convertView,
                                    ViewGroup parent) {
            TextView label = new TextView(context);
            label.setText(myObjs.get(position).getL10N().get("en_US") + " " + myObjs.get(position).getPrefix());
            return label;
        }
    }


}
