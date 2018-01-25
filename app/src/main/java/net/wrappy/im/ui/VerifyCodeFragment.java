package net.wrappy.im.ui;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import com.goodiebag.pinview.Pinview;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.wrappy.im.R;
import net.wrappy.im.helper.AppDelegate;
import net.wrappy.im.helper.AppFuncs;
import net.wrappy.im.helper.LoginTask;
import net.wrappy.im.helper.RestAPI;
import net.wrappy.im.helper.RestAPIListener;
import net.wrappy.im.model.Registration;
import net.wrappy.im.model.RegistrationAccount;
import net.wrappy.im.model.WpkToken;
import net.wrappy.im.provider.Store;
import net.wrappy.im.ui.onboarding.OnboardingAccount;
import net.wrappy.im.util.Constant;
import net.wrappy.im.util.PopupUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;

/**
 * Created by ben on 07/12/2017.
 */

public class VerifyCodeFragment extends Fragment {

    View mainView;

    AppDelegate appDelegate;
    AppFuncs appFuncs;
    String data = "";
    Registration registration;

    @BindView(R.id.txt_pin_entry)
    Pinview txtPin;
    @BindView(R.id.edVerifyPhone)
    EditText edVerifyPhone;
    @BindView(R.id.btnVerifyChangePhone)
    ImageButton btnVerifyChangePhone;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        appDelegate = (AppDelegate) activity;
    }

    public static VerifyCodeFragment newInstance(Bundle bundle) {
        VerifyCodeFragment verifyCodeFragment = new VerifyCodeFragment();
        if (bundle!=null) {
            verifyCodeFragment.setArguments(bundle);
        }
        return verifyCodeFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.verify_code_fragment, null);
        ButterKnife.bind(this, mainView);
        appFuncs = AppFuncs.getInstance();
        if (getArguments()!=null)
            data = getArguments().getString("data", "");
        if (!TextUtils.isEmpty(data)) {
            Gson gson = new Gson();
            registration = gson.fromJson(data, Registration.class);
            edVerifyPhone.setText(registration.getWpKMemberDto().getMobile());
        }
        btnVerifyChangePhone.setSelected(false);
        return mainView;
    }

    @OnTextChanged(R.id.edVerifyPhone)
    protected void handleTextChange(Editable editable) {
        String text = editable.toString().trim();
        if (!text.equalsIgnoreCase(registration.getWpKMemberDto().getMobile())) {
            btnVerifyChangePhone.setImageResource(R.drawable.ic_check_active);
            btnVerifyChangePhone.setSelected(true);
        } else {
            btnVerifyChangePhone.setImageResource(R.drawable.page_1);
            btnVerifyChangePhone.setSelected(false);
        }
    }

    private boolean isValidPassCode(String pin) {
        String error = "";
        if (TextUtils.isEmpty(pin)) {
            error = getString(R.string.error_empty_passcode);
        } else if (pin.length() < 5) {
            error = getString(R.string.error_invalid_passcode);
        }
        if (TextUtils.isEmpty(error))
            return true;
        else {
            PopupUtils.showOKDialog(getActivity(), getString(R.string.error), error, null);
            return false;
        }
    }

    private boolean isValidPhoneNumber(String pin) {
        String error = "";
        if (TextUtils.isEmpty(pin)) {
            error = getString(R.string.error_empty_phone);
        }
        if (TextUtils.isEmpty(error))
            return true;
        else {
            PopupUtils.showOKDialog(getActivity(), getString(R.string.error), error, null);
            return false;
        }
    }

    @OnClick({R.id.btnVerifyCheck, R.id.btnVerifyChangePhone, R.id.btnSendCodeAgain})
    public void onClick(View v) {
        if (v.getId() == R.id.btnVerifyCheck) {
            String pin = txtPin.getValue();
            if (!isValidPassCode(pin)) {
                return;
            }
            AppFuncs.showProgressWaiting(getActivity());
            RestAPI.PostDataWrappy(getActivity(), new JsonObject(), RestAPI.getVerifyCodeUrl(registration.getWpKMemberDto().getMobile(), pin), new RestAPIListener(getActivity()) {
                @Override
                public void OnComplete(int httpCode, String error, String s) {
                    String url = RestAPI.loginUrl(registration.getWpKMemberDto().getIdentifier(), registration.getWpKAuthDto().getSecret());
                    AppFuncs.log(url);
                    RestAPIListener listener = new RestAPIListener(getActivity()) {

                        @Override
                        public void OnComplete(int httpCode, String error, String s) {
                            try {
                                AppFuncs.log("loginUrl: " + s);
                                JsonObject jsonObject = (new JsonParser()).parse(s).getAsJsonObject();
                                Gson gson = new Gson();
                                WpkToken wpkToken = gson.fromJson(jsonObject, WpkToken.class);
                                wpkToken.saveToken(getActivity());
                                RegistrationAccount account = new RegistrationAccount(wpkToken.getJid() + Constant.EMAIL_DOMAIN, wpkToken.getXmppPassword());
                                account.setNickname(registration.getWpKMemberDto().getIdentifier());
                                account.setEmail(registration.getWpKMemberDto().getEmail());
                                account.setPhone(registration.getWpKMemberDto().getMobile());
                                account.setGender(registration.getWpKMemberDto().getGender());
                                (new LoginTask(getActivity(), new LoginTask.EventListenner() {
                                    @Override
                                    public void OnComplete(boolean isSuccess, OnboardingAccount onboardingAccount) {
                                        AppFuncs.dismissProgressWaiting();
                                        if (!isSuccess) {
                                            AppFuncs.alert(getActivity(), getString(R.string.network_error), false);
                                        } else {
                                            AppFuncs.getSyncUserInfo(onboardingAccount.accountId);
                                            ReferralActivity.start();
                                            getActivity().finish();
                                        }
                                    }
                                })).execute(account);
                            } catch (Exception ex) {
                                AppFuncs.dismissProgressWaiting();
                                ex.printStackTrace();
                            }
                        }
                    };
                    RestAPI.PostDataWrappy(getActivity(), null, url, listener);
                }
            });
        } else if (v.getId() == R.id.btnVerifyChangePhone) {
            try {
                ImageButton btn = (ImageButton) v;
                if (btn.isSelected()) {
                    if (!isValidPhoneNumber(edVerifyPhone.getText().toString().trim()))
                        return;
                    btn.setImageResource(R.drawable.page_1);
                    btnVerifyChangePhone.setSelected(false);
                    requestChangePhoneNumber();
                } else {
                    edVerifyPhone.setFocusableInTouchMode(true);
                    edVerifyPhone.requestFocus();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        } else if (v.getId() == R.id.btnSendCodeAgain) {
            sendCodeAgain();
        }

    }

    private void sendCodeAgain() {
        edVerifyPhone.setFocusable(false);
        String phone = edVerifyPhone.getText().toString().trim();
        String url = RestAPI.getVerifyCodeUrlResend(Store.getStringData(getActivity(), Store.USERNAME), phone);
        AppFuncs.log("sendCodeAgain: " + url);
        RestAPI.PostDataWrappy(getActivity(), new JsonObject(), url, new RestAPIListener(getActivity()) {
            @Override
            public void OnComplete(int httpCode, String error, String s) {
                AppFuncs.alert(getActivity(), getString(R.string.verify_send_sms_success), false);
            }
        });
    }

    private void requestChangePhoneNumber() {
        edVerifyPhone.setFocusable(false);
        String newPhone = edVerifyPhone.getText().toString().trim();
        String url = RestAPI.getVerifyCodeByNewPhoneNumber(Store.getStringData(getActivity(), Store.USERNAME), registration.getWpKMemberDto().getMobile(), newPhone);
        registration.getWpKMemberDto().setMobile(newPhone);
        AppFuncs.log("requestChangePhoneNumber" + url);
        RestAPI.PostDataWrappy(getActivity(), new JsonObject(), url, new RestAPIListener(getActivity()) {
            @Override
            public void OnComplete(int httpCode, String error, String s) {
                AppFuncs.alert(getActivity(), getString(R.string.verify_send_sms_success), false);
            }
        });
    }
}
