package net.wrappy.im.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Response;

import net.wrappy.im.R;
import net.wrappy.im.helper.AppDelegate;
import net.wrappy.im.helper.AppFuncs;
import net.wrappy.im.helper.RestAPI;
import net.wrappy.im.util.PopupUtils;

/**
 * Created by ben on 18/12/2017.
 */

public class SecurityQuestionActivity extends BaseActivity implements AppDelegate {

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
                    RestAPI.apiPUT(getApplicationContext(),RestAPI.PUT_CHANGE_SECURITY_QUESTION,jsonObject).setCallback(new FutureCallback<Response<String>>() {
                        @Override
                        public void onCompleted(Exception e, Response<String> result) {
                            if (result!=null) {
                                if (RestAPI.checkHttpCode(result.getHeaders().code())) {
                                    PopupUtils.showStatusViewDialog(SecurityQuestionActivity.this,true);
                                    finish();
                                } else {
                                    AppFuncs.alert(getApplicationContext(), "Fail", true);
                                }
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
