package net.wrappy.im.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ViewFlipper;

import net.wrappy.im.R;
import net.wrappy.im.provider.Store;
import net.wrappy.im.util.PopupUtils;


public class LauncherActivity extends BaseActivity {

    private ViewFlipper mViewFlipper;
    private EditText mEditUsername;
    private Button mBtnLogin;
    private Button mBtnregister;
    public static final int REQUEST_CODE_REGISTER = 1111;
    public static final int REQUEST_CODE_LOGIN = 1112;

    public static void start(Activity activity) {
        Intent intent = new Intent(activity, LauncherActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }

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

        mEditUsername = (EditText) viewSplash.findViewById(R.id.edtUserMame);
        mBtnLogin = (Button) viewSplash.findViewById(R.id.btnShowLogin);
        mBtnregister = (Button) viewSplash.findViewById(R.id.btnShowRegister);

        mBtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEditUsername.getText().toString().isEmpty()) {
                    PopupUtils.showCustomDialog(LauncherActivity.this, getString(R.string.warning), getString(R.string.error_empty_username)
                            , R.string.yes, null, false);
                } else {
                    Store.putStringData(getApplicationContext(), Store.USERNAME, mEditUsername.getText().toString().trim());
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

    private void showPrevious() {
        setAnimRight();
        getSupportActionBar().setTitle("");

        if (mViewFlipper.getCurrentView().getId() == R.id.flipViewMain) {
            finish();
        } else if (mViewFlipper.getCurrentView().getId() == R.id.flipViewLogin
                || mViewFlipper.getCurrentView().getId() == R.id.flipViewRegister) {
            showSplash();
        }

    }

    private void showSplash() {
        getSupportActionBar().hide();
        mViewFlipper.setDisplayedChild(0);

    }

    private void showLogin() {

        Intent intent = PatternActivity.getStartIntent(LauncherActivity.this);
        Bundle arg = new Bundle();
        arg.putInt("type", REQUEST_CODE_LOGIN);
        arg.putString("username", mEditUsername.getText().toString().trim());
        intent.putExtras(arg);
        this.startActivity(intent);

    }

    private void showRegister() {
        Intent intent = PatternActivity.getStartIntent(this);
        Bundle arg = new Bundle();
        arg.putInt("type", REQUEST_CODE_REGISTER);
        arg.putString("username", "");
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


    private void setAnimLeft() {
        Animation animIn = AnimationUtils.loadAnimation(this, R.anim.push_left_in);
        Animation animOut = AnimationUtils.loadAnimation(this, R.anim.push_left_out);
        mViewFlipper.setInAnimation(animIn);
        mViewFlipper.setOutAnimation(animOut);
    }

    private void setAnimRight() {
        Animation animIn = AnimationUtils.loadAnimation(LauncherActivity.this, R.anim.push_right_in);
        Animation animOut = AnimationUtils.loadAnimation(LauncherActivity.this, R.anim.push_right_out);
        mViewFlipper.setInAnimation(animIn);
        mViewFlipper.setOutAnimation(animOut);
    }
}
