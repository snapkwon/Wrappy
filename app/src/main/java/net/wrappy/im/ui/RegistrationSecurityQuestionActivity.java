package net.wrappy.im.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.wrappy.im.R;
import net.wrappy.im.helper.AppDelegate;
import net.wrappy.im.model.SecurityQuestions;
import net.wrappy.im.model.WpKAuthDto;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ben on 13/11/2017.
 */

public class RegistrationSecurityQuestionActivity extends BaseActivity implements AppDelegate {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration_activity_security_question);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_ab_arrow_back);
        getSupportActionBar().setTitle(R.string.registration);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getFragmentManager().beginTransaction().replace(R.id.frRegistrationSecurityQuestion,SecurityQuestionCreateFragment.newsIntance()).commit();
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==android.R.id.home) {
            LauncherActivity.start(RegistrationSecurityQuestionActivity.this);
            finish();
        }
        return true;
    }

    @Override
    public void onChangeInApp(int id, String data) {
        try {
            if (id == AppDelegate.ACTION_FROM_CREATE_NEW && !data.isEmpty()) {
                Gson gson = new Gson();
                ArrayList<SecurityQuestions> securityQuestions = gson.fromJson(data, new TypeToken<List<SecurityQuestions>>(){}.getType());
                WpKAuthDto wpKAuthDto = getIntent().getParcelableExtra(WpKAuthDto.class.getName());

                Intent intent = new Intent(RegistrationSecurityQuestionActivity.this,UpdateProfileActivity.class);
                intent.putExtra(WpKAuthDto.class.getName(),wpKAuthDto);
                intent.putParcelableArrayListExtra(SecurityQuestions.class.getName(),securityQuestions);
                startActivity(intent);
            }
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
