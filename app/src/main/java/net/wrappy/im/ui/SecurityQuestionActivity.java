package net.wrappy.im.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.wrappy.im.R;
import net.wrappy.im.helper.AppDelegate;
import net.wrappy.im.helper.AppFuncs;
import net.wrappy.im.helper.RestAPI;
import net.wrappy.im.util.PopupUtils;

/**
 * Created by ben on 18/12/2017.
 */

public class SecurityQuestionActivity extends BaseActivity implements AppDelegate {

    boolean isFinish = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.security_question_activity);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_ab_arrow_back);
        getSupportActionBar().setTitle(getString(R.string.security_question_change));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        changeFragment(ForgetPasswordQuestionFragment.newInstance(1));
    }

    private void changeFragment(Fragment fragment) {
        getFragmentManager().beginTransaction().replace(R.id.frChangeSecurityContainer, fragment).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            this.finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    Handler handler = new Handler();

    @Override
    public void onChangeInApp(int id, String data) {
        switch (id) {
            case ACTION_FROM_QUESTION:
                changeFragment(SecurityQuestionCreateFragment.newsIntance(1));
                break;
            case ACTION_FROM_CREATE_NEW:

                JsonObject jsonObject = new JsonObject();
                try {
                    JsonArray jsonArray = AppFuncs.convertToJson(data).getAsJsonArray();
                    jsonObject.add("kMemberSecurityQuestionDtoList", jsonArray);
                    RestAPI.PutDataWrappy(getApplicationContext(), jsonObject, RestAPI.PUT_CHANGE_SECURITY_QUESTION, new RestAPI.RestAPIListenner() {
                        @Override
                        public void OnComplete(int httpCode, String error, String s) {
                            if (RestAPI.checkHttpCode(httpCode)) {
                                isFinish = true;
                                ImageView view = new ImageView(SecurityQuestionActivity.this);
                                view.setImageResource(R.drawable.waiting_success);
                                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                params.gravity = Gravity.CENTER;
                                view.setLayoutParams(params);
                                PopupUtils.showCustomViewDialog(SecurityQuestionActivity.this,view,getString(R.string.success), getString(R.string.secu_success_description),-1,-1,null,null);
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        finish();
                                    }
                                },2000);
                            } else {
                                AppFuncs.alert(getApplicationContext(), getString(R.string.fail), true);
                                finish();
                            }
                        }
                    });
                }catch (Exception ex){
                    ex.printStackTrace();
                }
                break;
            default:
        }
    }
}
