package net.wrappy.im.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.wrappy.im.R;
import net.wrappy.im.helper.AppFuncs;
import net.wrappy.im.helper.RestAPI;
import net.wrappy.im.helper.layout.AppButton;
import net.wrappy.im.helper.layout.AppTextView;
import net.wrappy.im.model.SecurityQuestions;
import net.wrappy.im.model.WpKAuthDto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by ben on 13/11/2017.
 */

public class RegistrationSecurityQuestionActivity extends AppCompatActivity implements View.OnClickListener {

    LinearLayout securityQuestionLayout;

    ImageButton headerbarBack;
    AppTextView headerbarTitle;
    ArrayAdapter<String> questionsAdapter;
    AppButton btnQuestionComplete;
    ArrayList<Spinner> spinnersQuestion = new ArrayList<>();
    ArrayList<EditText> appEditTextViewsAnswers = new ArrayList<>();
    boolean isFlag;

    String password;

    private

    ProgressDialog dialog;
    JsonObject registerJson = new JsonObject();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration_activity_security_question);

//        Bundle arg = getIntent().getExtras();
//        if (arg!=null) {
//            password = arg.getString("password","");
//            if (password.equalsIgnoreCase("")) {
//                finish();
//                return;
//            }
//            JsonObject secretJson = new JsonObject();
//            secretJson.addProperty("secret",password);
//            registerJson.add("wpKAuthDto", secretJson);
//        }

        referenceView();
        dialog = new ProgressDialog(RegistrationSecurityQuestionActivity.this);
        dialog.setCancelable(false);
    }

    private void referenceView() {
        securityQuestionLayout = (LinearLayout) findViewById(R.id.securityQuestionLayout);
        headerbarBack = (ImageButton) findViewById(R.id.headerbarBack);
        headerbarBack.setOnClickListener(this);
        headerbarTitle = (AppTextView) findViewById(R.id.headerbarTitle);
        headerbarTitle.setText("REGISTRATION");
        btnQuestionComplete = (AppButton) findViewById(R.id.btnQuestionComplete);
        btnQuestionComplete.setOnClickListener(this);

        getListQuestion();

    }

    private void getListQuestion() {

        RestAPI.GetDataWrappy(getApplicationContext(), RestAPI.GET_QUESTIONS_SECURITY, new RestAPI.RestAPIListenner() {


            @Override
            public void OnComplete(int httpCode, String error, String s) {
                try {
                    if (!RestAPI.checkHttpCode(httpCode)) {
                        AppFuncs.alert(getApplicationContext(),"Connection fail", true);
                        finish();
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
                    questionsAdapter = new ArrayAdapter<String>(RegistrationSecurityQuestionActivity.this,R.layout.registration_activity_security_question_item_textview,stringQuestions);
                    securityQuestionLayout.removeAllViews();
                    for (int i=0 ; i < 3; i++) {
                        View questionLayoutView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.registration_activity_security_question_item,null);
                        securityQuestionLayout.addView(questionLayoutView);
                        AppTextView txtQuestionTitle = (AppTextView) questionLayoutView.findViewById(R.id.txtQuestionTitle);
                        txtQuestionTitle.setText("Question " + String.valueOf(i+1));
                        Spinner questionSpinner = (Spinner) questionLayoutView.findViewById(R.id.spinnerQuestion);
                        spinnersQuestion.add(questionSpinner);
                        questionSpinner.setAdapter(questionsAdapter);
                        questionSpinner.setSelection(i);
                        EditText editTextView = (EditText) questionLayoutView.findViewById(R.id.edQuestionAnswer);
                        appEditTextViewsAnswers.add(editTextView);
                    }
                    btnQuestionComplete.setEnabled(true);
                }catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

    }



    @Override
    public void onClick(View view) {
        if (isFlag) {
            return;
        }isFlag = true;
        try {
            if (view.getId() == headerbarBack.getId()) {
                LauncherActivity.start(RegistrationSecurityQuestionActivity.this);
                finish();
            }
            if (view.getId() == btnQuestionComplete.getId()) {
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

                        if (AppFuncs.detectSpecialCharacters(answer)) {
                            errorString = String.format("Your answer of question %d have special characters", (i+1));
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
                        AppFuncs.alert(getApplicationContext(),errorString,true);
                        return;
                    }
                    WpKAuthDto wpKAuthDto = getIntent().getParcelableExtra(WpKAuthDto.class.getName());

                    Intent intent = new Intent(RegistrationSecurityQuestionActivity.this,UpdateProfileActivity.class);
                    intent.putExtra(WpKAuthDto.class.getName(),wpKAuthDto);
                    intent.putParcelableArrayListExtra(SecurityQuestions.class.getName(),securityQuestions);
                    startActivity(intent);
                }

            }
        }catch (Exception ex) {
            ex.printStackTrace();
        }finally {
            isFlag = false;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }
}
