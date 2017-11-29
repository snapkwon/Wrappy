package net.wrappy.im.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
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


public class LauncherActivity extends BaseActivity implements RestAPI.RestAPIListenner {

    private ViewFlipper mViewFlipper;
    private EditText mEditUsername;
    private Button mBtnForgetPass;
    private Button mBtnLogin;
    private Button mBtnregister;
    public static final int REQUEST_CODE_REGISTER = 1111;
    public static final int REQUEST_CODE_LOGIN = 1112;

    int type_request;


    private static final List<PatternView.Cell> LOGO_PATTERN = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        getSupportActionBar().setTitle("");

        View viewSplash = findViewById(R.id.flipViewMain);

        mViewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper1);

        getSupportActionBar().hide();
        getSupportActionBar().setTitle("");

        mViewFlipper.setDisplayedChild(0);

        mEditUsername = (EditText)viewSplash.findViewById(R.id.edtUserMame);
        mBtnLogin = (Button)viewSplash.findViewById(R.id.btnShowLogin);
        mBtnregister = (Button)viewSplash.findViewById(R.id.btnShowRegister);

        mBtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mEditUsername.getText().toString().isEmpty())
                {
                    AlertDialog alertDialog = new AlertDialog.Builder(LauncherActivity.this).create();
                    alertDialog.setTitle("Warning");
                    alertDialog.setMessage("username cannot be empty");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                }
                else {
                    showLogin();
                }
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
        else if (mViewFlipper.getCurrentView().getId()==R.id.flipViewLogin
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

    private void showLogin()
    {

        Intent intent= new Intent(this, PatternActivity.class);
        Bundle arg = new Bundle();
        arg.putInt("type",REQUEST_CODE_LOGIN);
        arg.putString("username" , mEditUsername.getText().toString().trim());
        intent.putExtras(arg);
        this.startActivity(intent);

    }

    private void showRegister()
    {
        Intent intent= new Intent(this, PatternActivity.class);
        Bundle arg = new Bundle();
        arg.putInt("type",REQUEST_CODE_REGISTER);
        arg.putString("username" , "");
        intent.putExtras(arg);
        startActivity(intent);
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
    public void OnInit() {

    }

    @Override
    public void OnComplete(int HTTP_CODE, String error, String s) {
        JSONObject mainObject = null;
        try {
              mainObject = new JSONObject(s);
              JSONObject uniObject = mainObject.getJSONObject("data");
              int  status = mainObject.getInt("status");
              if(status == 1) {
                  String username = uniObject.getString("jid");
                  String password = uniObject.getString("xmppPass");
                  Intent intent = new Intent(this, PatternActivity.class);
                  Bundle arg = new Bundle();
                  arg.putInt("type",type_request);
                  arg.putString("username" , username);
                  arg.putString("password" , password);
                  intent.putExtras(arg);
                  this.startActivity(intent);
                 // finish();
              }


        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
