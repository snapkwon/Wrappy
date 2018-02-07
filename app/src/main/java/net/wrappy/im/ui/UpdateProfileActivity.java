package net.wrappy.im.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatSpinner;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Response;
import com.yalantis.ucrop.UCrop;

import net.wrappy.im.R;
import net.wrappy.im.helper.AppFuncs;
import net.wrappy.im.helper.RestAPI;
import net.wrappy.im.helper.RestAPIListener;
import net.wrappy.im.helper.layout.AppEditTextView;
import net.wrappy.im.helper.layout.AppTextView;
import net.wrappy.im.helper.layout.CircleImageView;
import net.wrappy.im.model.Avatar;
import net.wrappy.im.model.Banner;
import net.wrappy.im.model.BottomSheetCell;
import net.wrappy.im.model.BottomSheetListener;
import net.wrappy.im.model.Registration;
import net.wrappy.im.model.SecurityQuestions;
import net.wrappy.im.model.WpKAuthDto;
import net.wrappy.im.model.WpKMemberDto;
import net.wrappy.im.model.WpkCountry;
import net.wrappy.im.model.WpkToken;
import net.wrappy.im.provider.Store;
import net.wrappy.im.ui.legacy.SimpleAlertHandler;
import net.wrappy.im.util.PopupUtils;
import net.wrappy.im.util.Utils;

