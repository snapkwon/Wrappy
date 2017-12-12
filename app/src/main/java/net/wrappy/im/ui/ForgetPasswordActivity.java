package net.wrappy.im.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import net.wrappy.im.R;
import net.wrappy.im.helper.AppDelegate;
import net.wrappy.im.helper.layout.AppTextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by ben on 07/12/2017.
 */

public class ForgetPasswordActivity extends Activity implements AppDelegate {

    public static final int ACTION_FROM_QUESTION = 1;
    public static final int ACTION_FROM_RESET_EMAIL = 2;
    public static final int ACTION_FROM_CHECK_EMAIL = 3;
    public static final String FORGET_PASSWORD = "forgetpassword";



    public static void start(Activity activity) {
        Intent intent = new Intent(activity,ForgetPasswordActivity.class);
        activity.startActivity(intent);
    }


    @BindView(R.id.headerbarTitle) AppTextView headerbarTitle;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forget_password_question_activity);
        ButterKnife.bind(this);
        headerbarTitle.setText("FORGET PASSWORD");
        goToQuestionFragment();

    }

    @OnClick(R.id.headerbarBack)
    public void headerBack(View v) {
        LauncherActivity.start(ForgetPasswordActivity.this);
        finish();
    }

    private void goToQuestionFragment() {
        getFragmentManager().beginTransaction().replace(R.id.forgetPasswordContainer,ForgetPasswordQuestionFragment.newInstance()).commit();
    }

    private void goToResetEmailFragment() {
        getFragmentManager().beginTransaction().replace(R.id.forgetPasswordContainer,ForgetPasswordResetEmailFragment.newInstance()).commit();
    }

    private void goToCheckEmailFragment() {
        getFragmentManager().beginTransaction().replace(R.id.forgetPasswordContainer,ForgetPasswordCheckEmailFragment.newInstance()).commit();
    }


    @Override
    public void onChangeInApp(int id, String data) {
        switch (id) {
            case ACTION_FROM_QUESTION:
                if (data.isEmpty()) {
                    goToResetEmailFragment();
                } else {
                    data = data.replaceAll("\"","");
                    if (data.length() > 10) {
                        Intent intent= PatternActivity.getStartIntent(this);
                        Bundle arg = new Bundle();
                        arg.putInt("type",LauncherActivity.REQUEST_CODE_REGISTER);
                        arg.putString("username" , "");
                        arg.putString(FORGET_PASSWORD,data);
                        intent.putExtras(arg);
                        startActivity(intent);
                        finish();
                    }
                }

                break;
            case ACTION_FROM_RESET_EMAIL:
                goToCheckEmailFragment();
                break;
            case ACTION_FROM_CHECK_EMAIL:
                break;
            default:
        }
    }
}
