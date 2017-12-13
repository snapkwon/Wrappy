package net.wrappy.im.ui;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.wrappy.im.R;
import net.wrappy.im.helper.AppDelegate;
import net.wrappy.im.helper.AppFuncs;
import net.wrappy.im.helper.RestAPI;
import net.wrappy.im.helper.layout.AppEditTextView;
import net.wrappy.im.helper.layout.AppTextView;
import net.wrappy.im.model.WpKMemberSecurityQuestionDto;
import net.wrappy.im.provider.Store;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static net.wrappy.im.ui.ForgetPasswordActivity.ACTION_FROM_QUESTION;

/**
 * Created by ben on 07/12/2017.
 */

public class ForgetPasswordQuestionFragment extends Fragment {

    View mainView;
    @BindView(R.id.txtSecurityForgetQuestion01) AppTextView txtSecurityForgetQuestion01;
    @BindView(R.id.txtSecurityForgetQuestion02) AppTextView txtSecurityForgetQuestion02;
    @BindView(R.id.edSecurityForgetQuestion01) AppEditTextView edSecurityForgetQuestion01;
    @BindView(R.id.edSecurityForgetQuestion02) AppEditTextView edSecurityForgetQuestion02;

    AppDelegate appDelegate;
    ArrayList<WpKMemberSecurityQuestionDto> stringQuestions = new ArrayList<>();

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        appDelegate = (AppDelegate) activity;
    }

    AppFuncs appFuncs;

    public static ForgetPasswordQuestionFragment newInstance() {
        return new ForgetPasswordQuestionFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.forget_password_question_fragment,null);
        ButterKnife.bind(this, mainView);
        appFuncs = AppFuncs.getInstance();
        getListQuestion();
        return mainView;
    }

    private void getListQuestion() {
        appFuncs.showProgressWaiting(getActivity());
        RestAPI.GetDataWrappy(getActivity(), RestAPI.GET_RANDOM_2_QUESTIONS+ Store.getStringData(getActivity(),Store.USERNAME) , new RestAPI.RestAPIListenner() {



            @Override
            public void OnComplete(int httpCode, String error, String s) {
                try {
                    appFuncs.dismissProgressWaiting();
                    if (!RestAPI.checkHttpCode(httpCode)) {
                        AppFuncs.alert(getActivity(),"Check internet connection",true);
                        getActivity().finish();
                        return;
                    }

                    JsonArray jsonArray = (new JsonParser()).parse(s).getAsJsonArray();

                    for (JsonElement jsonElement : jsonArray) {
                        try {
                            JsonObject jsonObject = jsonElement.getAsJsonObject();
                            String question = jsonObject.get("question").getAsString();
                            TAG(question);
                            int index = jsonObject.get("index").getAsInt();
                            TAG(String.valueOf(index));
                            WpKMemberSecurityQuestionDto questionDto = new WpKMemberSecurityQuestionDto();
                            questionDto.setQuestion(question);
                            questionDto.setIndex(index);
                            stringQuestions.add(questionDto);

                        }catch (Exception ex){
                            ex.printStackTrace();
                        }
                    }
                    txtSecurityForgetQuestion01.setText("1. " + stringQuestions.get(0).getQuestion());
                    txtSecurityForgetQuestion02.setText("2. " + stringQuestions.get(1).getQuestion());
                }catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

    }

    @OnClick(R.id.btnForgetPasswordQuestion)
    public void onClick(View v) {
        String answer01 = edSecurityForgetQuestion01.getText().toString().trim();
        String answer02 = edSecurityForgetQuestion02.getText().toString().trim();
        String error = "";
        if (answer01.isEmpty()) {
            error = "Answer of question 01 is empty";
        } else if (answer02.isEmpty()) {
            error = "Answer of question 02 is empty";
        } else {
            postResultToServer(answer01,answer02);
        }
        if (!error.isEmpty()) {
            AppFuncs.alert(getActivity(),error,true);
        }
    }

    private void postResultToServer(String an1,String an2) {
        String url = RestAPI.getHashStringResetPassUrl(Store.getStringData(getActivity(),Store.USERNAME),an1,an2,"ada");
        RestAPI.GetDataWrappy(getActivity(), url, new RestAPI.RestAPIListenner() {
            @Override
            public void OnComplete(int httpCode, String error, String s) {
                if (RestAPI.checkHttpCode(httpCode)) {
                    if (!s.isEmpty()) {
                        appDelegate.onChangeInApp(ACTION_FROM_QUESTION,s);
                    }
                } else {
                    AppFuncs.alert(getActivity(), "Answer wrong! Try with email", true);
                    appDelegate.onChangeInApp(ACTION_FROM_QUESTION,"");
                }

            }
        });
    }

    private void TAG(String s) {
        Log.i("LTH",s);
    }

}
