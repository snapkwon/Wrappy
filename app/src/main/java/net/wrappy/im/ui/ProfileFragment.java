package net.wrappy.im.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.Fragment;
import android.support.v4.content.IntentCompat;
import android.support.v7.widget.AppCompatSpinner;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Response;
import com.yalantis.ucrop.UCrop;

import net.wrappy.im.ImApp;
import net.wrappy.im.MainActivity;
import net.wrappy.im.R;
import net.wrappy.im.helper.AppDelegate;
import net.wrappy.im.helper.AppFuncs;
import net.wrappy.im.helper.RestAPI;
import net.wrappy.im.helper.glide.GlideHelper;
import net.wrappy.im.helper.layout.AppEditTextView;
import net.wrappy.im.helper.layout.AppTextView;
import net.wrappy.im.model.Avatar;
import net.wrappy.im.model.Banner;
import net.wrappy.im.model.BottomSheetCell;
import net.wrappy.im.model.BottomSheetListener;
import net.wrappy.im.model.WpKMemberDto;
import net.wrappy.im.provider.Imps;
import net.wrappy.im.ui.legacy.DatabaseUtils;
import net.wrappy.im.util.Constant;
import net.wrappy.im.util.PopupUtils;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by ben on 18/12/2017.
 */

public class ProfileFragment extends Fragment {

    public static final int AVATAR = 321;
    public static final int BANNER = 322;
    View mainView;
    AppFuncs appFuncs;
    String jid = "";
    WpKMemberDto wpKMemberDto;
    private long mContactId = -1;
    private String mNickname = null;
    private String reference = "";
    private ImApp mApp;
    private boolean isSelf;
    boolean isRequestAvatar = true;
    AppDelegate appDelegate;

    MainActivity mainActivity;

    @BindView(R.id.imgProfileHeader)
    ImageView imgProfileHeader;
    @BindView(R.id.imgPhotoAvatar)
    ImageView imgPhotoAvatar;
    @BindView(R.id.txtProfileUsername)
    AppTextView txtUsername;
    @BindView(R.id.edProfilePhone)
    AppEditTextView edPhone;
    @BindView(R.id.edProfileEmail)
    AppEditTextView edEmail;
    @BindView(R.id.edProfileGender)
    AppEditTextView edGender;
    @BindView(R.id.linearForSeft)
    LinearLayout linearForSeft;
    @BindView(R.id.linearForContact)
    LinearLayout linearForContact;
    @BindView(R.id.btnPhotoCameraAvatar)
    ImageButton btnPhotoCameraAvatar;
    @BindView(R.id.btnProfileSubmit)
    Button btnProfileSubmit;
    @BindView(R.id.btnProfileCameraHeader)
    ImageButton btnProfileCameraHeader;
    @BindView(R.id.spnProfile)
    AppCompatSpinner spnProfile;

    ArrayAdapter adapterGender;

    String email, gender;

    public static ProfileFragment newInstance(long contactId, String nickName, String reference, String jid) {
        Bundle bundle = new Bundle();
        bundle.putLong("contactId", contactId);
        bundle.putString("nickName", nickName);
        bundle.putString("reference", reference);
        bundle.putString("jid", jid);
        ProfileFragment profileFragment = new ProfileFragment();
        profileFragment.setArguments(bundle);
        return profileFragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof AppDelegate) {
            appDelegate = (AppDelegate) activity;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.profile_fragment, null);
        ButterKnife.bind(this, mainView);
        mApp = (ImApp) getActivity().getApplication();
        appFuncs = AppFuncs.getInstance();
        jid = getArguments().getString("jid");
        if (TextUtils.isEmpty(jid)) {
            isSelf = true;
            if (mApp.getDefaultUsername().contains("@")) {
                jid = mApp.getDefaultUsername().split("@")[0];
                linearForContact.setVisibility(View.GONE);
            }
        } else {
            linearForSeft.setVisibility(View.GONE);
        }
        mContactId = getArguments().getLong("contactId");
        mNickname = getArguments().getString("nickName");
        reference = getArguments().getString("nickName");
        preferenceView();
        if (isSelf) {
            mainActivity = (MainActivity) getActivity();
        }

