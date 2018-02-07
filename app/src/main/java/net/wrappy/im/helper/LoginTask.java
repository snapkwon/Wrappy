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

    private SimpleAlertHandler mHandler;
    private EventListenner listenner;
    private WeakReference<Activity> weakReference;

    public interface EventListenner {
        void OnComplete(boolean isSuccess, OnboardingAccount onboardingAccount);
    }

    public LoginTask(Activity activity, EventListenner listenner) {
        this.listenner = listenner;
        mHandler = new SimpleAlertHandler(activity);
        weakReference = new WeakReference<>(activity);
    }

    public Activity getActivity() {
        if (weakReference != null) {
            return weakReference.get();
        }
        return null;
    }

    @Override
    protected OnboardingAccount doInBackground(RegistrationAccount... accounts) {
        try {
            if (getActivity() != null) {
                OtrAndroidKeyManagerImpl keyMan = OtrAndroidKeyManagerImpl.getInstance(getActivity());
                KeyPair keyPair = keyMan.generateLocalKeyPair();

                RegistrationAccount registrationAccount = accounts[0];
                final OnboardingAccount result = OnboardingManager.addExistingAccount(getActivity(), mHandler, registrationAccount);

                if (result != null) {
                    String jabberId = result.username + '@' + result.domain;
                    keyMan.storeKeyPair(jabberId, keyPair);
                    SignInHelper signInHelper = new SignInHelper(getActivity(), mHandler);
                    signInHelper.setSignInListener(new SignInHelper.SignInListener() {
                        @Override
                        public void connectedToService() {
                            if (listenner != null) {
                                listenner.OnComplete(true, result);
                            }
                        }

                        @Override
                        public void stateChanged(int state, long accountId) {
                        }
                    });
                    signInHelper.activateAccount(result.providerId, result.accountId);
                    signInHelper.signIn(result.password, result.providerId, result.accountId, true);
                }
                return result;
            }
        } catch (Exception e) {
            Log.e(ImApp.LOG_TAG, "auto onboarding fail", e);

        }
        return null;
    }

    @Override
    protected void onPostExecute(final OnboardingAccount account) {
        if (weakReference.get() == null)
            return;

        if (account == null) {
            listenner.OnComplete(false, null);
            return;
        }

    }
}
