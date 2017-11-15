package net.wrappy.im.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ViewFlipper;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.wrappy.im.R;
import net.wrappy.im.helper.RestAPI;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import me.tornado.android.patternlock.PatternView;


public class LauncherActivity extends BaseActivity implements RestAPI.RectAPIListenner {

    private ViewFlipper mViewFlipper;
    private EditText mEditUsername;
    private Button mBtnForgetPass;
    private Button mBtnLogin;
    private Button mBtnregister;
    public static final int REQUEST_CODE_REGISTER = 1111;
    public static final int REQUEST_CODE_LOGIN = 1112;


    private static final List<PatternView.Cell> LOGO_PATTERN = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        getSupportActionBar().setTitle("");

        View viewSplash = findViewById(R.id.flipViewMain);
        View viewForgetPass = findViewById(R.id.flipViewForgetPass);

        mViewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper1);

        getSupportActionBar().hide();
        getSupportActionBar().setTitle("");

        mViewFlipper.setDisplayedChild(0);

        mEditUsername = (EditText)viewSplash.findViewById(R.id.edtUserMame);
        mBtnForgetPass = (Button)viewSplash.findViewById(R.id.btnforgetpass);
        mBtnLogin = (Button)viewSplash.findViewById(R.id.btnShowLogin);
        mBtnregister = (Button)viewSplash.findViewById(R.id.btnShowRegister);

        mBtnForgetPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showForgetPass();
            }
        });

        mBtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogin();
            }
        });

        mBtnregister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRegister();
            }
        });


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:

                showPrevious();

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showPrevious()
    {
        setAnimRight();
        getSupportActionBar().setTitle("");

        if (mViewFlipper.getCurrentView().getId()==R.id.flipViewMain)
        {
            finish();
        }
        else if (mViewFlipper.getCurrentView().getId()==R.id.flipViewForgetPass
                ||mViewFlipper.getCurrentView().getId()==R.id.flipViewLogin
                ||mViewFlipper.getCurrentView().getId()==R.id.flipViewRegister )
        {
            showSplash();
        }

    }

    private void showSplash ()
    {
        getSupportActionBar().hide();
        mViewFlipper.setDisplayedChild(0);

    }

    private void showForgetPass ()
    {
        getSupportActionBar().show();
        mViewFlipper.setDisplayedChild(1);

    }

    private void showLogin()
    {

        this.startActivity(new Intent(this, PatternActivity.class));
        finish();

    }

    private void showRegister()
    {
        new RestAPI.PostDataUrl(null, this).execute(RestAPI.POST_REGISTER);
    }

   /* @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_REGISTER) {
                String result = data.getStringExtra("result");

                JsonArray jsonArray = new JsonArray();

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("UserName",mEditUsername.getText().toString());
                jsonObject.addProperty("PassWord",result);


                jsonArray.add(jsonObject);
                new RestAPI.PostDataUrl(jsonArray.toString(), this).execute(RestAPI.POST_REGISTER);
            } else  if (requestCode == REQUEST_CODE_LOGIN) {
                String result = data.getStringExtra("result");
            }
        }
    }*/


    private void setAnimLeft ()
    {
        Animation animIn = AnimationUtils.loadAnimation(this, R.anim.push_left_in);
        Animation animOut = AnimationUtils.loadAnimation(this, R.anim.push_left_out);
        mViewFlipper.setInAnimation(animIn);
        mViewFlipper.setOutAnimation(animOut);
    }

    private void setAnimRight ()
    {
        Animation animIn = AnimationUtils.loadAnimation(LauncherActivity.this, R.anim.push_right_in);
        Animation animOut = AnimationUtils.loadAnimation(LauncherActivity.this, R.anim.push_right_out);
        mViewFlipper.setInAnimation(animIn);
        mViewFlipper.setOutAnimation(animOut);
    }

    @Override
    public void OnComplete(String error, String s) {
        JSONObject mainObject = null;
        try {
              mainObject = new JSONObject(s);
              JSONObject uniObject = mainObject.getJSONObject("data");
              int  status = mainObject.getInt("status");
              if(status == 1) {
                  String username = uniObject.getString("jid");
                  Intent intent = new Intent(this, PatternActivity.class);
                  intent.putExtra("username" , username);
                  this.startActivity(new Intent(this, PatternActivity.class));
                  finish();
              }


        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
