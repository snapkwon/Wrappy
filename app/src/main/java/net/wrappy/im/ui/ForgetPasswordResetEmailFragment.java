package net.wrappy.im.ui;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.wrappy.im.R;
import net.wrappy.im.helper.AppDelegate;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by ben on 07/12/2017.
 */

public class ForgetPasswordResetEmailFragment extends Fragment{

    View mainView;

    AppDelegate appDelegate;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        appDelegate = (AppDelegate) activity;
    }

    public static ForgetPasswordResetEmailFragment newInstance() {
        return new ForgetPasswordResetEmailFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.forget_password_reset_email_fragment,null);
        ButterKnife.bind(this,mainView);
        return mainView;
    }

    @OnClick(R.id.btnForgetPasswordResetEmail)
    public void onClick(View v) {
        appDelegate.onChangeInApp(ForgetPasswordActivity.ACTION_FROM_RESET_EMAIL,"");
    }
}
