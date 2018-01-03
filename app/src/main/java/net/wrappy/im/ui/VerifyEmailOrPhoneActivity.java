package net.wrappy.im.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import net.wrappy.im.R;
import net.wrappy.im.helper.AppDelegate;

/**
 * Created by ben on 02/01/2018.
 */

public class VerifyEmailOrPhoneActivity extends BaseActivity implements AppDelegate {

    public static void start(Activity activity, Bundle bundle) {
        Intent intent = new Intent(activity,VerifyEmailOrPhoneActivity.class);
        if (bundle!=null) {
            intent.putExtras(bundle);
        }
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.verify_email_or_phone_activity);
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle(getString(R.string.page_verify));
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getFragmentManager().beginTransaction().replace(R.id.frVerifyContainer, ForgetPasswordCheckEmailFragment.newInstance()).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return true;
    }

    @Override
    public void onChangeInApp(int id, String data) {

    }
}
