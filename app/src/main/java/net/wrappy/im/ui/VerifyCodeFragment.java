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
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Response;

import net.wrappy.im.R;
import net.wrappy.im.helper.AppDelegate;
import net.wrappy.im.helper.AppFuncs;
import net.wrappy.im.helper.RestAPI;
import net.wrappy.im.provider.Store;

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
    String phone = "";

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
        phone = getArguments().getString("phone");
        edVerifyPhone.setText(phone);
        btnVerifyChangePhone.setSelected(false);
        return mainView;
    }

    @OnTextChanged(R.id.edVerifyPhone)
    protected void handleTextChange(Editable editable) {
        String text = editable.toString().trim();
        if (!text.equalsIgnoreCase(phone)) {
            btnVerifyChangePhone.setImageResource(R.drawable.ic_icon_check);
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

            RestAPI.apiPOST(getActivity(), RestAPI.getVerifyCodeUrl(phone, pin), new JsonObject()).setCallback(new FutureCallback<Response<String>>() {
                @Override
                public void onCompleted(Exception e, Response<String> result) {
                    if (result != null) {
                        if (RestAPI.checkHttpCode(result.getHeaders().code())) {
                            appDelegate.onChangeInApp(VerifyEmailOrPhoneActivity.VERIFY_OK, "");
                            return;
                        }
                    }
                    AppFuncs.alert(getActivity(), getString(R.string.verify_fail), false);
                    appDelegate.onChangeInApp(VerifyEmailOrPhoneActivity.VERIFY_ERROR, "");
                }
            });
        } else if (v.getId() == R.id.btnVerifyChangePhone) {
            try {
                ImageButton btn = (ImageButton) v;
                if (btn.isSelected()) {
                    btn.setImageResource(R.drawable.page_1);
                    btnVerifyChangePhone.setSelected(false);
                    sendCodeAgain();
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
        phone = edVerifyPhone.getText().toString().trim();
        String url = RestAPI.getVerifyCodeUrlResend(Store.getStringData(getActivity(), Store.USERNAME), phone);
        AppFuncs.log(url);
        RestAPI.apiPOST(getActivity(), url, new JsonObject()).setCallback(new FutureCallback<Response<String>>() {
            @Override
            public void onCompleted(Exception e, Response<String> result) {
                if (result != null) if (RestAPI.checkHttpCode(result.getHeaders().code())) {
                    AppFuncs.alert(getActivity(), getString(R.string.verify_send_sms_success), false);
                } else {
                    AppFuncs.alert(getActivity(), getString(R.string.verify_send_sms_fail), false);
                }
            }
        });
    }
}
