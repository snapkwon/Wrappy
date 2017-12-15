package net.wrappy.im.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Response;

import net.wrappy.im.R;
import net.wrappy.im.helper.AppFuncs;
import net.wrappy.im.helper.RestAPI;
import net.wrappy.im.helper.layout.AppTextView;
import net.wrappy.im.model.WpKMemberDto;
import net.wrappy.im.service.IImConnection;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by ben on 14/12/2017.
 */

public class ProfileActivity extends BaseActivity {


    AppFuncs appFuncs;
    String jid = "";
    WpKMemberDto wpKMemberDto;

    @BindView(R.id.headerbarTitle) AppTextView headerbarTitle;
    @BindView(R.id.imgProfileHeader) ImageView imgProfileHeader;
    @BindView(R.id.imgPhotoAvatar) ImageView imgPhotoAvatar;
    @BindView(R.id.txtProfileUsername) AppTextView txtUsername;
    @BindView(R.id.txtProfilePhone) AppTextView txtPhone;
    @BindView(R.id.txtProfileEmail) AppTextView txtEmail;
    @BindView(R.id.txtProfileName) AppTextView txtProfileName;

    private long mContactId = -1;
    private String mNickname = null;
    private String mUsername = null;
    private long mProviderId = -1;
    private long mAccountId = -1;
    private String reference = "";
    private IImConnection mConn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.profile_activity);
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        appFuncs = AppFuncs.getInstance();
        jid = getIntent().getStringExtra("address");
        String[] arr = jid.split("@");
        jid = arr[0];
        preferenceView();
        mContactId = getIntent().getLongExtra("contactId", -1);

        mNickname = getIntent().getStringExtra("nickname");
        mUsername = getIntent().getStringExtra("address");
        mProviderId = getIntent().getLongExtra("provider", -1);
        mAccountId = getIntent().getLongExtra("account", -1);
        reference = getIntent().getStringExtra("reference");
    }

    private void preferenceView() {
        headerbarTitle.setText(getIntent().getStringExtra("nickname"));
        txtProfileName.setText(getIntent().getStringExtra("nickname"));
        getDataMember();
    }

    private void getDataMember() {
        RestAPI.apiGET(getApplicationContext(),RestAPI.getMemberByIdUrl(jid)).setCallback(new FutureCallback<Response<String>>() {
            @Override
            public void onCompleted(Exception e, Response<String> result) {
                if (result!=null) {
                    if (RestAPI.checkHttpCode(result.getHeaders().code())) {
                        Gson gson = new Gson();
                        try {
                            wpKMemberDto = gson.fromJson(result.getResult(),WpKMemberDto.getType());
                            txtUsername.setText(wpKMemberDto.getIdentifier());
                            txtEmail.setText(wpKMemberDto.getEmail());
                            txtPhone.setText(wpKMemberDto.getMobile());
                            RestAPI.getBitmapFromUrl(getApplicationContext(),wpKMemberDto.getReference()).setCallback(new FutureCallback<Bitmap>() {
                                @Override
                                public void onCompleted(Exception e, Bitmap result) {
                                    if (result!=null) {
                                        imgPhotoAvatar.setImageBitmap(result);
                                    }

                                }
                            });
                            //RestAPI.loadImageUrl(getApplicationContext(),imgPhotoAvatar,wpKMemberDto.getReference());
                        }catch (Exception ex) {
                            ex.printStackTrace();
                        }

                    }
                } else {
                    AppFuncs.alert(getApplicationContext(),"Connnect time out",true);
                    finish();
                }
            }
        });
    }

    @OnClick({R.id.headerbarBack, R.id.btnProfileSendMessage})
    public void onClick(View view) {
        if (view.getId()==R.id.headerbarBack) {
            onBackPressed();
        } else if (view.getId()==R.id.btnProfileSendMessage) {
            startChat();
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public void startChat ()
    {

        Intent intent = ConversationDetailActivity.getStartIntent(this);
        intent.putExtra("id", mContactId);
        intent.putExtra("nickname", mNickname);
        intent.putExtra("reference", reference);
        startActivity(intent);
        finish();


    }
}
