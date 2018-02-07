package net.wrappy.im.ui;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatSpinner;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Response;
import com.yalantis.ucrop.UCrop;

import net.wrappy.im.ImApp;
import net.wrappy.im.R;
import net.wrappy.im.comon.BaseFragmentV4;
import net.wrappy.im.helper.AppFuncs;
import net.wrappy.im.helper.NotificationCenter;
import net.wrappy.im.helper.RestAPI;
import net.wrappy.im.helper.RestAPIListener;
import net.wrappy.im.helper.glide.GlideHelper;
import net.wrappy.im.helper.layout.AppEditTextView;
import net.wrappy.im.helper.layout.AppTextView;
import net.wrappy.im.model.Avatar;
import net.wrappy.im.model.Banner;
import net.wrappy.im.model.BottomSheetCell;
import net.wrappy.im.model.BottomSheetListener;
import net.wrappy.im.model.MemberAccount;
import net.wrappy.im.model.WpKMemberDto;
import net.wrappy.im.provider.Imps;
import net.wrappy.im.util.Constant;
import net.wrappy.im.util.PopupUtils;
import net.wrappy.im.util.Utils;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by ben on 18/12/2017.
 */

public class ProfileFragment extends BaseFragmentV4 {

    private final int MY_SELF = 1;
    private final int FRIEND = 2;
    private final int STRANGER = 3;

    public static final int AVATAR = 321;
    public static final int BANNER = AVATAR + 1;
    public static final int CROP_AVATAR = BANNER + 1;
    public static final int CROP_BANNER = CROP_AVATAR + 1;
    View mainView;
    String jid = "";
    WpKMemberDto wpKMemberDto;
    private long mContactId = -1;
    private String mNickname = null;
    private String reference = "";
    private ImApp mApp;
    private boolean isSelf;
    boolean isRequestAvatar = true;

    @BindView(R.id.imgProfileHeader)
    ImageView imgProfileHeader;
    @BindView(R.id.imgPhotoAvatar)
    ImageView imgPhotoAvatar;
    @BindView(R.id.txtProfileUsername)
    AppTextView txtUsername;
    @BindView(R.id.txtProfileNickname)
    AppTextView txtProfileNickname;
    @BindView(R.id.edProfileFullName)
    AppEditTextView edFullName;
    @BindView(R.id.edProfilePhone)
    AppEditTextView edPhone;
    @BindView(R.id.edProfileEmail)
    AppEditTextView edEmail;
    @BindView(R.id.edProfileGender)
    AppTextView edGender;
    @BindView(R.id.lnForSeft)
    LinearLayout lnForSeft;
    @BindView(R.id.lnForFriend)
    LinearLayout lnForFriend;
    @BindView(R.id.lnForStranger)
    LinearLayout lnForStranger;
    @BindView(R.id.btnPhotoCameraAvatar)
    ImageButton btnPhotoCameraAvatar;
    @BindView(R.id.btnProfileSubmit)
    Button btnProfileSubmit;
    @BindView(R.id.btnProfileCameraHeader)
    ImageButton btnProfileCameraHeader;
    @BindView(R.id.spnProfile)
    AppCompatSpinner spnProfile;
    @BindView(R.id.lnProfilePhone)
    LinearLayout lnProfilePhone;
    @BindView(R.id.lnProfileEmail)
    LinearLayout lnProfileEmail;
    @BindView(R.id.lnProfileGender)
    LinearLayout lnProfileGender;
    @BindView(R.id.lnProfileUsername)
    LinearLayout lnProfileUsername;
    @BindView(R.id.txtClientName)
    AppTextView txtClientName;

    ArrayAdapter adapterGender;

    String emailTemp = "";
    String genderTemp = "";
    String nameTemp = "";
    private String[] arrDetail;


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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        mainView = inflater.inflate(R.layout.profile_fragment, null);
        ButterKnife.bind(this, mainView);
        mApp = (ImApp) getActivity().getApplication();
        jid = getArguments().getString("jid");
        arrDetail = getResources().getStringArray(R.array.profile_gender_display);

