package net.wrappy.im.ui.legacy;

import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;
import android.widget.EditText;

import net.wrappy.im.ImApp;
import net.wrappy.im.R;
import net.wrappy.im.crypto.IOtrChatSession;
import net.wrappy.im.crypto.otr.OtrDebugLogger;
import net.wrappy.im.model.Address;
import net.wrappy.im.service.IChatSession;
import net.wrappy.im.service.ImServiceConstants;
import net.wrappy.im.ui.BaseActivity;
import net.wrappy.im.util.PopupUtils;

public class SmpResponseActivity extends BaseActivity {

    private EditText mInputSMP;
    private String mSessionId;
    private String mQuestion;
    private long mProviderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mInputSMP = new EditText(this);

        mSessionId = getIntent().getStringExtra("sid");
        mProviderId = getIntent().getLongExtra(ImServiceConstants.EXTRA_INTENT_PROVIDER_ID, -1);
        mQuestion = getIntent().getStringExtra("q");
        showQuestionDialog();
    }

    private void showQuestionDialog() {
        String title = getString(R.string.smp_question_title);
        String strQuestion = mSessionId + ": " + mQuestion;

        PopupUtils.showCustomViewDialog(this, mInputSMP, title, strQuestion, R.string.yes, R.string.cancel, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String secret = mInputSMP.getText().toString();
                respondSmp(mSessionId, secret);

                SmpResponseActivity.this.finish();
            }
        }, null);
    }

    private void respondSmp(String sid, String answer) {

        ImApp app = (ImApp) getApplication();


        IOtrChatSession iOtrSession;
        try {
            IChatSession chatSession = app.getChatSession(mProviderId, -1, Address.stripResource(sid));
            iOtrSession = chatSession.getDefaultOtrChatSession();
            if (iOtrSession == null) {
                OtrDebugLogger.log("no session in progress for provider " + mProviderId);
                return;
            }
            iOtrSession.respondSmpVerification(answer);

        } catch (RemoteException e) {
            OtrDebugLogger.log("could not respond to SMP", e);
        }
    }

}
