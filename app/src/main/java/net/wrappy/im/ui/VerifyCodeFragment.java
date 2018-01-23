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

import com.alimuzaffar.lib.pin.PinEntryEditText;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.wrappy.im.R;
import net.wrappy.im.helper.AppDelegate;
import net.wrappy.im.helper.AppFuncs;
import net.wrappy.im.helper.LoginTask;
import net.wrappy.im.helper.RestAPI;
import net.wrappy.im.helper.RestAPIListenner;
import net.wrappy.im.model.Registration;
import net.wrappy.im.model.RegistrationAccount;
import net.wrappy.im.model.WpErrors;
import net.wrappy.im.model.WpkToken;
import net.wrappy.im.provider.Store;
import net.wrappy.im.ui.onboarding.OnboardingAccount;
import net.wrappy.im.util.Constant;

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
    PinEntryEditText txtPin;
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
        verifyCodeFragment.setArguments(bundle);
        return verifyCodeFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.verify_code_fragment, null);
        ButterKnife.bind(this, mainView);
        appFuncs = AppFuncs.getInstance();
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

    @OnClick({R.id.btnVerifyCheck, R.id.btnVerifyChangePhone, R.id.btnSendCodeAgain})
    public void onClick(View v) {
        if (v.getId() == R.id.btnVerifyCheck) {
            String pin = txtPin.getText().toString().trim();
            if (TextUtils.isEmpty(pin)) {
                return;
            }
            appFuncs.showProgressWaiting(getActivity());
            RestAPI.PostDataWrappy(getActivity(), new JsonObject(), RestAPI.getVerifyCodeUrl(registration.getWpKMemberDto().getMobile(), pin), new RestAPIListenner() {
                @Override
                public void OnComplete(int httpCode, String error, String s) {

                    if (RestAPI.checkHttpCode(httpCode)) {
                        String url = RestAPI.loginUrl(registration.getWpKMemberDto().getIdentifier(), registration.getWpKAuthDto().getSecret());
                        AppFuncs.log(url);
                        RestAPI.PostDataWrappy(getActivity(), null, url, new RestAPIListenner() {

                            @Override
                            public void OnComplete(int httpCode, String error, String s) {
                                try {
                                    if (!RestAPI.checkHttpCode(httpCode)) {
                                        String er = WpErrors.getErrorMessage(s);
                                        if (!TextUtils.isEmpty(er)) {
                                            AppFuncs.alert(getActivity(), er, true);
                                        }
                                        appFuncs.dismissProgressWaiting();
                                        return;
                                    }
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
                                            appFuncs.dismissProgressWaiting();
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
                                    appFuncs.dismissProgressWaiting();
                                    ex.printStackTrace();
                                }
                            }
                        });

                        return;
                    } else {
                        appFuncs.dismissProgressWaiting();
                        txtPin.setText("");
                        AppFuncs.alert(getActivity(), getString(R.string.network_error), false);
                    }
                }
            });
        } else if (v.getId() == R.id.btnVerifyChangePhone) {
            try {
                ImageButton btn = (ImageButton) v;
                if (btn.isSelected()) {
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
        RestAPI.PostDataWrappy(getActivity(), new JsonObject(), url, new RestAPIListenner() {
            @Override
            public void OnComplete(int httpCode, String error, String s) {
                if (RestAPI.checkHttpCode(httpCode)) {
                    AppFuncs.alert(getActivity(), getString(R.string.verify_send_sms_success), false);
                } else {
                    AppFuncs.alert(getActivity(), getString(R.string.verify_send_sms_fail), false);
                }
            }
        });
    }

    private void requestChangePhoneNumber() {
        edVerifyPhone.setFocusable(false);
        String newPhone = edVerifyPhone.getText().toString().trim();
        String url = RestAPI.getVerifyCodeByNewPhoneNumber(Store.getStringData(getActivity(), Store.USERNAME), registration.getWpKMemberDto().getMobile(), newPhone);
        registration.getWpKMemberDto().setMobile(newPhone);
        AppFuncs.log("requestChangePhoneNumber" + url);
        RestAPI.PostDataWrappy(getActivity(), new JsonObject(), url, new RestAPIListenner() {
            @Override
            public void OnComplete(int httpCode, String error, String s) {
                if (RestAPI.checkHttpCode(httpCode)) {
                    AppFuncs.alert(getActivity(), getString(R.string.verify_send_sms_success), false);
                } else {
                    AppFuncs.alert(getActivity(), getString(R.string.verify_send_sms_fail), false);
                }
            }
        });
    }
}
