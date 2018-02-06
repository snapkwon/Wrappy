package net.wrappy.im.ui;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatSpinner;
import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.goodiebag.pinview.Pinview;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import net.wrappy.im.MainActivity;
import net.wrappy.im.R;
import net.wrappy.im.helper.AppDelegate;
import net.wrappy.im.helper.AppFuncs;
import net.wrappy.im.helper.LoginTask;
import net.wrappy.im.helper.RestAPI;
import net.wrappy.im.helper.RestAPIListener;
import net.wrappy.im.helper.layout.AppTextView;
import net.wrappy.im.model.Registration;
import net.wrappy.im.model.RegistrationAccount;
import net.wrappy.im.model.WpkCountry;
import net.wrappy.im.model.WpkToken;
import net.wrappy.im.provider.Store;
import net.wrappy.im.ui.onboarding.OnboardingAccount;
import net.wrappy.im.util.Constant;
import net.wrappy.im.util.PopupUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import butterknife.OnTouch;

/**
 * Created by ben on 07/12/2017.
 */

public class VerifyCodeFragment extends Fragment {

    View mainView;

    AppDelegate appDelegate;
    AppFuncs appFuncs;
    String data = "";
    String countryCode = "";
    Registration registration;
    List<WpkCountry> wpkCountry;
    ArrayAdapter countryAdapter;
    String locale = "";
    String phone = "";

    @BindView(R.id.txt_pin_entry)
    Pinview txtPin;
    @BindView(R.id.edVerifyPhone)
    EditText edVerifyPhone;
    @BindView(R.id.btnVerifyChangePhone)
    ImageButton btnVerifyChangePhone;
    @BindView(R.id.lnVerifyContainer)
    LinearLayout lnVerifyContainer;
    @BindView(R.id.spnProfileCountryCodes)
    AppCompatSpinner spnProfileCountryCodes;
    @BindView(R.id.btnSendCodeAgain) AppTextView btnSendCodeAgain;

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
        if (getArguments()!=null) {
            data = getArguments().getString("data", "");
            countryCode = getArguments().getString("country", "");
        }
        if (!TextUtils.isEmpty(data)) {
            Gson gson = new Gson();
            registration = gson.fromJson(data, Registration.class);
            String phone = registration.getWpKMemberDto().getMobile();
            phone = phone.substring(countryCode.length(),phone.length());
            edVerifyPhone.setText(phone);
        }

