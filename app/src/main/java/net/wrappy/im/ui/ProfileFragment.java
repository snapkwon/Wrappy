package net.wrappy.im.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Response;

import net.wrappy.im.ImApp;
import net.wrappy.im.R;
import net.wrappy.im.helper.AppFuncs;
import net.wrappy.im.helper.RestAPI;
import net.wrappy.im.helper.glide.GlideHelper;
import net.wrappy.im.helper.layout.AppTextView;
import net.wrappy.im.model.Avatar;
import net.wrappy.im.model.BottomSheetCell;
import net.wrappy.im.model.BottomSheetListener;
import net.wrappy.im.model.WpKMemberDto;
import net.wrappy.im.util.PopupUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by ben on 18/12/2017.
 */

public class ProfileFragment extends Fragment {

    private final int AVATAR = 321;
    View mainView;
    AppFuncs appFuncs;
    String jid = "";
    WpKMemberDto wpKMemberDto;
    private long mContactId = -1;
    private String mNickname = null;
    private String mUsername = null;
    private long mProviderId = -1;
    private long mAccountId = -1;
    private String reference = "";
    private ImApp mApp;
    private boolean isSelf;
    Bitmap photoAvatar;

    @BindView(R.id.imgProfileHeader)
    ImageView imgProfileHeader;
    @BindView(R.id.imgPhotoAvatar)
    ImageView imgPhotoAvatar;
    @BindView(R.id.txtProfileUsername)
    AppTextView txtUsername;
    @BindView(R.id.txtProfilePhone)
    AppTextView txtPhone;
    @BindView(R.id.txtProfileEmail)
    AppTextView txtEmail;
    @BindView(R.id.txtProfileGender)
    AppTextView txtGender;
    @BindView(R.id.linearForSeft)
    LinearLayout linearForSeft;
    @BindView(R.id.linearForContact)
    LinearLayout linearForContact;
    @BindView(R.id.btnPhotoCameraAvatar)
    ImageButton btnPhotoCameraAvatar;

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

