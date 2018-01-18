package net.wrappy.im.ui.promotion;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import net.wrappy.im.R;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by ben on 03/01/2018.
 */

public class PromotionActivity extends Activity {

    public static void start(Activity activity, Bundle bundle) {
        Intent intent = new Intent(activity, PromotionActivity.class);
        if (bundle != null) {
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
        finish();
    }

    @OnClick(R.id.lnInviteFriend)
    public void inviteFriendToGetPromotion() {
        shareText();
    }

    public void shareText() {
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("text/plain");
        String shareBodyText = "Your sharing message goes here";
        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject/Title");
        intent.putExtra(android.content.Intent.EXTRA_TEXT, shareBodyText);
        startActivity(Intent.createChooser(intent, getString(R.string.share_title)));
    }
}
