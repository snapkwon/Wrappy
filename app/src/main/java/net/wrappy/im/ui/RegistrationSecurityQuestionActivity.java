package net.wrappy.im.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.wrappy.im.R;
import net.wrappy.im.helper.AppDelegate;
import net.wrappy.im.model.SecurityQuestions;
import net.wrappy.im.model.WpKAuthDto;
import net.wrappy.im.util.PopupUtils;

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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.actionbar_register);

        TextView title = (TextView) findViewById(getResources().getIdentifier("action_bar_title", "id", getPackageName()));
        title.setText(R.string.registration);

        ImageView backButton = (ImageView) findViewById(getResources().getIdentifier("action_bar_arrow_back", "id", getPackageName()));
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        getFragmentManager().beginTransaction().replace(R.id.frRegistrationSecurityQuestion,SecurityQuestionCreateFragment.newsIntance(0)).commit();
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==android.R.id.home) {
            onBackPressed();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        PopupUtils.showCustomDialog(this, getString(R.string.warning), getString(R.string.cancel_and_home), R.string.yes, R.string.cancel, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LauncherActivity.start(RegistrationSecurityQuestionActivity.this);
            }
        },null);
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
