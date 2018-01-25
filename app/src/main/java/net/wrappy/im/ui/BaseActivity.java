package net.wrappy.im.ui;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.ironrabbit.type.CustomTypefaceManager;
import net.wrappy.im.R;
import net.wrappy.im.helper.AppFuncs;
import net.wrappy.im.helper.layout.AppTextView;

import butterknife.ButterKnife;

/**
 * Created by n8fr8 on 5/7/16.
 */
public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

        if (settings.getBoolean("prefBlockScreenshots",false))
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                    WindowManager.LayoutParams.FLAG_SECURE);

        //not set color
        int themeColorHeader = settings.getInt("themeColor",-1);
        int themeColorBg = settings.getInt("themeColorBg",-1);

        if (themeColorHeader != -1) {
            if (Build.VERSION.SDK_INT >= 21) {
                getWindow().setNavigationBarColor(themeColorHeader);
                getWindow().setStatusBarColor(themeColorHeader);
            }

            if (getSupportActionBar() != null)
                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(themeColorHeader));
        }


        if (themeColorBg != -1)
        {
            getWindow().getDecorView().setBackgroundColor(themeColorBg);
        }
    }

    public void initActionBarDefault(boolean isShowBackButton, int resStringId) {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.actionbar_register);
        if (isShowBackButton) {
            ImageView backButton = (ImageView) findViewById(getResources().getIdentifier("action_bar_arrow_back", "id", getPackageName()));
            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onClickActionBar(-1);
                }
            });
        }

        if (resStringId!=0) {
            AppTextView txt = (AppTextView) findViewById(getResources().getIdentifier("action_bar_title", "id", getPackageName()));
            txt.setText(getString(resStringId));
        }
    }

    public void addIconActionBar(final int resId) {
        LinearLayout linearLayout = (LinearLayout) findViewById(getResources().getIdentifier("actionBarContainer", "id", getPackageName()));
        ImageView view = new ImageView(this);
        int pad = (int) AppFuncs.convertDpToPixel(6,this);
        view.setPadding(pad,pad,pad,pad);
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

    public void setTitle(int resStringId) {
        if (resStringId!=0) {
            AppTextView txt = (AppTextView) findViewById(getResources().getIdentifier("action_bar_title", "id", getPackageName()));
            if (txt!=null) {
                txt.setText(getString(resStringId));
            }
        }
    }

    public void onClickActionBar(int resId) {}

    public void applyStyleForToolbar() {

        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        int themeColorHeader = settings.getInt("themeColor",-1);
        int themeColorText = settings.getInt("themeColorText",-1);

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



}
