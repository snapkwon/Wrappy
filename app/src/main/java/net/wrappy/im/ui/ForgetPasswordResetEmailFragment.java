package net.wrappy.im.ui;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.wrappy.im.R;
import net.wrappy.im.helper.AppDelegate;
import net.wrappy.im.helper.AppFuncs;
import net.wrappy.im.helper.RestAPI;
import net.wrappy.im.helper.RestAPIListener;
import net.wrappy.im.helper.layout.AppEditTextView;
import net.wrappy.im.provider.Store;
import net.wrappy.im.util.PopupUtils;
import net.wrappy.im.util.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by ben on 07/12/2017.
 */

public class ForgetPasswordResetEmailFragment extends Fragment {

    View mainView;

    AppDelegate appDelegate;

    @BindView(R.id.edForgetPasswordEmail)
    AppEditTextView edForgetPasswordEmail;

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
        mainView = inflater.inflate(R.layout.forget_password_reset_email_fragment, null);
        ButterKnife.bind(this, mainView);
        return mainView;
    }

    @OnClick(R.id.btnForgetPasswordResetEmail)
    public void onClick(View v) {
        String email = edForgetPasswordEmail.getText().toString().trim();
        String error = Utils.isValidEmail(getActivity(), email);
        if (!TextUtils.isEmpty(error)) {
            PopupUtils.showOKDialog(getActivity(), getString(R.string.error), error);
            return;
        }
        AppFuncs.showProgressWaiting(getActivity());
        RestAPI.GetDataWrappy(getActivity(), RestAPI.sendEmailAndUsernameToGetPassUrl(Store.getStringData(getActivity(), Store.USERNAME), email), new RestAPIListener(getActivity()) {
            @Override
            public void OnComplete(int httpCode, String error, String s) {
                AppFuncs.dismissProgressWaiting();
                AppFuncs.alert(getActivity(), getString(R.string.request_send_email_success), true);
                getActivity().finish();
            }
        });
        //appDelegate.onChangeInApp(ForgetPasswordActivity.ACTION_FROM_RESET_EMAIL,"");
    }
}