        edFullName.setEnabled(false);
        if (TextUtils.isEmpty(jid)) {
            isSelf = true;
            if (mApp.getDefaultUsername().contains("@")) {
                jid = mApp.getDefaultUsername().split("@")[0];
            }
            isSelfOrFriendsOrStranger(MY_SELF);
        } else {
            boolean isExists = Imps.Contacts.checkExists(getActivity().getContentResolver(), jid + "@" + Constant.DOMAIN);
            if (isExists) {
                isSelfOrFriendsOrStranger(FRIEND);
            } else {
                isSelfOrFriendsOrStranger(STRANGER);
            }
        }

        getFriendInforFromIntent();

        preferenceView();
        return mainView;
    }

    private void isSelfOrFriendsOrStranger(int who) {
        switch (who) {
            case MY_SELF:
                updateDataFromLocal();
                loadAvatarFromLocal(jid);
                lnForSeft.setVisibility(View.VISIBLE);
                break;
            case FRIEND:
                loadAvatarFromLocal(jid);
                lnProfileEmail.setVisibility(View.GONE);
                lnProfileGender.setVisibility(View.GONE);
                lnProfilePhone.setVisibility(View.GONE);
                lnProfileUsername.setVisibility(View.GONE);
                lnForFriend.setVisibility(View.VISIBLE);
                break;
            case STRANGER:
                lnProfileEmail.setVisibility(View.GONE);
                lnProfileGender.setVisibility(View.GONE);
                lnProfilePhone.setVisibility(View.GONE);
                lnProfileUsername.setVisibility(View.GONE);
                lnForStranger.setVisibility(View.VISIBLE);
                break;
        }
    }

    public final String[] CHAT_PROJECTION = {
            Imps.AccountColumns.ACCOUNT_NAME,
            Imps.AccountColumns.ACCOUNT_EMAIL,
            Imps.AccountColumns.ACCOUNT_PHONE,
            Imps.AccountColumns.ACCOUNT_GENDER,
            Imps.AccountColumns.NAME
    };

    private void updateDataFromLocal() {
        Uri accountUri = ContentUris.withAppendedId(Imps.Account.CONTENT_URI, mApp.getDefaultAccountId());
        Cursor newCursor = getActivity().getContentResolver().query(accountUri, CHAT_PROJECTION, null, null, null);
        if (newCursor.moveToFirst()) {
            String c_username = newCursor.getString(newCursor.getColumnIndex(Imps.AccountColumns.NAME));
            String c_name = newCursor.getString(newCursor.getColumnIndex(Imps.AccountColumns.ACCOUNT_NAME));
            String c_email = newCursor.getString(newCursor.getColumnIndex(Imps.AccountColumns.ACCOUNT_EMAIL));
            String c_phone = newCursor.getString(newCursor.getColumnIndex(Imps.AccountColumns.ACCOUNT_PHONE));
            String c_gender = newCursor.getString(newCursor.getColumnIndex(Imps.AccountColumns.ACCOUNT_GENDER));
            wpKMemberDto = new WpKMemberDto();
            if (!Utils.setTextForView(txtUsername, c_username)) {
                wpKMemberDto.setIdentifier(c_username);
                loadAvatarFromLocal(c_username);
            }
            if (!TextUtils.isEmpty(c_gender)) {
                genderTemp = Utils.uppercaseFirstChar(c_gender);
                setTextGender(genderTemp);
                wpKMemberDto.setGender(genderTemp.toUpperCase());
            }
            if (Utils.setTextForView(edPhone, c_phone)) {
                wpKMemberDto.setMobile(c_phone);
            }
            if (Utils.setTextForView(edEmail, c_email)) {
                wpKMemberDto.setEmail(c_email);
                emailTemp = c_email;
            }
            if (Utils.setTextForView(edFullName, c_name)) {
                wpKMemberDto.setGiven(c_name);
                nameTemp = c_name;
            }
        }
    }

    private void setTextGender(String text) {
        if (text.contains("F"))
            edGender.setText(arrDetail[0]);
        else
            edGender.setText(arrDetail[1]);
    }

    private void loadAvatarFromLocal(String mJid) {
        if (mJid.contains("@") && mJid.contains(Constant.DOMAIN)) {
            mJid = mJid.split("@")[0];
        }
        String reference = Imps.Avatars.getAvatar(getActivity().getContentResolver(), mJid + "@" + Constant.DOMAIN);
        if (!TextUtils.isEmpty(reference)) {
            GlideHelper.loadBitmap(getActivity(), imgPhotoAvatar, RestAPI.getAvatarUrl(reference), false);
        }
    }

    private void getFriendInforFromIntent() {
        mContactId = getArguments().getLong("contactId");
        mNickname = getArguments().getString("nickName");
        reference = getArguments().getString("reference");
    }

    private void preferenceView() {
        if (isSelf) {
            btnPhotoCameraAvatar.setVisibility(View.VISIBLE);
            btnProfileCameraHeader.setVisibility(View.VISIBLE);
        }

        adapterGender = new ArrayAdapter<String>(getActivity(), R.layout.update_profile_textview, arrDetail) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final AppTextView v = (AppTextView) vi.inflate(R.layout.update_profile_textview, null);
                v.setText(arrDetail[position]);
                return v;
            }
        };
        spnProfile.setAdapter(adapterGender);
        spnProfile.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Utils.setTextForView(edGender, Utils.uppercaseFirstChar(getResources().getStringArray(R.array.profile_gender_display)[i]));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        getDataMember();
    }

    private void getDataMember() {
        String url = RestAPI.getMemberByIdUrl(jid);
        RestAPIListener listener = new RestAPIListener(getActivity()) {
            @Override
            public void OnComplete(String s) {
                Gson gson = new Gson();
                try {
                    AppFuncs.log("load: " + s);
                    if (isSelf) {
                        MemberAccount account = gson.fromJson(s, MemberAccount.class);
                        wpKMemberDto = account.getWpKMemberDto();
                    } else {
                        wpKMemberDto = gson.fromJson(s, WpKMemberDto.getType());
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.changeProfileName, wpKMemberDto.getGiven());
                    }
                    emailTemp = wpKMemberDto.getEmail();
                    genderTemp = wpKMemberDto.getGender();

                    Utils.setTextForView(edEmail, emailTemp);
                    Utils.setTextForView(txtUsername, wpKMemberDto.getIdentifier());
                    Utils.setTextForView(edPhone, wpKMemberDto.getMobile());
                    if (Utils.setTextForView(edFullName, wpKMemberDto.getGiven())) {
                        nameTemp = wpKMemberDto.getGiven();
                    }

                    if (!TextUtils.isEmpty(genderTemp)) {
                        String upperString = Utils.uppercaseFirstChar(wpKMemberDto.getGender());
                        if (upperString.startsWith("F")) {
                            spnProfile.setSelection(0);
                        } else {
                            spnProfile.setSelection(1);
                        }
                    }

                    if (wpKMemberDto.getAvatar() != null) {
                        GlideHelper.loadBitmap(getActivity(), imgPhotoAvatar, RestAPI.getAvatarUrl(wpKMemberDto.getAvatar().getReference()), false);
                    }
                    if (wpKMemberDto.getBanner() != null && !TextUtils.isEmpty(wpKMemberDto.getBanner().getReference())) {
                        GlideHelper.loadBitmap(getActivity(), imgProfileHeader, RestAPI.getAvatarUrl(wpKMemberDto.getBanner().getReference()), false);
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        };
        listener.setOnListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isSelf) {
                    getActivity().finish();
                }
            }
        });
        if (isSelf) {
            RestAPI.GetDataWrappy(getActivity(), RestAPI.GET_MEMBER_INFO, listener);
        } else {
            RestAPI.GetDataWrappy(getActivity(), url, listener);
        }

    }

    private void logout() {
        ImApp.sImApp.logout();
        getActivity().finishAffinity();
    }

    private String getGender() {
        String text = edGender.getString().toUpperCase();
        if (!TextUtils.isEmpty(text) && text.equalsIgnoreCase(arrDetail[0]))
            return getResources().getStringArray(R.array.profile_gender)[0];
        else
            return getResources().getStringArray(R.array.profile_gender)[1];
    }

    @OnClick({R.id.btnProfileSubmit, R.id.btnProfileCameraHeader, R.id.btnPhotoCameraAvatar, R.id.lnProfileSendMessage, R.id.lnProfileChangeQuestion, R.id.lnProfileInvite, R.id.lnProfileLogout, R.id.lnProfileFragment, R.id.lnProfileBlockUser, R.id.lnProfileShareContact, R.id.lnProfileAddFriend})
    public void onClick(View view) {
        if (view.getId() == R.id.btnProfileSubmit) {
            int[] stringRes = new int[]{R.string.error_empty_nickname, R.string.error_invalid_email};
            AppEditTextView[] views = new AppEditTextView[]{edFullName, edEmail};
            if (!Utils.checkValidateAppEditTextViews(getActivity(), views, stringRes)) {
                return;
            }
            onDataEditChange(false);
            wpKMemberDto.setEmail(edEmail.getString());
            wpKMemberDto.setGender(getGender());
            wpKMemberDto.setGiven(edFullName.getString());
            updateData();
            NotificationCenter.getInstance().postNotificationName(NotificationCenter.showEditIconOnMainActivity, "");
        } else if (view.getId() == R.id.btnPhotoCameraAvatar || view.getId() == R.id.btnProfileCameraHeader) {
            ArrayList<BottomSheetCell> sheetCells = new ArrayList<>();
            BottomSheetCell sheetCell = new BottomSheetCell(1, R.drawable.ic_choose_camera, getString(R.string.popup_take_photo));
            sheetCells.add(sheetCell);
            sheetCell = new BottomSheetCell(2, R.drawable.ic_choose_gallery, getString(R.string.popup_choose_gallery));
            sheetCells.add(sheetCell);

            if (view.getId() == R.id.btnPhotoCameraAvatar) {
                isRequestAvatar = true;
                if (wpKMemberDto != null && wpKMemberDto.getAvatar() != null && !TextUtils.isEmpty(wpKMemberDto.getAvatar().getReference())) {
                    sheetCell = new BottomSheetCell(3, R.drawable.setting_delete, getString(R.string.popup_delete_photo));
                    sheetCells.add(sheetCell);
                }
            } else {
                isRequestAvatar = false;
                if (wpKMemberDto != null && wpKMemberDto.getBanner() != null && !TextUtils.isEmpty(wpKMemberDto.getBanner().getReference())) {
                    sheetCell = new BottomSheetCell(3, R.drawable.setting_delete, getString(R.string.popup_delete_photo));
                    sheetCells.add(sheetCell);
                }
            }

            PopupUtils.createBottomSheet(getActivity(), sheetCells, new BottomSheetListener() {
                @Override
                public void onSelectBottomSheetCell(int index) {
                    switch (index) {
                        case 1:
                            AppFuncs.openCamera(getActivity(), isRequestAvatar);
                            break;
                        case 2:
                            AppFuncs.openGallery(getActivity(), isRequestAvatar);
                            break;
                        case 3:
                            if (isRequestAvatar) {
                                imgPhotoAvatar.setImageResource(R.drawable.avatar);
                                if (wpKMemberDto.getAvatar() != null) {
                                    if (!TextUtils.isEmpty(wpKMemberDto.getAvatar().getReference())) {
                                        RestAPI.DeleteDataWrappy(getActivity(), new JsonObject(), RestAPI.DELETE_AVATAR, new RestAPIListener(getActivity()) {
                                            @Override
                                            public void OnComplete(String s) {
                                                wpKMemberDto.setAvatar(null);
                                                AppFuncs.alert(getActivity(), getString(R.string.message_remove_avatar_success), true);
                                            }
                                        });
                                    }
                                }
                            } else {
                                imgProfileHeader.setImageResource(android.R.color.transparent);
                                if (wpKMemberDto.getBanner() != null) {
                                    if (!TextUtils.isEmpty(wpKMemberDto.getBanner().getReference())) {
                                        RestAPI.DeleteDataWrappy(getActivity(), new JsonObject(), RestAPI.DELETE_BANNER, new RestAPIListener(getActivity()) {
                                            @Override
                                            public void OnComplete(String s) {
                                                wpKMemberDto.setBanner(null);
                                                AppFuncs.alert(getActivity(), getString(R.string.message_remove_banner_success), true);
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
        } else if (view.getId() == R.id.lnProfileSendMessage || view.getId() == R.id.lnProfileAddFriend) {
            startChat();
        } else if (view.getId() == R.id.lnProfileChangeQuestion) {
            Intent intent = new Intent(getActivity(), SecurityQuestionActivity.class);
            startActivity(intent);
        } else if (view.getId() == R.id.lnProfileLogout) {
            PopupUtils.showCustomDialog(getActivity(), getString(R.string.warning), getString(R.string.logout_noti), R.string.ok, R.string.cancel, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    logout();
                }
            }, null);
        } else if (view.getId() == R.id.lnProfileInvite) {
            AppFuncs.sendRequestInviteFriend(getActivity());
        } else if (view.getId() == R.id.lnProfileFragment) {
            AppFuncs.dismissKeyboard(getActivity());
        } else if (view.getId() == R.id.lnProfileBlockUser) {
            AppFuncs.alert(getActivity(), getString(R.string.comming_soon), false);
        } else if (view.getId() == R.id.lnProfileShareContact) {
            AppFuncs.alert(getActivity(), getString(R.string.comming_soon), false);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            isRequestAvatar = false;
            switch (requestCode) {
                case AVATAR:
                    isRequestAvatar = true;
                case BANNER:
                    AppFuncs.cropImage(getActivity(), data, isRequestAvatar, isRequestAvatar ? CROP_AVATAR : CROP_BANNER);
                    break;

                case CROP_AVATAR:
                    isRequestAvatar = true;
                case CROP_BANNER:
                    final Uri resultUri = UCrop.getOutput(data);
                    AppFuncs.showProgressWaiting(getActivity());
                    RestAPI.uploadFile(getActivity(), new File(resultUri.getPath()), RestAPI.PHOTO_AVATAR).setCallback(new FutureCallback<Response<String>>() {
                        @Override
                        public void onCompleted(Exception e, Response<String> result) {
                            AppFuncs.dismissProgressWaiting();
                            try {
                                String reference = RestAPI.getPhotoReference(result.getResult());
                                if (!TextUtils.isEmpty(reference)) {
                                    if (isRequestAvatar) {
                                        //GlideHelper.loadBitmap(getActivity(), imgPhotoAvatar, resultUri.toString(), true);
                                        Avatar avatar = new Avatar(reference);
                                        wpKMemberDto.setAvatar(avatar);
                                    } else {
                                        //GlideHelper.loadBitmap(getActivity(), imgProfileHeader, resultUri.toString(), false);
                                        Banner banner = new Banner(reference);
                                        wpKMemberDto.setBanner(banner);
                                    }
                                    updateData();
                                } else {
                                    AppFuncs.alert(getActivity(), getString(R.string.upload_fail), false);
                                }

                            } catch (Exception ex) {
                                AppFuncs.alert(getActivity(), getString(R.string.upload_fail), false);
                                ex.printStackTrace();
                            }
                        }
                    });
                    break;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void receiverReferenceAvatarOrBanner(boolean isAvatar, String reference) {
        if (!TextUtils.isEmpty(reference)) {
            if (isAvatar) {
                Avatar avatar = new Avatar(reference);
                wpKMemberDto.setAvatar(avatar);
            } else {
                Banner banner = new Banner(reference);
                wpKMemberDto.setBanner(banner);
            }
            updateData();
        } else {
            AppFuncs.alert(getActivity(), getString(R.string.upload_fail), false);
        }
    }

    private void updateData() {
        JsonObject jsonObject = AppFuncs.convertClassToJsonObject(wpKMemberDto);
        AppFuncs.log("update: " + jsonObject.toString());
        RestAPI.PutDataWrappy(getActivity(), jsonObject, RestAPI.GET_MEMBER_INFO, new RestAPIListener(getActivity()) {
            @Override
            public void OnComplete(String s) {
                updateTemp();
                PopupUtils.showOKDialog(getActivity(), getString(R.string.info), getString(R.string.update_profile_success));
                Imps.Account.updateAccountFromDataServer(getActivity().getContentResolver(), wpKMemberDto);
                NotificationCenter.getInstance().postNotificationName(NotificationCenter.showEditIconOnMainActivity, "");
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (wpKMemberDto.getAvatar() != null) {
                            GlideHelper.loadBitmap(getActivity(), imgPhotoAvatar, RestAPI.getAvatarUrl(wpKMemberDto.getAvatar().getReference()), true);
                        }
                        if (wpKMemberDto.getBanner() != null) {
                            GlideHelper.loadBitmap(getActivity(), imgProfileHeader, RestAPI.getAvatarUrl(wpKMemberDto.getBanner().getReference()), false);
                        }
                    }
                });
            }

            @Override
            protected void onError(int errorCode) {
                super.onError(errorCode);
                returnDataFromTemp();
            }
        });

    }

    public void startChat() {
        Intent intent = ConversationDetailActivity.getStartIntent(getActivity(), mContactId, jid, reference);
        startActivity(intent);
        getActivity().finish();
    }

    public void onDataEditChange(boolean isEditting) {
        if (isEditting) {
            edEmail.setEnabled(true);
            edFullName.setEnabled(true);
            edFullName.requestFocus();
            edFullName.setFocusableInTouchMode(true);
            edPhone.setTextColor(getResources().getColor(R.color.line));
            txtUsername.setTextColor(getResources().getColor(R.color.line));
            btnProfileSubmit.setVisibility(View.VISIBLE);
            spnProfile.setVisibility(View.VISIBLE);
            edGender.setVisibility(View.INVISIBLE);
        } else {
            btnProfileSubmit.setVisibility(View.GONE);
            edEmail.clearFocus();
            edGender.clearFocus();
            edEmail.setEnabled(false);
            edFullName.clearFocus();
            edFullName.setEnabled(false);
            edPhone.setTextColor(Color.BLACK);
            txtUsername.setTextColor(Color.BLACK);
            btnProfileSubmit.setVisibility(View.GONE);
            spnProfile.setVisibility(View.GONE);
            edGender.setVisibility(View.VISIBLE);
        }
    }

    private void updateTemp() {
        emailTemp = wpKMemberDto.getEmail();
        genderTemp = wpKMemberDto.getGender();
        nameTemp = wpKMemberDto.getGiven();
    }

    private void returnDataFromTemp() {
        try {
            edEmail.setText(emailTemp);
            setTextGender(Utils.uppercaseFirstChar(genderTemp));
            edFullName.setText(nameTemp);
            wpKMemberDto.setEmail(emailTemp);
            wpKMemberDto.setGender(genderTemp);
            wpKMemberDto.setGiven(nameTemp);
            spnProfile.setVisibility(View.INVISIBLE);
            edGender.setVisibility(View.VISIBLE);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void reloadFragment() {

    }
}
