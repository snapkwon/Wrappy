package net.wrappy.im.ui;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.wrappy.im.R;
import net.wrappy.im.helper.AppDelegate;
import net.wrappy.im.helper.AppFuncs;
import net.wrappy.im.helper.RestAPI;
import net.wrappy.im.helper.RestAPIListenner;
import net.wrappy.im.helper.layout.AppButton;
import net.wrappy.im.helper.layout.AppTextView;
import net.wrappy.im.model.SecurityQuestions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static net.wrappy.im.ui.SecurityQuestionActivity.ACTION_FROM_CREATE_NEW;

/**
 * Created by ben on 19/12/2017.
 */

public class SecurityQuestionCreateFragment extends Fragment {

    View mainView;
    ArrayAdapter<String> questionsAdapter;
    AppDelegate appDelegate;

    @BindView(R.id.btnQuestionComplete) AppButton btnQuestionComplete;
    @BindView(R.id.securityQuestionLayout) LinearLayout securityQuestionLayout;
    @BindView(R.id.txtSecurityTitle) AppTextView txtSecurityTitle;

    ArrayList<Spinner> spinnersQuestion = new ArrayList<>();
    ArrayList<EditText> appEditTextViewsAnswers = new ArrayList<>();

    public static SecurityQuestionCreateFragment newsIntance(int type) {
        SecurityQuestionCreateFragment fragment = new SecurityQuestionCreateFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("type",type);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        appDelegate = (AppDelegate) activity;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.security_question_create_fragment,null);
        ButterKnife.bind(this,mainView);
        int type = getArguments().getInt("type",0);
        if (type==1) {
            txtSecurityTitle.setText(getString(R.string.choose_new_secret_question));
        }
        getListQuestion();
        return mainView;
    }

    private void getListQuestion() {

        RestAPI.GetDataWrappy(getActivity(), RestAPI.GET_QUESTIONS_SECURITY, new RestAPIListenner() {


            @Override
            public void OnComplete(int httpCode, String error, String s) {
                try {
                    if (!RestAPI.checkHttpCode(httpCode)) {
                        return;
                    }
                    JsonArray jsonArray = (new JsonParser()).parse(s).getAsJsonArray();
                    ArrayList<String> stringQuestions = new ArrayList<>();
                    for (JsonElement jsonElement : jsonArray) {
                        try {
                            JsonObject jsonObject = jsonElement.getAsJsonObject();
                            JsonObject l10N = jsonObject.get("l10N").getAsJsonObject();
                            HashMap<String, String> stringHashMap = RestAPI.jsonToMap(l10N);
                            String question;
                            if (stringHashMap.containsKey(Locale.getDefault().toString())) {
                                question = stringHashMap.get(Locale.getDefault().toString());
                            } else {
                                question = stringHashMap.get("en_US");
                            }
                            stringQuestions.add(question);

                        }catch (Exception ex){
                            ex.printStackTrace();
                        }
                    }
                    questionsAdapter = new ArrayAdapter<String>(getActivity(),R.layout.registration_activity_security_question_item_textview,stringQuestions);
                    securityQuestionLayout.removeAllViews();
                    for (int i=0 ; i < 3; i++) {
                        View questionLayoutView = LayoutInflater.from(getActivity()).inflate(R.layout.registration_activity_security_question_item,null);
                        securityQuestionLayout.addView(questionLayoutView);
                        AppTextView txtQuestionTitle = (AppTextView) questionLayoutView.findViewById(R.id.txtQuestionTitle);
                        txtQuestionTitle.setText("Question " + String.valueOf(i+1));
                        Spinner questionSpinner = (Spinner) questionLayoutView.findViewById(R.id.spinnerQuestion);
                        spinnersQuestion.add(questionSpinner);
                        questionSpinner.setAdapter(questionsAdapter);
                        questionSpinner.setSelection(i);
                        EditText editTextView = (EditText) questionLayoutView.findViewById(R.id.edQuestionAnswer);
                        editTextView.setImeOptions(EditorInfo.IME_ACTION_DONE);
                        editTextView.setSingleLine();
                        appEditTextViewsAnswers.add(editTextView);
                    }
                    btnQuestionComplete.setEnabled(true);
                }catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

    }

    @OnClick(R.id.btnQuestionComplete)
    public void onClick(View v) {
        try {
            if (spinnersQuestion.size() > 0) {
                String errorString = "";
                ArrayList<String> strings = new ArrayList<>();
                ArrayList<String> stringQuestions = new ArrayList<>();
                ArrayList<SecurityQuestions> securityQuestions = new ArrayList<>();
                for (int i=0; i < spinnersQuestion.size(); i++) {
                    Spinner spinner = spinnersQuestion.get(i);
                    String question = spinner.getSelectedItem().toString();
                    EditText editTextView = appEditTextViewsAnswers.get(i);
                    String answer = editTextView.getText().toString().trim();
                    if (stringQuestions.size() > 0) {
                        if (stringQuestions.contains(question)) {
                            errorString = "Your question have duplicated";
                            break;
                        }
                    }
                    // validate answer text
                    if (answer.isEmpty()) {
                        errorString = String.format("Your answer of question %d is empty", (i+1));
                        break;
                    }

                    if (answer.length() < 3) {
                        errorString = String.format("Your answer of question %d must have at least 3 letter", (i+1));
                        break;
                    }

                    if (strings.size() > 0) {
                        if (strings.contains(answer)) {
                            errorString = "Your answer have duplicated";
                            break;
                        }
                    }
                    strings.add(answer);
                    stringQuestions.add(question);
                    SecurityQuestions questions = new SecurityQuestions(i+1,question,answer);
                    securityQuestions.add(questions);
                }
                if (!errorString.isEmpty()) {
                    AppFuncs.alert(getActivity(),errorString,true);
                    return;
                }
                if (securityQuestions!=null) {
                    String s = AppFuncs.convertToJson(securityQuestions).toString();
                    appDelegate.onChangeInApp(ACTION_FROM_CREATE_NEW,s);
                }
            }

        }catch (Exception ex) {
            ex.printStackTrace();
        }


    }

}
