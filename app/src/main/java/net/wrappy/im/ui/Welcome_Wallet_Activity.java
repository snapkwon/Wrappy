package net.wrappy.im.ui;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import net.wrappy.im.GethService.GethLightService;
import net.wrappy.im.R;

public class Welcome_Wallet_Activity extends AppCompatActivity {

    private Intent serviceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_wallet);

        serviceIntent = new Intent(getApplicationContext(), GethLightService.class);
        getApplicationContext().startService(serviceIntent);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isServiceRunning(GethLightService.class)){
            getApplicationContext().startService(serviceIntent);
        }

    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