        return mainView;
    }

    private void preferenceView() {
        if (isSelf) {
            btnPhotoCameraAvatar.setVisibility(View.VISIBLE);
        }
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
                            wpKMemberDto = gson.fromJson(result.getResult(), WpKMemberDto.getType());
                            txtUsername.setText(wpKMemberDto.getIdentifier());
                            txtEmail.setText(wpKMemberDto.getEmail());
                            txtPhone.setText(wpKMemberDto.getMobile());
                            txtGender.setText(wpKMemberDto.getGender());
                            if (wpKMemberDto.getAvatar() != null) {
                                GlideHelper.loadBitmapToCircleImage(getContext(), imgPhotoAvatar, RestAPI.getAvatarUrl(wpKMemberDto.getAvatar().getReference()));
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
                    AppFuncs.alert(getActivity(), "Connnect time out", true);
                    getActivity().finish();
                }
            }
        });
    }

    @OnClick({R.id.btnPhotoCameraAvatar, R.id.lnProfileSendMessage, R.id.lnProfileChangeQuestion, R.id.lnProfileLogout})
    public void onClick(View view) {
        if (view.getId() == R.id.btnPhotoCameraAvatar) {
            ArrayList<BottomSheetCell> sheetCells = new ArrayList<>();
            BottomSheetCell sheetCell = new BottomSheetCell(1,R.drawable.ic_choose_camera, "Take Photo");
            sheetCells.add(sheetCell);
            sheetCell = new BottomSheetCell(2,R.drawable.ic_choose_gallery,"Choose from Gallery");
            sheetCells.add(sheetCell);
            if (wpKMemberDto!=null) if (wpKMemberDto.getAvatar()!=null) if (!TextUtils.isEmpty(wpKMemberDto.getAvatar().getReference())){
                sheetCell = new BottomSheetCell(3,R.drawable.setting_delete,"Delete Photo");
                sheetCells.add(sheetCell);
            }
            PopupUtils.createBottomSheet(getActivity(), sheetCells, new BottomSheetListener() {
                @Override
                public void onSelectBottomSheetCell(int index) {
                    switch (index) {
                        case 1:
                            AppFuncs.openCamera(getActivity(), AVATAR);
                            break;
                        case 2:
                            AppFuncs.openGallery(getActivity(), AVATAR);
                            break;
                        case 3:
                            photoAvatar = null;
                            imgPhotoAvatar.setImageResource(R.drawable.avatar);
                            if (wpKMemberDto.getAvatar()!=null) if (!TextUtils.isEmpty(wpKMemberDto.getAvatar().getReference())) {
                                RestAPI.apiDELETE(getActivity(),RestAPI.DELETE_AVATAR,new JsonObject()).setCallback(new FutureCallback<Response<String>>() {
                                    @Override
                                    public void onCompleted(Exception e, Response<String> result) {
                                        if (result!=null) {
                                            if (RestAPI.checkHttpCode(result.getHeaders().code())) {
                                                wpKMemberDto.setAvatar(null);
                                                AppFuncs.alert(getActivity(),"Remove Avatar Success",true);
                                            }
                                        }
                                    }
                                });
                            }
                            break;
                        default:
                    }
                }
            }).show();
        }else if (view.getId() == R.id.lnProfileSendMessage) {
            startChat();
        } else if (view.getId() == R.id.lnProfileChangeQuestion) {
            Intent intent = new Intent(getActivity(), SecurityQuestionActivity.class);
            startActivity(intent);
        } else if (view.getId() == R.id.lnProfileLogout) {
            ArrayList<BottomSheetCell> sheetCells = new ArrayList<>();
            BottomSheetCell sheetCell = new BottomSheetCell(1, R.drawable.ic_menutab_logout, "Logout this device");
            sheetCells.add(sheetCell);
            sheetCell = new BottomSheetCell(2, R.drawable.ic_logout_all, "Logout all devices");
            sheetCells.add(sheetCell);
            BottomSheetDialog bottomSheetDialog = PopupUtils.createBottomSheet(getActivity(), sheetCells, new BottomSheetListener() {
                @Override
                public void onSelectBottomSheetCell(int index) {
                    if (index == 1) {
                        AppFuncs.alert(getActivity(), "Logout this device", true);
                    } else if (index == 2) {
                        AppFuncs.alert(getActivity(), "Logout all devices", true);
                    }
                }
            });
            bottomSheetDialog.show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==AVATAR) {
            appFuncs.showProgressWaiting(getActivity());
            photoAvatar = AppFuncs.getBitmapFromIntentResult(getActivity(),data);
            RestAPI.uploadFile(getActivity(),AppFuncs.convertBitmapToFile(getActivity(),photoAvatar),RestAPI.PHOTO_AVATAR).setCallback(new FutureCallback<Response<String>>() {
                @Override
                public void onCompleted(Exception e, Response<String> result) {
                    appFuncs.dismissProgressWaiting();
                    try {
                        String reference = RestAPI.getPhotoReference(result.getResult());
                        Avatar avatar = new Avatar(reference);
                        wpKMemberDto.setAvatar(avatar);
                        updateData();
                    }catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
            getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                imgPhotoAvatar.setImageBitmap(photoAvatar);
                                            }
                                        }
            );

        }
    }

    private void updateData() {
        JsonObject jsonObject = AppFuncs.convertClassToJsonObject(wpKMemberDto);
        RestAPI.apiPUT(getActivity(),RestAPI.GET_MEMBER_INFO,jsonObject).setCallback(new FutureCallback<Response<String>>() {
            @Override
            public void onCompleted(Exception e, Response<String> result) {
                if (result!=null) {
                    if (RestAPI.checkHttpCode(result.getHeaders().code())) {
                        AppFuncs.alert(getActivity(),"Update Success",true);
                    } else {
                        imgPhotoAvatar.setImageResource(R.drawable.avatar);
                        AppFuncs.alert(getActivity(),"Update Fail",true);
                    }
                }
            }
        });
    }

    public void startChat() {

        Intent intent = ConversationDetailActivity.getStartIntent(getActivity());
        intent.putExtra("id", mContactId);
        intent.putExtra("nickname", mNickname);
        intent.putExtra("reference", reference);
        startActivity(intent);
        getActivity().finish();


    }
}