        return mainView;
    }

    private void preferenceView() {
        edGender.setEnabled(false);
        edEmail.setEnabled(false);
        edPhone.setEnabled(false);
        spnProfile.setVisibility(View.INVISIBLE);
        if (isSelf) {
            btnPhotoCameraAvatar.setVisibility(View.VISIBLE);
            btnProfileCameraHeader.setVisibility(View.VISIBLE);
        }
        final String[] arr = {"", ""};
        final String[] arrDetail = getResources().getStringArray(R.array.profile_gender);
        adapterGender = new ArrayAdapter<String>(getActivity(), R.layout.update_profile_textview, arrDetail) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View v = vi.inflate(android.R.layout.simple_spinner_item, null);
                final TextView t = (TextView) v.findViewById(android.R.id.text1);
                t.setText(arr[position]);
                return v;
            }
        };
        spnProfile.setAdapter(adapterGender);
        spnProfile.setSelection(1);
        spnProfile.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                edGender.setText(getResources().getStringArray(R.array.profile_gender)[i]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        getDataMember();
    }

    private void getDataMember() {
        RestAPI.apiGET(getActivity(), RestAPI.getMemberByIdUrl(jid)).setCallback(new FutureCallback<Response<String>>() {
            @Override
            public void onCompleted(Exception e, Response<String> result) {
                if (result != null) {
                    if (RestAPI.checkHttpCode(result.getHeaders().code())) {
                        Gson gson = new Gson();
                        try {
                            AppFuncs.log("load: " + result.getResult());
                            wpKMemberDto = gson.fromJson(result.getResult(), WpKMemberDto.getType());
                            txtUsername.setText(wpKMemberDto.getIdentifier());
                            edEmail.setText(wpKMemberDto.getEmail());
                            edPhone.setText(wpKMemberDto.getMobile());
                            edGender.setText(wpKMemberDto.getGender());
                            if (wpKMemberDto.getAvatar() != null) {
                                GlideHelper.loadBitmap(getActivity(), imgPhotoAvatar, RestAPI.getAvatarUrl(wpKMemberDto.getAvatar().getReference()), false);
                            }
                            if (wpKMemberDto.getBanner() != null && !TextUtils.isEmpty(wpKMemberDto.getBanner().getReference())) {
                                GlideHelper.loadBitmapToImageView(getContext(), imgProfileHeader, RestAPI.getAvatarUrl(wpKMemberDto.getBanner().getReference()));
                            }

                            //RestAPI.loadImageUrl(getApplicationContext(),imgPhotoAvatar,wpKMemberDto.getReference());
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }

                    }
                } else {
                    AppFuncs.alert(getActivity(), getString(R.string.error_get_user_profile), true);
                    if (!isSelf) {
                        getActivity().finish();
                    }
                    //
                }
            }
        });
    }

    private void confirmDeleteAccount(int mAccountId, int mProviderId) {

        //need to delete
        ImApp.deleteAccount(getActivity().getContentResolver(), mAccountId, mProviderId);

        PackageManager packageManager = getActivity().getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(getActivity().getPackageName());
        ComponentName componentName = intent.getComponent();
        Intent mainIntent = IntentCompat.makeRestartActivityTask(componentName);
        getActivity().startActivity(mainIntent);
        System.exit(0);
    }

    @OnClick({R.id.btnProfileSubmit, R.id.btnProfileCameraHeader, R.id.btnPhotoCameraAvatar, R.id.lnProfileSendMessage, R.id.lnProfileChangeQuestion, R.id.lnProfileLogout})
    public void onClick(View view) {
        if (view.getId() == R.id.btnProfileSubmit) {

            String email = edEmail.getText().toString().trim();
            String gender = edGender.getText().toString().trim().toUpperCase();
            if (!TextUtils.isEmpty(email)) if (!AppFuncs.isEmailValid(email)) {
                AppFuncs.alert(getActivity(), getString(R.string.error_invalid_email), true);
                return;
            }
            edEmail.clearFocus();
            edGender.clearFocus();
            edEmail.setEnabled(false);
            wpKMemberDto.setEmail(email);
            wpKMemberDto.setGender(gender);
            updateData();
            btnProfileSubmit.setVisibility(View.GONE);
            appDelegate.onChangeInApp(MainActivity.UPDATE_PROFILE_COMPLETE, "");
        } else if (view.getId() == R.id.btnPhotoCameraAvatar || view.getId() == R.id.btnProfileCameraHeader) {
            ArrayList<BottomSheetCell> sheetCells = new ArrayList<>();
            BottomSheetCell sheetCell = new BottomSheetCell(1, R.drawable.ic_choose_camera, getString(R.string.popup_take_photo));
            sheetCells.add(sheetCell);
            sheetCell = new BottomSheetCell(2, R.drawable.ic_choose_gallery, getString(R.string.popup_choose_gallery));
            sheetCells.add(sheetCell);

            if (view.getId() == R.id.btnPhotoCameraAvatar) {
                isRequestAvatar = true;
                if (wpKMemberDto != null) if (wpKMemberDto.getAvatar() != null) {
                    if (!TextUtils.isEmpty(wpKMemberDto.getAvatar().getReference())) {
                        sheetCell = new BottomSheetCell(3, R.drawable.setting_delete, getString(R.string.popup_delete_photo));
                        sheetCells.add(sheetCell);
                    }
                }
            } else {
                isRequestAvatar = false;
                if (wpKMemberDto != null) if (wpKMemberDto.getBanner() != null) {
                    if (!TextUtils.isEmpty(wpKMemberDto.getBanner().getReference())) {
                        sheetCell = new BottomSheetCell(3, R.drawable.setting_delete, getString(R.string.popup_delete_photo));
                        sheetCells.add(sheetCell);
                    }
                }
            }

            PopupUtils.createBottomSheet(getActivity(), sheetCells, new BottomSheetListener() {
                @Override
                public void onSelectBottomSheetCell(int index) {
                    switch (index) {
                        case 1:
                            AppFuncs.openCamera(getActivity(), isRequestAvatar ? AVATAR : BANNER);
                            break;
                        case 2:
                            AppFuncs.openGallery(getActivity(), isRequestAvatar ? AVATAR : BANNER);
                            break;
                        case 3:
                            if (isRequestAvatar) {
                                imgPhotoAvatar.setImageResource(R.drawable.avatar);
                                if (wpKMemberDto.getAvatar() != null) {
                                    if (!TextUtils.isEmpty(wpKMemberDto.getAvatar().getReference())) {
                                        RestAPI.apiDELETE(getActivity(), RestAPI.DELETE_AVATAR, new JsonObject()).setCallback(new FutureCallback<Response<String>>() {
                                            @Override
                                            public void onCompleted(Exception e, Response<String> result) {
                                                if (result != null) {
                                                    if (RestAPI.checkHttpCode(result.getHeaders().code())) {
                                                        wpKMemberDto.setAvatar(null);
                                                        AppFuncs.alert(getActivity(), getString(R.string.message_remove_avatar_success), true);
                                                    }
                                                }
                                            }
                                        });
                                    }
                                }
                            } else {
                                imgProfileHeader.setImageResource(android.R.color.transparent);
                                if (wpKMemberDto.getBanner() != null) {
                                    if (!TextUtils.isEmpty(wpKMemberDto.getBanner().getReference())) {
                                        RestAPI.apiDELETE(getActivity(), RestAPI.DELETE_AVATAR, new JsonObject()).setCallback(new FutureCallback<Response<String>>() {
                                            @Override
                                            public void onCompleted(Exception e, Response<String> result) {
                                                if (result != null) {
                                                    if (RestAPI.checkHttpCode(result.getHeaders().code())) {
                                                        wpKMemberDto.setBanner(null);
                                                        AppFuncs.alert(getActivity(), getString(R.string.message_remove_banner_success), true);
                                                    }
                                                }
                                            }
                                        });
                                    }
                                }
                            }


                            break;
                        default:
                    }
                }
            }).show();
        } else if (view.getId() == R.id.lnProfileSendMessage) {
            startChat();
        } else if (view.getId() == R.id.lnProfileChangeQuestion) {
            Intent intent = new Intent(getActivity(), SecurityQuestionActivity.class);
            startActivity(intent);
        } else if (view.getId() == R.id.lnProfileLogout) {
            ArrayList<BottomSheetCell> sheetCells = new ArrayList<>();
            BottomSheetCell sheetCell = new BottomSheetCell(1, R.drawable.ic_menutab_logout, getString(R.string.logout_device));
            sheetCells.add(sheetCell);
            sheetCell = new BottomSheetCell(2, R.drawable.ic_logout_all, getString(R.string.logout_all_devices));
            sheetCells.add(sheetCell);
            BottomSheetDialog bottomSheetDialog = PopupUtils.createBottomSheet(getActivity(), sheetCells, new BottomSheetListener() {
                @Override
                public void onSelectBottomSheetCell(int index) {
                    if (index == 1) {
                        confirmDeleteAccount(mainActivity.getDefaultAcountid(), mainActivity.getDefaultProviderid());
                        //AppFuncs.alert(getActivity(), "Logout this device", true);
                    } else if (index == 2) {
                        AppFuncs.alert(getActivity(), getString(R.string.logout_all_devices), true);
                    }
                }
            });
            bottomSheetDialog.show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == AVATAR || requestCode == BANNER) {
                if (data != null) {
                    AppFuncs.cropImage(getActivity(), data, true);
                }
            } else if (requestCode == UCrop.REQUEST_CROP) {
                final Uri resultUri = UCrop.getOutput(data);
                appFuncs.showProgressWaiting(getActivity());
                RestAPI.uploadFile(getActivity(), new File(resultUri.getPath()), RestAPI.PHOTO_AVATAR).setCallback(new FutureCallback<Response<String>>() {
                    @Override
                    public void onCompleted(Exception e, Response<String> result) {
                        appFuncs.dismissProgressWaiting();
                        try {
                            String reference = RestAPI.getPhotoReference(result.getResult());
                            if (!TextUtils.isEmpty(reference)) {
                                if (isRequestAvatar) {
                                    GlideHelper.loadBitmap(getActivity(), imgPhotoAvatar, resultUri.toString(), true);
                                    Avatar avatar = new Avatar(reference);
                                    wpKMemberDto.setAvatar(avatar);
                                } else {
                                    GlideHelper.loadBitmap(getActivity(), imgProfileHeader, resultUri.toString(), false);
                                    Banner banner = new Banner(reference);
                                    wpKMemberDto.setBanner(banner);
                                }
                                updateData();
                            } else {
                                AppFuncs.alert(getActivity(), "Upload fail", false);
                            }

                        } catch (Exception ex) {
                            AppFuncs.alert(getActivity(), "Upload fail", false);
                            ex.printStackTrace();
                        }
                    }
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void updateData() {
        JsonObject jsonObject = AppFuncs.convertClassToJsonObject(wpKMemberDto);
        AppFuncs.log("update: " + jsonObject.toString());
        RestAPI.apiPUT(getActivity(), RestAPI.GET_MEMBER_INFO, jsonObject).setCallback(new FutureCallback<Response<String>>() {
            @Override
            public void onCompleted(Exception e, Response<String> result) {
                if (result != null) {
                    AppFuncs.log(result.getResult());
                    if (RestAPI.checkHttpCode(result.getHeaders().code())) {
                        AppFuncs.alert(getActivity(), getString(R.string.update_profile_success), true);
                        if (wpKMemberDto != null && wpKMemberDto.getAvatar() != null && !TextUtils.isEmpty(wpKMemberDto.getAvatar().getReference())) {
                            String avatarReference = wpKMemberDto.getAvatar().getReference();
                            String bannerReference = wpKMemberDto.getBanner() != null ? wpKMemberDto.getBanner().getReference() : "";
                            String hash = DatabaseUtils.generateHashFromAvatar(avatarReference);
                            String address = wpKMemberDto.getXMPPAuthDto().getAccount() + Constant.EMAIL_DOMAIN;
                            DatabaseUtils.insertAvatarBlob(ImApp.sImApp.getContentResolver(), Imps.Avatars.CONTENT_URI, ImApp.sImApp.getDefaultProviderId(), ImApp.sImApp.getDefaultAccountId(), avatarReference, bannerReference, hash, address);
                            ImApp.broadcastIdentity(null);
                            spnProfile.setVisibility(View.INVISIBLE);
                            if (appDelegate != null) {
                                appDelegate.onChangeInApp(MainActivity.UPDATE_PROFILE_COMPLETE, "");
                            }
                        }
                    } else {
                        AppFuncs.alert(getActivity(), getString(R.string.update_profile_fail), true);
                    }
                }
            }
        });
    }

    public void startChat() {
        Intent intent = ConversationDetailActivity.getStartIntent(getActivity(), mContactId, mNickname, reference);
        startActivity(intent);
        getActivity().finish();
    }

    public void onDataChange() {
        edEmail.setEnabled(true);
        edEmail.setFocusable(true);
        btnProfileSubmit.setVisibility(View.VISIBLE);
        spnProfile.setVisibility(View.VISIBLE);
    }
}
