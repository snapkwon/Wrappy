package net.wrappy.im.ui;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alimuzaffar.lib.pin.PinEntryEditText;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Response;

import net.wrappy.im.R;
import net.wrappy.im.helper.AppDelegate;
import net.wrappy.im.helper.RestAPI;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by ben on 07/12/2017.
 */

public class VerifyCodeFragment extends Fragment {

    View mainView;

    AppDelegate appDelegate;
    String phone = "";

    @BindView(R.id.txt_pin_entry)
    PinEntryEditText txtPin;

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
        mainView = inflater.inflate(R.layout.verify_code_fragment,null);
        ButterKnife.bind(this,mainView);
        phone = getArguments().getString("phone");
        return mainView;
    }

    @OnClick(R.id.btnVerifyCheck)
    public void onClick(View v) {
        String pin = txtPin.getText().toString().trim();
        if (TextUtils.isEmpty(pin)) {
            return;
        }

        RestAPI.apiPOST(getActivity(),RestAPI.getVerifyCodeUrl(phone,pin),new JsonObject()).setCallback(new FutureCallback<Response<String>>() {
            @Override
            public void onCompleted(Exception e, Response<String> result) {
                if (result!=null) {
                    if (RestAPI.checkHttpCode(result.getHeaders().code())) {
                        appDelegate.onChangeInApp(VerifyEmailOrPhoneActivity.VERIFY_OK,"");
                        return;
                    }
                }
                appDelegate.onChangeInApp(VerifyEmailOrPhoneActivity.VERIFY_ERROR,"");
            }
        });

    }
}