import java.io.File;
import java.lang.reflect.Type;
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

    private final int VERIFY_CODE = 104;

    public static final String PASSCODE = "passcode";

    boolean isFlag;
    String user, email, phone, gender, password, nickname;
    Registration registrationData;
    AppFuncs appFuncs;

    @BindView(R.id.spnProfileCountryCodes)
    AppCompatSpinner spnProfileCountryCodes;
    @BindView(R.id.imgPhotoAvatar)
    CircleImageView imgAvatar;
    @BindView(R.id.imgProfileHeader)
    ImageView imgHeader;
    @BindView(R.id.edProfileUsername)
    AppEditTextView edUsername;
    @BindView(R.id.edProfileEmail)
    AppEditTextView edEmail;
    @BindView(R.id.edProfilePhone)
    AppEditTextView edPhone;
    @BindView(R.id.spinnerProfileGender)
    AppCompatSpinner spinnerProfileGender;
    @BindView(R.id.spnProfileCountryCodesReference)
    AppCompatSpinner spnProfileCountryCodesReference;
    @BindView(R.id.btnProfileCameraHeader)
    ImageButton btnCameraHeader;
    @BindView(R.id.btnPhotoCameraAvatar)
    ImageButton btnCameraAvatar;
    @BindView(R.id.edProfileReferral)
    AppEditTextView edProfileReferral;
    @BindView(R.id.txtProfileMobile)
    AppTextView txtProfileMobile;
    @BindView(R.id.txtProfileUser)
    AppTextView txtProfileUser;
    @BindView(R.id.edProfileNickname) AppEditTextView edProfileNickname;
    @BindView(R.id.txtProfileNickname) AppTextView txtProfileNickname;

    ArrayAdapter countryAdapter;
    ArrayAdapter<CharSequence> adapterGender;
    String avatarReference, bannerReference;
    List<WpkCountry> wpkCountry;
    WpkToken wpkToken;
    String locale = "";
    String passcode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.update_profile_activity);
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        initActionBarDefault(true,R.string.update_profile);

        passcode =  getIntent().getStringExtra(UpdateProfileActivity.PASSCODE);

        ImageView backButton = (ImageView) findViewById(getResources().getIdentifier("action_bar_arrow_back", "id", getPackageName()));
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

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
        wpKAuthDto.setPasscode(passcode);
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
        txtProfileMobile.setText(txtProfileMobile.getString() + " *");
        txtProfileUser.setText(txtProfileUser.getString() + " *");
        txtProfileNickname.setText(txtProfileNickname.getString() + " *");
        locale = getResources().getConfiguration().locale.getCountry();

    }

    private void getCountryCodesFromServer() {
        RestAPI.GetDataWrappy(getApplicationContext(), RestAPI.GET_COUNTRY_CODES, new RestAPIListener(this) {
            @Override
            public void OnComplete(String s) {
                try {
                    if (s != null) {
                        Type listType = new TypeToken<ArrayList<WpkCountry>>() {
                        }.getType();
                        wpkCountry = new Gson().fromJson(s, listType);
                        wpkCountry.get(0).getCode();
                        List<String> strings = new ArrayList<>();
                        int j = 0;
                        for (int i = 0; i < wpkCountry.size(); i++) {
                            if (wpkCountry.get(i).getCode().toUpperCase().equalsIgnoreCase(locale.toUpperCase())) {
                                j = i;
                            }
                            strings.add(wpkCountry.get(i).getL10N().get(WpkCountry.country_en_US) + " " + wpkCountry.get(i).getPrefix());
                        }
                        countryAdapter = new ArrayAdapter<String>(UpdateProfileActivity.this, R.layout.update_profile_textview, strings) {
                            @NonNull
                            @Override
                            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                                LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                final View v = vi.inflate(android.R.layout.simple_spinner_item, null);
                                final TextView t = (TextView) v.findViewById(android.R.id.text1);
                                t.setText(wpkCountry.get(position).getPrefix());
                                return v;
                            }
                        };
                        spnProfileCountryCodes.setAdapter(countryAdapter);
                        spnProfileCountryCodesReference.setAdapter(countryAdapter);
                        spnProfileCountryCodes.setSelection(j);
                        spnProfileCountryCodesReference.setSelection(j);
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
                AppFuncs.alert(getApplicationContext(), "Upload File Fail", true);
            }
        }
    };

    @Optional
    @OnClick({R.id.btnProfileComplete, R.id.btnProfileCameraHeader, R.id.btnPhotoCameraAvatar, R.id.lnUpdateProfile})
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
                                AppFuncs.openCamera(UpdateProfileActivity.this, true);
                                break;
                            case 2:
                                AppFuncs.openGallery(UpdateProfileActivity.this, true);
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
                BottomSheetCell sheetCell = new BottomSheetCell(1, R.drawable.ic_choose_camera, getString(R.string.popup_take_photo));
                sheetCells.add(sheetCell);
                sheetCell = new BottomSheetCell(2, R.drawable.ic_choose_gallery, getString(R.string.popup_choose_gallery));
                sheetCells.add(sheetCell);
                if (uriHeader != null) {
                    sheetCell = new BottomSheetCell(3, R.drawable.setting_delete, getString(R.string.popup_delete_photo));
                    sheetCells.add(sheetCell);
                }
                PopupUtils.createBottomSheet(this, sheetCells, new BottomSheetListener() {
                    @Override
                    public void onSelectBottomSheetCell(int index) {
                        switch (index) {
                            case 1:
                                AppFuncs.openCamera(UpdateProfileActivity.this, false);
                                break;
                            case 2:
                                AppFuncs.openGallery(UpdateProfileActivity.this, false);
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
            if (view.getId()==R.id.lnUpdateProfile) {
                AppFuncs.dismissKeyboard(UpdateProfileActivity.this);
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
                    if (type.equals(RestAPI.PHOTO_AVATAR)) {
                        avatarReference = reference;
                    } else {
                        bannerReference = reference;
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private void postDataToServer() {
        AppFuncs.log("postDataToServer");
        WpKMemberDto wpKMemberDto = new WpKMemberDto();
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
        if (!TextUtils.isEmpty(nickname)) {
            wpKMemberDto.setGiven(nickname);
        }
        registrationData.setWpKMemberDto(wpKMemberDto);

        Gson gson = new Gson();
        final JsonObject dataJson = gson.toJsonTree(registrationData).getAsJsonObject();
        AppFuncs.log(dataJson.toString());
        RestAPIListener listener = new RestAPIListener(this) {
            @Override
            public void OnComplete(String s) {
                Bundle bundle = new Bundle();
                bundle.putString("data", dataJson.toString());
                bundle.putString("country" , wpkCountry.get(spnProfileCountryCodes.getSelectedItemPosition()).getPrefix());
                Store.putStringData(getApplicationContext(), Store.USERNAME, user);
                VerifyEmailOrPhoneActivity.start(UpdateProfileActivity.this, bundle, VERIFY_CODE);
                finish();
            }
        };
        RestAPI.PostDataWrappy(getApplicationContext(), dataJson, RestAPI.POST_REGISTER, listener);
    }

    private String validateData() {
        String error = "";
        try {
            user = edUsername.getString();
            email = edEmail.getString();
            phone = edPhone.getString();
            nickname = edProfileNickname.getString();

            if (TextUtils.isEmpty(nickname)) {
                error = getString(R.string.error_empty_nickname);
            }else if (TextUtils.isEmpty(user)) {
                error = getString(R.string.error_empty_username);
            } else if (user.length() < 6) {
                error = getString(R.string.error_invalid_text_length);
            } else if (AppFuncs.detectSpecialCharacters(user)) {
                error = getString(R.string.error_invalid_characters);
            } else if (TextUtils.isEmpty(phone)) {
                error = getString(R.string.error_empty_phone);
            } else if (!TextUtils.isEmpty(Utils.isValidEmail(this, email))) {
                error = Utils.isValidEmail(this, email);
            }
            if (!TextUtils.isEmpty(phone) && phone.startsWith("0")) {
                phone = phone.substring(1, phone.length());
            }
            if (wpkCountry != null) {
                String countryName = wpkCountry.get(spnProfileCountryCodes.getSelectedItemPosition()).getPrefix();
                phone = countryName + phone;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return error;
    }

    //ExistingAccountTask mExistingAccountTask;
    SimpleAlertHandler mHandler;

    Uri uriHeader, uriAvatar;
    boolean isAvatarRequest = false;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        PopupUtils.showCustomDialog(this, getString(R.string.warning), getString(R.string.cancel_and_home), R.string.yes, R.string.cancel, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LauncherActivity.start(UpdateProfileActivity.this);
            }
        }, null);
    }
    @Override
    public void onResultPickerImage(boolean isAvatar, Intent data, boolean isSuccess) {
        super.onResultPickerImage(isAvatar, data, isSuccess);
        if (isAvatar) {
            uriAvatar = UCrop.getOutput(data);
            imgAvatar.setImageURI(uriAvatar);
        } else {
            uriHeader = UCrop.getOutput(data);
            imgHeader.setImageURI(uriHeader);
        }
    }
}
