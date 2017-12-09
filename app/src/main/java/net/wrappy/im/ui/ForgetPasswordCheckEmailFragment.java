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

import butterknife.OnClick;

/**
 * Created by ben on 07/12/2017.
 */

public class ForgetPasswordCheckEmailFragment extends Fragment {

    View mainView;

    AppDelegate appDelegate;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        appDelegate = (AppDelegate) activity;
    }

    public static ForgetPasswordCheckEmailFragment newInstance() {
        return new ForgetPasswordCheckEmailFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.forget_password_check_email_fragment,null);
        return mainView;
    }

    @OnClick(R.id.btnForgetPasswordCheckEmail)
    public void onClick(View v) {
        appDelegate.onChangeInApp(ForgetPasswordActivity.ACTION_FROM_CHECK_EMAIL,"");
    }
}
