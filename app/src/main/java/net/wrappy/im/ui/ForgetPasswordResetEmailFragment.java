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
import net.wrappy.im.helper.AppFuncs;
import net.wrappy.im.helper.RestAPI;
import net.wrappy.im.helper.layout.AppEditTextView;
import net.wrappy.im.provider.Store;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by ben on 07/12/2017.
 */

public class ForgetPasswordResetEmailFragment extends Fragment{

    View mainView;

    AppDelegate appDelegate;
    AppFuncs appFuncs;

    @BindView(R.id.edForgetPasswordEmail) AppEditTextView edForgetPasswordEmail;

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
        appFuncs = AppFuncs.getInstance();
        return mainView;
    }

    @OnClick(R.id.btnForgetPasswordResetEmail)
    public void onClick(View v) {
        String email = edForgetPasswordEmail.getText().toString().trim();
        if (email.isEmpty()) {
            AppFuncs.alert(getActivity(),"Email is empty", true);
            return;
        }
        if (!AppFuncs.isEmailValid(email)) {
            AppFuncs.alert(getActivity(),"Invalid email format",true);
            return;
        }
        appFuncs.showProgressWaiting(getActivity());
        RestAPI.GetDataWrappy(getActivity(), RestAPI.sendEmailAndUsernameToGetPassUrl(Store.getStringData(getActivity(),Store.USERNAME),email), new RestAPI.RestAPIListenner() {
            @Override
            public void OnComplete(int httpCode, String error, String s) {
                appFuncs.dismissProgressWaiting();
                if (RestAPI.checkHttpCode(httpCode)) {
                    AppFuncs.alert(getActivity(),"Send successful! Please check email to change new password", true);
                    getActivity().finish();
                } else {
                    AppFuncs.alert(getActivity(),"The username or email is incorrect", true);
                }

            }
        });
        //appDelegate.onChangeInApp(ForgetPasswordActivity.ACTION_FROM_RESET_EMAIL,"");
    }
}
