package net.wrappy.im.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.gson.Gson;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Response;

import net.wrappy.im.ImApp;
import net.wrappy.im.R;
import net.wrappy.im.helper.AppFuncs;
import net.wrappy.im.helper.RestAPI;
import net.wrappy.im.helper.glide.GlideHelper;
import net.wrappy.im.helper.layout.AppTextView;
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
    @BindView(R.id.txtProfileName)
    AppTextView txtProfileName;
    @BindView(R.id.btnChangeSecuQuestion)
    LinearLayout btnChangeSecuQuestion;
    @BindView(R.id.btnLogout)
    LinearLayout btnLogout;
    @BindView(R.id.linearForSeft)
    LinearLayout linearForSeft;
    @BindView(R.id.linearForContact)
    LinearLayout linearForContact;

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
        if (jid.isEmpty()) {
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
        txtProfileName.setText(mNickname);
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

    @OnClick({R.id.txtProfileSendMessage, R.id.txtProfileChangeQuestion, R.id.txtProfileLogout})
    public void onClick(View view) {
        if (view.getId() == R.id.txtProfileSendMessage) {
            startChat();
        } else if (view.getId() == R.id.txtProfileChangeQuestion) {
            Intent intent = new Intent(getActivity(), SecurityQuestionActivity.class);
            startActivity(intent);
        } else if (view.getId() == R.id.txtProfileLogout) {
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

    public void startChat() {

        Intent intent = ConversationDetailActivity.getStartIntent(getActivity());
        intent.putExtra("id", mContactId);
        intent.putExtra("nickname", mNickname);
        intent.putExtra("reference", reference);
        startActivity(intent);
        getActivity().finish();


    }
}
