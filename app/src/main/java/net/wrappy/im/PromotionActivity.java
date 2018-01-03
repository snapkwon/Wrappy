package net.wrappy.im;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by ben on 03/01/2018.
 */

public class PromotionActivity extends Activity {

    public static void start(Activity activity, Bundle bundle) {
        Intent intent = new Intent(activity, PromotionActivity.class);
        if (bundle!=null) {
            intent.putExtras(bundle);
        }
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.promotion_activity);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.btnPromotionClose)
    public void onClick(View view) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
