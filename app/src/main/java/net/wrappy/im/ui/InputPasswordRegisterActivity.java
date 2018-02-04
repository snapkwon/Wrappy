package net.wrappy.im.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import net.wrappy.im.MainActivity;
import net.wrappy.im.R;
import net.wrappy.im.model.WpKAuthDto;
import net.wrappy.im.util.PopupUtils;

import javax.annotation.Nullable;

import butterknife.BindView;
import butterknife.ButterKnife;

public class InputPasswordRegisterActivity extends BaseActivity {


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

    private String patternPassword;

    private void showQuestionScreen(String pass ,String passcode) {
            Intent intent = new Intent(this, RegistrationSecurityQuestionActivity.class);
            WpKAuthDto wpKAuthDto = new WpKAuthDto(pass,passcode);
            wpKAuthDto.setPasscode(passcode);
            intent.putExtra(UpdateProfileActivity.PASSCODE,passcode);
            intent.putExtra(WpKAuthDto.class.getName(), wpKAuthDto);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_password_register);

        patternPassword = getIntent().getStringExtra(PatternActivity.PASSWORD_INPUT);

        ButterKnife.bind(this);
        initViews();

        mBtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = mEditPassword.getText().toString();
                if(password.isEmpty())
                {
                    PopupUtils.showCustomDialog(InputPasswordRegisterActivity.this,getString(R.string.warning),getString(R.string.password_is_empty), R.string.ok, null);
                }
                else
                {
                    if(passwordValidation(password)==true) {
                        //passwordValidation(mEditPassword.getText().toString());
                        if(password.equals(mEditConfirmPassword.getText().toString()))
                        {
                            showQuestionScreen(patternPassword,password);
                        }
                        else
                        {
                            PopupUtils.showCustomDialog(InputPasswordRegisterActivity.this,getString(R.string.warning),getString(R.string.lock_screen_passphrases_not_matching), R.string.ok, null);
                        }
                    }
                    else
                    {
                        PopupUtils.showCustomDialog(InputPasswordRegisterActivity.this,getString(R.string.warning),getString(R.string.wrong_format_password), R.string.ok, null);
                    }
                }
            }
        });
    }

    boolean passwordValidation (String password){
        String pattern = "(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}";
        return password.matches(pattern);
    }

    private void initViews() {
        initActionBarDefault(true, R.string.registration);

        mTitlePage.setText(R.string.create_new_password);
        mTitlePassword.setText(R.string.create_new_password);
        mEditPassword.setHint(R.string.input_your_password);
        mEditConfirmPassword.setHint(R.string.input_repeat_password);
        mBtnLogin.setText(R.string.btn_next);

        mEditPassword.setText("");
        mEditConfirmPassword.setText("");



        mEditPassword.setOnTouchListener(new View.OnTouchListener() {
            boolean isVisible = false;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {

                    if (motionEvent.getRawX() >= (mEditPassword.getRight() - mEditPassword.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {

                        if (!isVisible) {
                            mEditPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                            mEditPassword.setSelection(mEditPassword.length());
                            isVisible = true;
                        } else {
                            mEditPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                            mEditPassword.setSelection(mEditPassword.length());
                            isVisible = false;
                        }
                    }
                }
                return false;
            }
        });
    }
}