        btnVerifyChangePhone.setSelected(false);
        getCountryCodesFromServer();
        spnProfileCountryCodes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (!countryCode.equalsIgnoreCase(wpkCountry.get(i).getPrefix())) {
                    btnVerifyChangePhone.setImageResource(R.drawable.ic_check_active);
                    btnVerifyChangePhone.setSelected(true);
                } else {
                    btnVerifyChangePhone.setImageResource(R.drawable.page_1);
                    btnVerifyChangePhone.setSelected(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        AppFuncs.dismissKeyboard(getActivity());
        return mainView;
    }

    @OnTextChanged(R.id.edVerifyPhone)
    protected void handleTextChange(Editable editable) {
        String text = editable.toString().trim();
        if (!text.equalsIgnoreCase(phone)) {
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

    private void getCountryCodesFromServer() {
        RestAPI.GetDataWrappy(getActivity(), RestAPI.GET_COUNTRY_CODES, new RestAPIListener(getActivity()) {
            @Override
            public void OnComplete(String s) {
                try {
                    if (s != null) {
                        Type listType = new TypeToken<ArrayList<WpkCountry>>() {
                        }.getType();
                        wpkCountry = new Gson().fromJson(s, listType);
                        wpkCountry.get(0).getCode();
                        List<String> strings = new ArrayList<>();
                        int j = 0;
                        for (int i = 0; i < wpkCountry.size(); i++) {
                            if (wpkCountry.get(i).getPrefix().toUpperCase().equalsIgnoreCase(countryCode.toUpperCase())) {
                                j = i;
                            }
                            strings.add(wpkCountry.get(i).getL10N().get(WpkCountry.country_en_US) + " " + wpkCountry.get(i).getPrefix());
                        }
                        countryAdapter = new ArrayAdapter<String>(getActivity(), R.layout.update_profile_textview, strings) {
                            @NonNull
                            @Override
                            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                                LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                final View v = vi.inflate(android.R.layout.simple_spinner_item, null);
                                final TextView t = (TextView) v.findViewById(android.R.id.text1);
                                t.setText(wpkCountry.get(position).getPrefix());
                                return v;
                            }
                        };
                        spnProfileCountryCodes.setAdapter(countryAdapter);
                        spnProfileCountryCodes.setSelection(j);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
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

    @OnTouch(R.id.btnSendCodeAgain)
    boolean exampleTouched(View v, MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            btnSendCodeAgain.setAlpha(0.5f);
            return true;
        } else if (ev.getAction() == MotionEvent.ACTION_CANCEL){
            btnSendCodeAgain.setAlpha(1f);
            return true;
        }
        return false;
    }

    @OnClick({R.id.btnVerifyCheck, R.id.btnVerifyChangePhone, R.id.btnSendCodeAgain, R.id.lnVerifyContainer})
    public void onClick(View v) {
        if (v.getId() == R.id.btnVerifyCheck) {
            String pin = txtPin.getValue();
            if (!isValidPassCode(pin)) {
                return;
            }
            AppFuncs.showProgressWaiting(getActivity());
            RestAPI.PostDataWrappy(getActivity(), new JsonObject(), RestAPI.getVerifyCodeUrl(registration.getWpKMemberDto().getMobile(), pin), new RestAPIListener(getActivity()) {
                @Override
                public void OnComplete(String s) {
                    String url = RestAPI.loginUrl(registration.getWpKMemberDto().getIdentifier(), registration.getWpKAuthDto().getSecret());
                    AppFuncs.log(url);
                    RestAPIListener listener = new RestAPIListener(getActivity()) {

                        @Override
                        public void OnComplete(String s) {
                            try {
                                AppFuncs.log("loginUrl: " + s);
                                JsonObject jsonObject = (new JsonParser()).parse(s).getAsJsonObject();
                                Gson gson = new Gson();
                                WpkToken wpkToken = gson.fromJson(jsonObject, WpkToken.class);
                                wpkToken.saveToken(getActivity());
                                RegistrationAccount account = new RegistrationAccount(wpkToken.getJid() + Constant.EMAIL_DOMAIN, wpkToken.getXmppPassword());
                                account.setNickname(registration.getWpKMemberDto().getGiven());
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
                                            Store.putBooleanData(getActivity(),Store.REFERRAL,true);
                                            MainActivity.start();
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
        } else if (v.getId() == R.id.lnVerifyContainer) {
            AppFuncs.dismissKeyboard(getActivity());
        }

    }

    private void sendCodeAgain() {
        edVerifyPhone.setFocusable(false);
        String phone =  wpkCountry.get(spnProfileCountryCodes.getSelectedItemPosition()).getPrefix() + edVerifyPhone.getText().toString().trim();
        String url = RestAPI.getVerifyCodeUrlResend(Store.getStringData(getActivity(), Store.USERNAME), phone);
        AppFuncs.log("sendCodeAgain: " + url);
        RestAPI.PostDataWrappy(getActivity(), new JsonObject(), url, new RestAPIListener(getActivity()) {
            @Override
            public void OnComplete(String s) {
                AppFuncs.alert(getActivity(), getString(R.string.verify_send_sms_success), false);
            }
        });
    }

    private void requestChangePhoneNumber() {
        edVerifyPhone.setFocusable(false);
        String newPhone = wpkCountry.get(spnProfileCountryCodes.getSelectedItemPosition()).getPrefix() + edVerifyPhone.getText().toString().trim();
        String url = RestAPI.getVerifyCodeByNewPhoneNumber(Store.getStringData(getActivity(), Store.USERNAME), registration.getWpKMemberDto().getMobile(), newPhone);
        registration.getWpKMemberDto().setMobile(newPhone);
        AppFuncs.log("requestChangePhoneNumber" + url);
        RestAPI.PostDataWrappy(getActivity(), new JsonObject(), url, new RestAPIListener(getActivity()) {
            @Override
            public void OnComplete(String s) {
                btnVerifyChangePhone.setImageResource(R.drawable.page_1);
                btnVerifyChangePhone.setSelected(false);
                AppFuncs.alert(getActivity(), getString(R.string.verify_send_sms_success), false);
            }
        });
    }
}
