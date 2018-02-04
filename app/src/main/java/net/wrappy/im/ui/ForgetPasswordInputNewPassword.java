package net.wrappy.im.ui;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import net.wrappy.im.R;

import javax.annotation.Nullable;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ForgetPasswordInputNewPassword extends BaseActivity {

    @Nullable
    @BindView(R.id.tile_input_password_page)
    TextView mTitlePage;
    @Nullable
    @BindView(R.id.title_password)
    TextView mTitlePassword;
    @Nullable
    @BindView(R.id.edtpassword)
    EditText mEditPassword;
    @Nullable
    @BindView(R.id.edtconfirmpassword)
    EditText mEditConfirmPassword;
    @Nullable
    @BindView(R.id.btcreatepassword)
    Button mBtnLogin;

    public static void start(Activity activity) {
        Intent intent = new Intent(activity, ForgetPasswordInputNewPassword.class);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_password_register);

        ButterKnife.bind(this);
        initViews();
    }

    private void initViews() {
        initActionBarDefault(true, R.string.forget_password);

        mTitlePage.setText(R.string.input_new_password);
        mTitlePassword.setText(R.string.input_new_password);
        mEditPassword.setHint(R.string.input_your_password);
        mEditConfirmPassword.setHint(R.string.input_repeat_password);
        mBtnLogin.setText(R.string.login);

        mEditPassword.setText("");
        mEditConfirmPassword.setText("");

        showHidePassword(mEditPassword);
        showHidePassword(mEditConfirmPassword);
    }
}
