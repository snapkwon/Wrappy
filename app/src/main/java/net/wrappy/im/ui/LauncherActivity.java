package net.wrappy.im.ui;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.support.v7.widget.ListPopupWindow;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ViewFlipper;

import net.wrappy.im.ui.legacy.SimpleAlertHandler;
import net.wrappy.im.ui.onboarding.OnboardingAccount;
import net.wrappy.im.ui.onboarding.OnboardingActivity;

import net.wrappy.im.R;
import net.wrappy.im.util.PatternLockUtils;

import java.util.ArrayList;
import java.util.List;

import me.zhanghai.android.patternlock.PatternView;


public class LauncherActivity extends BaseActivity {

    private ViewFlipper mViewFlipper;
    private EditText mEditUsername;
    private Button mBtnForgetPass;
    private Button mBtnLogin;
    private Button mBtnregister;

    private PatternView patterview;

    private static final List<PatternView.Cell> LOGO_PATTERN = new ArrayList<>();
    static {
        LOGO_PATTERN.add(PatternView.Cell.of(0, 1));
        LOGO_PATTERN.add(PatternView.Cell.of(1, 0));
        LOGO_PATTERN.add(PatternView.Cell.of(2, 1));
        LOGO_PATTERN.add(PatternView.Cell.of(1, 2));
        LOGO_PATTERN.add(PatternView.Cell.of(1, 1));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        getSupportActionBar().setTitle("");

        View viewSplash = findViewById(R.id.flipViewMain);
        View viewRegister =  findViewById(R.id.flipViewRegister);
        View viewLogin = findViewById(R.id.flipViewLogin);
        View viewForgetPass = findViewById(R.id.flipViewForgetPass);

        mViewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper1);

        getSupportActionBar().hide();
        getSupportActionBar().setTitle("");

        mViewFlipper.setDisplayedChild(0);

        mEditUsername = (EditText)viewSplash.findViewById(R.id.edtUserMame);
        mBtnForgetPass = (Button)viewSplash.findViewById(R.id.btnforgetpass);
        mBtnLogin = (Button)viewSplash.findViewById(R.id.btnShowLogin);
        mBtnregister = (Button)viewSplash.findViewById(R.id.btnShowRegister);
        patterview = (PatternView)viewRegister.findViewById(R.id.pattern_view);

        patterview.setPattern(PatternView.DisplayMode.Animate, LOGO_PATTERN);

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
        getSupportActionBar().show();
        mViewFlipper.setDisplayedChild(3);

    }

    private void showRegister()
    {
        this.startActivityForResult(new Intent(this, SetPatternActivity.class), 1);
        //getSupportActionBar().show();
      //  mViewFlipper.setDisplayedChild(2);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            String result = data.getStringExtra("result");
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


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

}
