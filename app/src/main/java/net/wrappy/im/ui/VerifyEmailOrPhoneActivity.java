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

    public static int VERIFY_OK = 200;
    public static int VERIFY_ERROR = 201;

    public static void start(Activity activity, Bundle bundle, int requestCode) {
        Intent intent = new Intent(activity,VerifyEmailOrPhoneActivity.class);
        if (bundle!=null) {
            intent.putExtras(bundle);
        }
        activity.startActivityForResult(intent,requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.verify_email_or_phone_activity);
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle(getString(R.string.page_verify));
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (getIntent().getExtras()!=null) {
            getFragmentManager().beginTransaction().replace(R.id.frVerifyContainer, VerifyCodeFragment.newInstance(getIntent().getExtras())).commit();
        }
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
        if (id == VERIFY_OK) {
            resultOK();
        }
    }

    private void resultOK() {
        Intent returnIntent = getIntent();
        returnIntent.putExtra("result",true);
        setResult(RESULT_OK,returnIntent);
        finish();
    }
}
