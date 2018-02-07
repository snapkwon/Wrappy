package net.wrappy.im.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yalantis.ucrop.UCrop;

import net.ironrabbit.type.CustomTypefaceManager;
import net.wrappy.im.R;
import net.wrappy.im.helper.AppFuncs;
import net.wrappy.im.helper.layout.AppFonts;
import net.wrappy.im.helper.layout.AppTextView;

import butterknife.ButterKnife;

/**
 * Created by n8fr8 on 5/7/16.
 */
public class BaseActivity extends AppCompatActivity {

    public static final int REQUEST_PERMISSION_CAMERA_AVATAR = 501;
    public static final int REQUEST_PERMISSION_CAMERA_BANNER = 502;
    public static final int RESULT_AVATAR = 503;
    public static final int RESULT_BANNER = 504;
    public static final int AVATAR = 505;
    public static final int BANNER = 506;
    public static final int REQUEST_PERMISSION_PICKER_AVATAR = 507;
    public static final int REQUEST_PERMISSION_PICKER_BANNER = 508;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

        if (settings.getBoolean("prefBlockScreenshots", false))
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                    WindowManager.LayoutParams.FLAG_SECURE);

        //not set color
        int themeColorHeader = settings.getInt("themeColor", -1);
        int themeColorBg = settings.getInt("themeColorBg", -1);

        if (themeColorHeader != -1) {
            if (Build.VERSION.SDK_INT >= 21) {
                getWindow().setNavigationBarColor(themeColorHeader);
                getWindow().setStatusBarColor(themeColorHeader);
            }

            if (getSupportActionBar() != null)
                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(themeColorHeader));
        }


        if (themeColorBg != -1) {
            getWindow().getDecorView().setBackgroundColor(themeColorBg);
        }
    }

    public void initActionBarDefault(boolean isShowBackButton, int resStringId) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.actionbar_register);
            ImageView backButton = (ImageView) findViewById(getResources().getIdentifier("action_bar_arrow_back", "id", getPackageName()));

            if (isShowBackButton) {
                backButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onClickActionBar(-1);
                    }
                });
            } else {
                backButton.setVisibility(View.GONE);
            }

            if (resStringId != 0) {
                AppTextView txt = (AppTextView) findViewById(getResources().getIdentifier("action_bar_title", "id", getPackageName()));
                txt.setText(getString(resStringId));
            }
        }
    }

    public void addIconActionBar(final int resId) {
        LinearLayout linearLayout = (LinearLayout) findViewById(getResources().getIdentifier("actionBarContainer", "id", getPackageName()));
        ImageView view = new ImageView(this);
        int pad = (int) AppFuncs.convertDpToPixel(6, this);
        view.setPadding(pad, pad, pad, pad);
        view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickActionBar(resId);
            }
        });
        view.setImageResource(resId);
        linearLayout.addView(view);
    }

    public void clearViewInActionBar() {
        LinearLayout linearLayout = (LinearLayout) findViewById(getResources().getIdentifier("actionBarContainer", "id", getPackageName()));
        linearLayout.removeAllViews();

        FrameLayout frameLayout = (FrameLayout) findViewById(getResources().getIdentifier("frActionBarContainer", "id", getPackageName()));
        frameLayout.removeAllViews();
    }

    public void clearViewLeftInActionBar() {
        FrameLayout frameLayout = (FrameLayout) findViewById(getResources().getIdentifier("frActionBarContainer", "id", getPackageName()));
        frameLayout.removeAllViews();
    }

    public void clearViewRightInActionBar() {
        FrameLayout frameLayout = (FrameLayout) findViewById(getResources().getIdentifier("actionBarContainer", "id", getPackageName()));
        frameLayout.removeAllViews();
    }

    public void addCustomViewToActionBar(View view) {
        FrameLayout frameLayout = (FrameLayout) findViewById(getResources().getIdentifier("frActionBarContainer", "id", getPackageName()));
        frameLayout.addView(view);
    }

    public void setTitle(int resStringId) {
        if (resStringId != 0) {
            AppTextView txt = (AppTextView) findViewById(getResources().getIdentifier("action_bar_title", "id", getPackageName()));
            if (txt != null) {
                txt.setText(getString(resStringId));
            }
        }
    }

    public void onClickActionBar(int resId) {
        if (resId == -1) {
            onBackPressed();
        }
    }

    public void applyStyleForToolbar() {

        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        int themeColorHeader = settings.getInt("themeColor", -1);
        int themeColorText = settings.getInt("themeColorText", -1);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        //first set font
        Typeface typeface = CustomTypefaceManager.getCurrentTypeface(this);

        if (typeface != null) {
            for (int i = 0; i < toolbar.getChildCount(); i++) {
                View view = toolbar.getChildAt(i);
                if (view instanceof TextView) {
                    TextView tv = (TextView) view;

                    tv.setTypeface(typeface);
                    break;
                }
            }
        }

        if (themeColorHeader != -1) {
            toolbar.setBackgroundColor(themeColorHeader);
            toolbar.setTitleTextColor(themeColorText);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case UCrop.RESULT_ERROR:
                    final Throwable cropError = UCrop.getError(data);
                    AppFuncs.log(cropError.getLocalizedMessage());
                    onResultPickerImage(false, data, false);
                    break;
                case RESULT_AVATAR:
                    AppFuncs.cropImage(this, data, true, AVATAR);
                    break;
                case RESULT_BANNER:
                    AppFuncs.cropImage(this, data, false, BANNER);
                    break;
                case AVATAR:
                    onResultPickerImage(true, data, true);
                    break;
                case BANNER:
                    onResultPickerImage(false, data, true);
                    break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSION_CAMERA_AVATAR:
                AppFuncs.openCamera(this, true);
                break;
            case REQUEST_PERMISSION_CAMERA_BANNER:
                AppFuncs.openCamera(this, false);
                break;
            case REQUEST_PERMISSION_PICKER_AVATAR:
                AppFuncs.openGallery(this, true);
                break;
            case REQUEST_PERMISSION_PICKER_BANNER:
                AppFuncs.openGallery(this, false);
                break;
        }
    }

    public void onResultPickerImage(boolean isAvatar, Intent data, boolean isSuccess) {
    }

    public void showHidePassword(final EditText editText, final boolean isLogin) {
        editText.setOnTouchListener(new View.OnTouchListener() {
            boolean isVisible = false;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {

                    if (motionEvent.getRawX() >= (editText.getWidth() - editText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width() - editText.getPaddingRight())) {

                        if (!isVisible) {
                            editText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                            editText.setSelection(editText.length());

                            if (isLogin) {
                                editText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock_1, 0, R.drawable.ic_show, 0);
                            } else {
                                editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_show, 0);
                            }

                            isVisible = true;
                        } else {
                            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                            editText.setSelection(editText.length());

                            if (isLogin) {
                                editText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock_1, 0, R.drawable.ic_hidden, 0);
                            } else {
                                editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_hidden, 0);
                            }

                            Typeface myTypeface = Typeface.createFromAsset(getApplicationContext().getAssets(), AppFonts.FONT_REGULAR);
                            editText.setTypeface(myTypeface);

                            isVisible = false;
                        }
                    }
                }
                return false;
            }
        });
    }


}
