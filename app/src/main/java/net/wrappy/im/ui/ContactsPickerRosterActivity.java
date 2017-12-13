package net.wrappy.im.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import net.wrappy.im.R;
import net.wrappy.im.helper.layout.AppTextView;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by ben on 11/12/2017.
 */

public class ContactsPickerRosterActivity extends BaseActivity {

    @BindView(R.id.headerbarTitleLeft) AppTextView headerbarTitleLeft;

    public static void start(Activity activity) {
        Intent intent = new Intent(activity,ContactsPickerRosterActivity.class);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.contacts_picker_roster_activity);
        super.onCreate(savedInstanceState);
        initView();
    }

    private void initView() {
        headerbarTitleLeft.setText("List");
    }

    @OnClick({R.id.headerbarBack,R.id.btnRosterAdd})
    public void onClick(View v) {
        if (v.getId()==R.id.headerbarBack) {
            finish();
        } else if (v.getId() == R.id.btnRosterAdd) {
            ContactsPickerRosterCreateActivity.start(this);
        }
    }
}
