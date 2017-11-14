package net.wrappy.im;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.wrappy.im.helper.AppFuncs;
import net.wrappy.im.helper.RectAPI;
import net.wrappy.im.helper.layout.AppButton;
import net.wrappy.im.helper.layout.AppEditTextView;
import net.wrappy.im.helper.layout.AppTextView;
import net.wrappy.im.ui.BaseActivity;

import java.util.ArrayList;

/**
 * Created by ben on 13/11/2017.
 */

public class RegistrationSecurityQuestionActivity extends BaseActivity implements View.OnClickListener {

    LinearLayout securityQuestionLayout;
    String[] questions;
    ImageButton headerbarBack;
    ArrayAdapter<String> questionsAdapter;
    AppButton btnQuestionComplete;
    ArrayList<Spinner> spinnersQuestion = new ArrayList<>();
    ArrayList<AppEditTextView> appEditTextViewsAnswers = new ArrayList<>();
    boolean isFlag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration_activity_security_question);
        referenceView();
    }

    private void referenceView() {
        securityQuestionLayout = (LinearLayout) findViewById(R.id.securityQuestionLayout);
        headerbarBack = (ImageButton) findViewById(R.id.headerbarBack);
        headerbarBack.setOnClickListener(this);
        btnQuestionComplete = (AppButton) findViewById(R.id.btnQuestionComplete);
        btnQuestionComplete.setOnClickListener(this);
        getListQuestion();

    }

    private void getListQuestion() {
        new RectAPI.GetDataUrl(new RectAPI.RectAPIListenner() {
            @Override
            public void OnComplete(String error, String s) {
                try {
                    //TODO get data question list from url
                    if (error!=null) {
                        AppFuncs.alert(getApplicationContext(),error,true);
                        return;
                    }
                    AppFuncs.log(s);
                    JsonObject jsonObject = RectAPI.parseStringToJsonElement(s).getAsJsonObject();
                    if (RectAPI.getStatus(jsonObject)==1) {
                        JsonArray jsonArrayQuestions = RectAPI.getData(jsonObject).getAsJsonArray();
                        questions = new String[jsonArrayQuestions.size()];
                        for (int i=0 ; i<jsonArrayQuestions.size();i++) {
                            questions[i] = jsonArrayQuestions.get(i).getAsJsonObject().get("question").getAsString();
                        }
                        questionsAdapter = new ArrayAdapter<String>(RegistrationSecurityQuestionActivity.this,R.layout.registration_activity_security_question_item_textview,questions);
                        securityQuestionLayout.removeAllViews();
                        if (questions.length > 2) {
                            for (int i=0 ; i < 3; i++) {
                                View questionLayoutView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.registration_activity_security_question_item,null);
                                securityQuestionLayout.addView(questionLayoutView);
                                AppTextView txtQuestionTitle = (AppTextView) questionLayoutView.findViewById(R.id.txtQuestionTitle);
                                txtQuestionTitle.setText("Question " + String.valueOf(i+1));
                                Spinner questionSpinner = (Spinner) questionLayoutView.findViewById(R.id.spinnerQuestion);
                                spinnersQuestion.add(questionSpinner);
                                questionSpinner.setAdapter(questionsAdapter);
                                questionSpinner.setSelection(i);
                                AppEditTextView editTextView = (AppEditTextView) questionLayoutView.findViewById(R.id.edQuestionAnswer);
                                appEditTextViewsAnswers.add(editTextView);
                            }
                            btnQuestionComplete.setEnabled(true);
                        }
                    }
                }catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).execute(RectAPI.GET_QUESTIONS_SECURITY);
    }

    @Override
    public void onClick(View view) {
        if (isFlag) {
            return;
        }isFlag = true;
        try {
            if (view.getId() == headerbarBack.getId()) {
                finish();
            }
            if (view.getId() == btnQuestionComplete.getId()) {
                if (spinnersQuestion.size() > 0) {
                    JsonArray jsonArray = new JsonArray();
                    String errorString = "";
                    ArrayList<String> strings = new ArrayList<>();
                    for (int i=0; i < spinnersQuestion.size(); i++) {
                        Spinner spinner = spinnersQuestion.get(i);
                        String question = spinner.getSelectedItem().toString();
                        AppEditTextView editTextView = appEditTextViewsAnswers.get(i);
                        String answer = editTextView.getText().toString().trim();
                        // validate answer text
                        if (answer.isEmpty()) {
                            errorString = String.format("Your answer of question %d is empty", (i+1));
                            break;
                        }

                        if (strings.size() > 0) {
                            if (strings.contains(answer)) {
                                errorString = "Your answer have duplicated";
                                break;
                            }
                        }
                        strings.add(answer);

                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("question",question);
                        jsonObject.addProperty("answer",answer);


                        jsonArray.add(jsonObject);
                    }
                    if (!errorString.isEmpty()) {
                        AppFuncs.alert(getApplicationContext(),errorString,true);
                        return;
                    }
                    String s = new RectAPI.PostDataUrl(jsonArray.toString(), null).execute(RectAPI.POST_QUESTION_ANSWERS).get();
                    AppFuncs.log(s);
                }

            }
        }catch (Exception ex) {
            ex.printStackTrace();
        }finally {
            isFlag = false;
        }

    }
}
