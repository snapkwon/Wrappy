package net.wrappy.im.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ViewFlipper;

import net.wrappy.im.BuildConfig;
import net.wrappy.im.R;
import net.wrappy.im.helper.AppFuncs;
import net.wrappy.im.helper.layout.AppTextView;
import net.wrappy.im.provider.Store;
import net.wrappy.im.util.PopupUtils;
import net.wrappy.im.util.Utils;

import butterknife.BindView;
import butterknife.OnClick;


public class LauncherActivity extends BaseActivity {

    @BindView(R.id.viewFlipper1)
    ViewFlipper mViewFlipper;
    @BindView(R.id.edtUserMame)
    EditText mEditUsername;
    @BindView(R.id.txtLoginVersionName)
    AppTextView txtLoginVersionName;

    public static final int REQUEST_CODE_REGISTER = 1111;
    public static final int REQUEST_CODE_LOGIN = 1112;
    public static final int REQUEST_CODE_INPUT_NEW_PASSWORD = 1113;
    public static final int RESULT_ERROR = 4001;

    boolean isFlag;

    public static void start(Activity activity) {
        Intent intent = new Intent(activity, LauncherActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_launcher);
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        mViewFlipper.setDisplayedChild(0);

        String versionName = BuildConfig.VERSION_NAME;
        Utils.setTextForView(txtLoginVersionName, "v " + versionName);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultIntent) {
        switch (requestCode) {
            case RESULT_ERROR:
                if (resultIntent != null && !resultIntent.getStringExtra("error").isEmpty()) {
                    PopupUtils.showOKDialog(LauncherActivity.this, LauncherActivity.this.getString(R.string.error), resultIntent.getStringExtra("error"), null);
                }
                break;
        }
    }

    @OnClick({R.id.btnShowLogin, R.id.btnShowRegister, R.id.lnLoginFrame})
    public void onClick(View v) {
        if (isFlag) {
            return;
        }
        isFlag = true;
        AppFuncs.dismissKeyboard(LauncherActivity.this);
        switch (v.getId()) {
            case R.id.btnShowLogin:
                if (TextUtils.isEmpty(mEditUsername.getText().toString().trim())) {
                    PopupUtils.showCustomDialog(LauncherActivity.this, getString(R.string.warning), getString(R.string.error_empty_username)
                            , R.string.yes, null, false);
                } else {
                    Store.putStringData(getApplicationContext(), Store.USERNAME, mEditUsername.getText().toString().trim());
                    showLogin();
                }
                break;
            case R.id.btnShowRegister:
                showRegister();
                break;
        }
        isFlag = false;
    }


    private void showLogin() {

        Intent intent = PatternActivity.getStartIntent(LauncherActivity.this);
        Bundle arg = new Bundle();
        arg.putInt("type", REQUEST_CODE_LOGIN);
        arg.putString("username", mEditUsername.getText().toString().trim());
        intent.putExtras(arg);
        this.startActivityForResult(intent, RESULT_ERROR);

    }

    private void showRegister() {
        Intent intent = PatternActivity.getStartIntent(this);
        Bundle arg = new Bundle();
        arg.putInt("type", REQUEST_CODE_REGISTER);
        arg.putString("username", "");
        intent.putExtras(arg);
        startActivity(intent);
    }
}
