package net.wrappy.im.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.google.gson.JsonObject;

import net.wrappy.im.MainActivity;
import net.wrappy.im.R;
import net.wrappy.im.helper.AppFuncs;
import net.wrappy.im.helper.RestAPI;
import net.wrappy.im.helper.layout.AppButton;
import net.wrappy.im.helper.layout.AppEditTextView;
import net.wrappy.im.helper.layout.AppTextView;
import net.wrappy.im.helper.layout.CircleImageView;
import net.wrappy.im.util.SecureMediaStore;

/**
 * Created by ben on 15/11/2017.
 */

public class UpdateProfileActivity extends BaseActivity implements View.OnClickListener {

    private final int IMAGE_HEADER = 100;
    private final int IMAGE_AVARTA = 101;

    ImageButton headerbarBack;
    AppTextView headerbarTitle;
    AppEditTextView edUsername, edEmail, edPhone, edOther;
    AppButton btnComplete, btnSkip;
    ImageButton btnCameraHeader,btnCameraAvarta;
    CircleImageView imgAvarta;
    ImageView imgHeader;
    boolean isFlag;
    String user,email,phone,other;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.update_profile_activity);
        preferenceView();
    }

    private void preferenceView() {
        headerbarBack = (ImageButton) findViewById(R.id.headerbarBack);
        headerbarBack.setOnClickListener(this);
        headerbarTitle = (AppTextView) findViewById(R.id.headerbarTitle);
        headerbarTitle.setText("Update Profile");
        //
        edUsername = (AppEditTextView) findViewById(R.id.edProfileUsername);
        edEmail = (AppEditTextView) findViewById(R.id.edProfileEmail);
        edPhone = (AppEditTextView) findViewById(R.id.edProfilePhone);
        edOther = (AppEditTextView) findViewById(R.id.edProfileOther);
        btnComplete = (AppButton) findViewById(R.id.btnProfileComplete);
        btnComplete.setOnClickListener(this);
        btnSkip = (AppButton) findViewById(R.id.btnProfileSkip);
        btnSkip.setOnClickListener(this);
        btnCameraAvarta = (ImageButton) findViewById(R.id.btnProfileCameraAvarta);
        btnCameraAvarta.setOnClickListener(this);
        btnCameraHeader = (ImageButton) findViewById(R.id.btnProfileCameraHeader);
        btnCameraHeader.setOnClickListener(this);
        imgAvarta = (CircleImageView) findViewById(R.id.imgProfileAvarta);
        imgHeader = (ImageView) findViewById(R.id.imgProfileHeader);
    }

    @Override
    public void onClick(View view) {
        if (isFlag) {
            return;
        } isFlag = true;
        try {
            if (view.getId() == headerbarBack.getId()) {
                finish();
            }
            if (view.getId() == btnComplete.getId()) {
                if (!validateData().isEmpty()) {
                    AppFuncs.alert(getApplicationContext(),validateData(),true);
                    return;
                }
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("jid","jid@wrappy.net");
                jsonObject.addProperty("username",user);
                jsonObject.addProperty("email",email);
                String respond = new RestAPI.PostDataUrl(jsonObject.toString(),null).execute(RestAPI.POST_UPDATE_EMAIL_USERNAME).get();
                JsonObject object = RestAPI.parseStringToJsonElement(respond).getAsJsonObject();
                if (RestAPI.getStatus(object)==0) {
                    AppFuncs.alert(getApplicationContext(),RestAPI.getDescription(object),true);
                    return;
                }
                // success
                AppFuncs.alert(getApplicationContext(),"Updated",true);
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            }
            if (view.getId() == btnSkip.getId()) {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();

            }
            if (view.getId() == btnCameraAvarta.getId()) {
                AppFuncs.getImageFromDevice(this,IMAGE_AVARTA);
            }
            if (view.getId() == btnCameraHeader.getId()) {
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
            other = edOther.getText().toString().trim();
            if (user.isEmpty()) {
                error = "Username is empty";
            } else if (user.length() < 6) {
                error = "Length of username must be more than 6 characters";
            } else if (AppFuncs.detectSpecialCharacters(user)) {
                error = "Username contains special characters";
            } else if (email.isEmpty()) {
                error = "Email is empty";
            } else if (!AppFuncs.isEmailValid(email)) {
                error = "Invalid email format";
            } else {}
        }catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            return error;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (data!=null) {
                if (requestCode==IMAGE_HEADER) {
                    Bitmap bmpThumbnail = SecureMediaStore.getThumbnailFile(UpdateProfileActivity.this, data.getData(), 512);
                    imgHeader.setImageBitmap(bmpThumbnail);
                } else if (requestCode == IMAGE_AVARTA) {
                    Bitmap bmpThumbnail = SecureMediaStore.getThumbnailFile(UpdateProfileActivity.this, data.getData(), 512);
                    imgAvarta.setImageBitmap(bmpThumbnail);
                }
            }
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
