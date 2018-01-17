package net.wrappy.im.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import net.wrappy.im.R;
import net.wrappy.im.helper.AppDelegate;
import net.wrappy.im.util.PopupUtils;

/**
 * Created by ben on 02/01/2018.
 */

public class VerifyEmailOrPhoneActivity extends BaseActivity implements AppDelegate {

    public static void start(Activity activity, Bundle bundle, int requestCode) {
        Intent intent = new Intent(activity, VerifyEmailOrPhoneActivity.class);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.verify_email_or_phone_activity);
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_ab_arrow_back);
        getSupportActionBar().setCustomView(R.layout.actionbar_verification);
        ((TextView) getSupportActionBar().getCustomView().findViewById(R.id.actionbar_title)).setText(getString(R.string.page_verify));
        ((ImageButton) getSupportActionBar().getCustomView().findViewById(R.id.verify_actionBarHelp)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (getIntent().getExtras() != null) {
            getFragmentManager().beginTransaction().replace(R.id.frVerifyContainer, VerifyCodeFragment.newInstance(getIntent().getExtras())).commit();
        }
    }


    @Override
    public void onChangeInApp(int id, String data) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        PopupUtils.showCustomDialog(VerifyEmailOrPhoneActivity.this, getString(R.string.warning), getString(R.string.cancel_and_home), R.string.yes, R.string.cancel, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LauncherActivity.start(VerifyEmailOrPhoneActivity.this);
            }
        },null);
    }
}
