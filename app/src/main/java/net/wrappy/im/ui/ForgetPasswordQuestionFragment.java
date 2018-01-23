package net.wrappy.im.ui;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
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
import net.wrappy.im.helper.RestAPIListener;
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

    private static String TYPE = "typeofsecu";

    View mainView;
    @BindView(R.id.txtSecurityForgetQuestion01)
    AppTextView txtSecurityForgetQuestion01;
    @BindView(R.id.txtSecurityForgetQuestion02)
    AppTextView txtSecurityForgetQuestion02;
    @BindView(R.id.edSecurityForgetQuestion01)
    AppEditTextView edSecurityForgetQuestion01;
    @BindView(R.id.edSecurityForgetQuestion02)
    AppEditTextView edSecurityForgetQuestion02;
    @BindView(R.id.txtSecurityQuestionTitle)
    AppTextView txtSecurityQuestionTitle;

    AppDelegate appDelegate;
    ArrayList<WpKMemberSecurityQuestionDto> stringQuestions = new ArrayList<>();

    int count = 0;
    int type = 0;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        appDelegate = (AppDelegate) activity;
    }

    AppFuncs appFuncs;

    public static ForgetPasswordQuestionFragment newInstance(int type) {
        ForgetPasswordQuestionFragment fragment = new ForgetPasswordQuestionFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(TYPE, type);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.forget_password_question_fragment, null);
        ButterKnife.bind(this, mainView);
        appFuncs = AppFuncs.getInstance();
        type = getArguments().getInt(TYPE, 0);
        if (type == 1) {
            txtSecurityQuestionTitle.setText(getString(R.string.security_change_old_question));
        }
        getListQuestion();
        return mainView;
    }

    private void getListQuestion() {
        AppFuncs.showProgressWaiting(getActivity());
        RestAPI.GetDataWrappy(getActivity(), RestAPI.GET_RANDOM_2_QUESTIONS + Store.getStringData(getActivity(), Store.USERNAME), new RestAPIListener(getActivity()) {
            @Override
            public void OnComplete(int httpCode, String error, String s) {
                try {
                    AppFuncs.dismissProgressWaiting();

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

                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                    txtSecurityForgetQuestion01.setText("1. " + stringQuestions.get(0).getQuestion());
                    txtSecurityForgetQuestion02.setText("2. " + stringQuestions.get(1).getQuestion());
                } catch (Exception ex) {
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
            error = String.format(getString(R.string.error_empty_answer), 1);
        } else if (answer02.isEmpty()) {
            error = String.format(getString(R.string.error_empty_answer), 2);
        } else {
            if (stringQuestions != null && stringQuestions.size() > 1) {
                stringQuestions.get(0).setAnswer(answer01);
                stringQuestions.get(1).setAnswer(answer02);
                postResultToServer();
            }
        }
        if (!error.isEmpty()) {
            AppFuncs.alert(getActivity(), error, true);
        }
    }

    private void postResultToServer() {
        JsonObject jsonObject = new JsonObject();
        try {
            jsonObject.add("kMemberSecurityQuestionDtoList", AppFuncs.convertToJson(stringQuestions).getAsJsonArray());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (type == 0) {
            postDataForForgetPassword(jsonObject);
        } else {
            postDataForChangeSecurityQuestions(jsonObject);
        }


    }

    int time = 0;

    private void postDataForForgetPassword(JsonObject json) {
        RestAPI.PostDataWrappy(getActivity(), json, RestAPI.getCheckForgetPasswordSecurityQuestionsUrl(Store.getStringData(getActivity(), Store.USERNAME)), new RestAPIListener() {
            @Override
            public void OnComplete(int httpCode, String error, String s) {
                if (!TextUtils.isEmpty(s))
                    appDelegate.onChangeInApp(ACTION_FROM_QUESTION, s);
            }

            @Override
            protected void onError(int errorCode) {
                if (time > 2) {
                    AppFuncs.alert(getActivity(), getString(R.string.error_wrong_answer_2nd), true);
                    appDelegate.onChangeInApp(ACTION_FROM_QUESTION, "");
                } else {
                    AppFuncs.alert(getActivity(), getString(R.string.error_wrong_answer_1st), true);
                }
                time++;
            }
        });
    }

    private void postDataForChangeSecurityQuestions(JsonObject json) {
        RestAPI.PostDataWrappy(getActivity(), json, RestAPI.POST_CHANGE_QUESTION_CHECK, new RestAPIListener(getActivity()) {
            @Override
            public void OnComplete(int httpCode, String error, String s) {
                if (!TextUtils.isEmpty(s))
                    appDelegate.onChangeInApp(ACTION_FROM_QUESTION, s);
            }

            @Override
            protected void onError(int errorCode) {
                AppFuncs.alert(getActivity(), getString(R.string.error_wrong_answer_1st), true);
            }
        });
    }

    private void TAG(String s) {
        Log.i("LTH", s);
    }

}
