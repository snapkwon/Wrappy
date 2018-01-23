package net.wrappy.im.helper;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import net.wrappy.im.ImApp;
import net.wrappy.im.crypto.otr.OtrAndroidKeyManagerImpl;
import net.wrappy.im.model.RegistrationAccount;
import net.wrappy.im.ui.legacy.SignInHelper;
import net.wrappy.im.ui.legacy.SimpleAlertHandler;
import net.wrappy.im.ui.onboarding.OnboardingAccount;
import net.wrappy.im.ui.onboarding.OnboardingManager;

import java.lang.ref.WeakReference;
import java.security.KeyPair;

/**
 * Created by ben on 16/01/2018.
 */

public class LoginTask extends AsyncTask<RegistrationAccount, Void, OnboardingAccount> {

    private Activity activity;
    private SimpleAlertHandler mHandler;
    private EventListenner listenner;
    private RegistrationAccount registrationAccount;
    private WeakReference<Activity> weakReference;

    public interface EventListenner {
        void OnComplete(boolean isSuccess, OnboardingAccount onboardingAccount);
    }

    public LoginTask(Activity activity, EventListenner listenner) {
        this.activity = activity;
        this.listenner = listenner;
        mHandler = new SimpleAlertHandler(activity);
        weakReference = new WeakReference<>(activity);
    }


    @Override
    protected OnboardingAccount doInBackground(RegistrationAccount... accounts) {
        try {

            OtrAndroidKeyManagerImpl keyMan = OtrAndroidKeyManagerImpl.getInstance(activity);
            KeyPair keyPair = keyMan.generateLocalKeyPair();

            registrationAccount = accounts[0];
            OnboardingAccount result = OnboardingManager.addExistingAccount(activity, mHandler, registrationAccount);

            if (result != null) {
                String jabberId = result.username + '@' + result.domain;
                keyMan.storeKeyPair(jabberId, keyPair);
            }

            return result;
        } catch (Exception e) {
            Log.e(ImApp.LOG_TAG, "auto onboarding fail", e);
            return null;
        }
    }

    @Override
    protected void onPostExecute(final OnboardingAccount account) {
        if (weakReference.get() == null)
            return;

        if (account == null) {
            listenner.OnComplete(false, null);
            return;
        }
        ImApp mApp = (ImApp) activity.getApplication();
        mApp.setDefaultAccount(account.providerId, account.accountId);

        SignInHelper signInHelper = new SignInHelper(activity, mHandler);
        signInHelper.setSignInListener(new SignInHelper.SignInListener() {
            @Override
            public void connectedToService() {
                if (listenner != null) {
                    listenner.OnComplete(true, account);
                }
            }

            @Override
            public void stateChanged(int state, long accountId) {
            }
        });
        signInHelper.activateAccount(account.providerId, account.accountId);
        signInHelper.signIn(account.password, account.providerId, account.accountId, true);
    }
}
