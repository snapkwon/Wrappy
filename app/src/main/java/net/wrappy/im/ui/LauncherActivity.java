package net.wrappy.im.ui;

import android.os.Bundle;
import android.app.Activity;
import android.support.v7.widget.ListPopupWindow;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ViewFlipper;

import net.wrappy.im.ui.legacy.SimpleAlertHandler;
import net.wrappy.im.ui.onboarding.OnboardingAccount;
import net.wrappy.im.ui.onboarding.OnboardingActivity;

import net.wrappy.im.R;


public class LauncherActivity extends BaseActivity {

    private ViewFlipper mViewFlipper;
    private EditText mEditUsername;
    private View mSetupProgress;

    private ImageView mImageAvatar;

    private MenuItem mItemSkip = null;

    private EditText mSpinnerDomains;

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

        mViewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper1);

        mViewFlipper.setDisplayedChild(0);


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
