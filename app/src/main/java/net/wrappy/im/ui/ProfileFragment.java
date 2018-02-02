package net.wrappy.im.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
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
import net.wrappy.im.MainActivity;
import net.wrappy.im.R;
import net.wrappy.im.comon.BaseFragmentV4;
import net.wrappy.im.helper.AppDelegate;
import net.wrappy.im.helper.AppFuncs;
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
import net.wrappy.im.provider.Store;
import net.wrappy.im.ui.legacy.DatabaseUtils;
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

    public static final int AVATAR = 321;
    public static final int BANNER = AVATAR + 1;
    public static final int CROP_AVATAR = BANNER + 1;
    public static final int CROP_BANNER = CROP_AVATAR + 1;
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
    @BindView(R.id.edProfileFullName)
    AppEditTextView edFullName;
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
    @BindView(R.id.lnProfilePhone) LinearLayout lnProfilePhone;
    @BindView(R.id.lnProfileEmail) LinearLayout lnProfileEmail;
    @BindView(R.id.lnProfileGender) LinearLayout lnProfileGender;
    @BindView(R.id.lnProfileUsername) LinearLayout lnProfileUsername;
    @BindView(R.id.txtClientName) AppTextView txtClientName;

    ArrayAdapter adapterGender;
    private MyLoaderCallbacks mLoaderCallbacks;
    private LoaderManager mLoaderManager;

    String emailTemp = "";
    String genderTemp = "";
    String nameTemp = "";

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

    class MyLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            StringBuilder buf = new StringBuilder();

            buf.append('(');
            buf.append(Imps.AccountColumns.ACCOUNT_NAME);
            buf.append(" = ");
            android.database.DatabaseUtils.appendValueToSql(buf,  jid);
            buf.append(')');
            Uri.Builder builder = Imps.Account.CONTENT_URI.buildUpon();
            Uri mUri = builder.build();
            CursorLoader loader = new CursorLoader(getActivity(), mUri, CHAT_PROJECTION,
                    buf == null ? null : buf.toString(), null, null);

            return loader;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor newCursor) {
            if (newCursor == null)
                return; // the app was quit or something while this was working

            if (newCursor.moveToFirst()){
                String c_username = newCursor.getString(newCursor.getColumnIndex(Imps.AccountColumns.NAME));
                String c_name = newCursor.getString(newCursor.getColumnIndex(Imps.AccountColumns.ACCOUNT_NAME));
                String c_email = newCursor.getString(newCursor.getColumnIndex(Imps.AccountColumns.ACCOUNT_EMAIL));
                String c_phone = newCursor.getString(newCursor.getColumnIndex(Imps.AccountColumns.ACCOUNT_PHONE));
                String c_gender = newCursor.getString(newCursor.getColumnIndex(Imps.AccountColumns.ACCOUNT_GENDER));
                if (!TextUtils.isEmpty(c_username)) {
                    txtUsername.setText(c_username);
                    String reference = Imps.Avatars.getAvatar(getActivity().getContentResolver(),c_username+"@"+Constant.DOMAIN);
                    if (!TextUtils.isEmpty(reference)) {
                        GlideHelper.loadBitmap(getActivity(), imgPhotoAvatar, RestAPI.getAvatarUrl(reference), false);
                    }
                }
                if (!TextUtils.isEmpty(c_gender)) {
                    String upperString = c_gender.substring(0, 1).toUpperCase() + c_gender.substring(1).toLowerCase();
                    edGender.setText(upperString);
                    genderTemp = c_gender;
                }
                if (!TextUtils.isEmpty(c_phone)) {
                    edPhone.setText(c_phone);
                }
                if (!TextUtils.isEmpty(c_email)) {
                    edEmail.setText(c_email);
                    emailTemp = c_email;
                }
                if (!TextUtils.isEmpty(c_name)) {
                    edFullName.setText(c_name);
                    nameTemp = c_name;
                }
            }


        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
        }

        public final String[] CHAT_PROJECTION = {
                Imps.AccountColumns.ACCOUNT_NAME,
                Imps.AccountColumns.ACCOUNT_EMAIL,
                Imps.AccountColumns.ACCOUNT_PHONE,
                Imps.AccountColumns.ACCOUNT_GENDER,
                Imps.AccountColumns.NAME
        };


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
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
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
            mLoaderCallbacks = new MyLoaderCallbacks();
            mLoaderManager = getLoaderManager();
            mLoaderManager.initLoader(1001, null, mLoaderCallbacks);
        } else {
            lnProfileEmail.setVisibility(View.GONE);
            lnProfileGender.setVisibility(View.GONE);
            lnProfilePhone.setVisibility(View.GONE);
            linearForSeft.setVisibility(View.GONE);
            lnProfileUsername.setVisibility(View.VISIBLE);
            txtClientName.setText(jid);
            String reference = Imps.Avatars.getAvatar(getActivity().getContentResolver(),jid+"@"+Constant.DOMAIN);
            if (!TextUtils.isEmpty(reference)) {
                GlideHelper.loadBitmap(getActivity(), imgPhotoAvatar, RestAPI.getAvatarUrl(reference), false);
            }

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
            txtUsername.setText(Store.getStringData(getActivity(), Store.USERNAME));
        }
        final String[] arr = {"", ""};
        final String[] arrDetail = getResources().getStringArray(R.array.profile_gender);
        adapterGender = new ArrayAdapter<String>(getActivity(), R.layout.update_profile_textview, arrDetail) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final AppTextView v = (AppTextView) vi.inflate(R.layout.update_profile_textview, null);
                v.setText(arr[position]);
                return v;
            }
        };
        spnProfile.setAdapter(adapterGender);
        spnProfile.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String upperString = getResources().getStringArray(R.array.profile_gender)[i].substring(0, 1).toUpperCase() + getResources().getStringArray(R.array.profile_gender)[i].substring(1).toLowerCase();
                edGender.setText(upperString);
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
                        if (!TextUtils.isEmpty(wpKMemberDto.getGiven())) {
                            txtClientName.setText(wpKMemberDto.getGiven());
                        }
                    }
                    emailTemp = wpKMemberDto.getEmail();
                    genderTemp = wpKMemberDto.getGender();

                    if (!TextUtils.isEmpty(emailTemp)) {
                        edEmail.setText(emailTemp);
                    }
                    if (!TextUtils.isEmpty(wpKMemberDto.getGiven())) {
                        nameTemp = wpKMemberDto.getGiven();
                        edFullName.setText(nameTemp);
                    }

                    txtUsername.setText(wpKMemberDto.getIdentifier());
                    edPhone.setText(wpKMemberDto.getMobile());

                    if (!TextUtils.isEmpty(genderTemp)) {
                        String upperString = wpKMemberDto.getGender().substring(0, 1).toUpperCase() + wpKMemberDto.getGender().substring(1).toLowerCase();
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
        if (!isSelf) {
            RestAPI.GetDataWrappy(getActivity(), url, listener);
        } else {
            RestAPI.GetDataWrappy(getActivity(), RestAPI.GET_MEMBER_INFO, listener);
        }

    }

    private void logout() {
        ImApp.sImApp.logout();
        getActivity().finishAffinity();
    }

    @OnClick({R.id.btnProfileSubmit, R.id.btnProfileCameraHeader, R.id.btnPhotoCameraAvatar, R.id.lnProfileSendMessage, R.id.lnProfileChangeQuestion, R.id.lnProfileInvite, R.id.lnProfileLogout, R.id.lnProfileFragment, R.id.lnProfileBlockUser, R.id.lnProfileShareContact})
    public void onClick(View view) {
        if (view.getId() == R.id.btnProfileSubmit) {

            String email = edEmail.getText().toString().trim();
            String gender = edGender.getText().toString().trim().toUpperCase();
            String name = edFullName.getText().toString().trim();
            if (!TextUtils.isEmpty(Utils.isValidEmail(getActivity(), email))) {
                PopupUtils.showCustomDialog(getActivity(),getString(R.string.error),Utils.isValidEmail(getActivity(), email),R.string.cancel,null);
                return;
            }
            if (TextUtils.isEmpty(name)) {
                PopupUtils.showCustomDialog(getActivity(),getString(R.string.error),getString(R.string.error_empty_name),R.string.cancel,null);
                return;
            }
            edEmail.clearFocus();
            edGender.clearFocus();
            edFullName.clearFocus();
            edEmail.setEnabled(false);
            edFullName.setEnabled(false);
            edPhone.setTextColor(Color.BLACK);
            txtUsername.setTextColor(Color.BLACK);
            wpKMemberDto.setEmail(email);
            wpKMemberDto.setGender(gender);
            wpKMemberDto.setGiven(name);
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
        } else if (view.getId() == R.id.lnProfileSendMessage) {
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
//            ArrayList<BottomSheetCell> sheetCells = new ArrayList<>();
//            BottomSheetCell sheetCell = new BottomSheetCell(1, R.drawable.ic_menutab_logout, getString(R.string.logout_device));
//            sheetCells.add(sheetCell);
//            sheetCell = new BottomSheetCell(2, R.drawable.ic_logout_all, getString(R.string.logout_all_devices));
//            sheetCells.add(sheetCell);
//            BottomSheetDialog bottomSheetDialog = PopupUtils.createBottomSheet(getActivity(), sheetCells, new BottomSheetListener() {
//                @Override
//                public void onSelectBottomSheetCell(int index) {
//                    if (index == 1) {
//                        logout();
//                        //AppFuncs.alert(getActivity(), "Logout this device", true);
//                    } else if (index == 2) {
//                        logout();
//                        //AppFuncs.alert(getActivity(), getString(R.string.logout_all_devices), true);
//                    }
//                }
//            });
//            bottomSheetDialog.show();
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

    private void updateData() {
        JsonObject jsonObject = AppFuncs.convertClassToJsonObject(wpKMemberDto);
        AppFuncs.log("update: " + jsonObject.toString());
        RestAPI.PutDataWrappy(getActivity(), jsonObject, RestAPI.GET_MEMBER_INFO, new RestAPIListener(getActivity()) {
            @Override
            public void OnComplete(String s) {
                AppFuncs.alert(getActivity(), getString(R.string.update_profile_success), true);
                if (wpKMemberDto != null) {
                    Imps.Account.updateAccountFromDataServer(getActivity().getContentResolver(),wpKMemberDto,Store.getLongData(getActivity(),Store.ACCOUNT_ID));
                    emailTemp = wpKMemberDto.getEmail();
                    genderTemp = wpKMemberDto.getGender();
                    nameTemp = wpKMemberDto.getGiven();
                    String avatarReference = wpKMemberDto.getAvatar() != null ? wpKMemberDto.getAvatar().getReference() : "";
                    String bannerReference = wpKMemberDto.getBanner() != null ? wpKMemberDto.getBanner().getReference() : "";
                    String hash = DatabaseUtils.generateHashFromAvatar(avatarReference);
                    String address = wpKMemberDto.getXMPPAuthDto().getAccount() + Constant.EMAIL_DOMAIN;
                    DatabaseUtils.insertAvatarBlob(ImApp.sImApp.getContentResolver(), Imps.Avatars.CONTENT_URI, ImApp.sImApp.getDefaultProviderId(), ImApp.sImApp.getDefaultAccountId(), avatarReference, bannerReference, hash, address);
                    ImApp.broadcastIdentity(null);
                    spnProfile.setVisibility(View.INVISIBLE);
                    if (appDelegate != null) {
                        appDelegate.onChangeInApp(MainActivity.UPDATE_PROFILE_COMPLETE, "");
                    }
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
                } else {
                    AppFuncs.alert(getActivity(), getString(R.string.network_error), false);
                }
            }

            @Override
            protected void onError(int errorCode) {
                super.onError(errorCode);
                try {
                    edEmail.setText(emailTemp);
                    String upperString = genderTemp.substring(0, 1).toUpperCase() + genderTemp.substring(1).toLowerCase();
                    edGender.setText(upperString);
                    edFullName.setText(nameTemp);
                    wpKMemberDto.setEmail(emailTemp);
                    wpKMemberDto.setGender(genderTemp);
                    wpKMemberDto.setGiven(nameTemp);
                    spnProfile.setVisibility(View.INVISIBLE);
                } catch (Exception ex) {
                  ex.printStackTrace();
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
        edFullName.setEnabled(true);
        edFullName.setFocusable(true);
        edFullName.requestFocus();
        edFullName.setFocusableInTouchMode(true);
        edPhone.setTextColor(getResources().getColor(R.color.line));
        txtUsername.setTextColor(getResources().getColor(R.color.line));
        btnProfileSubmit.setVisibility(View.VISIBLE);
        spnProfile.setVisibility(View.VISIBLE);
    }

    @Override
    public void reloadFragment() {

    }
}
