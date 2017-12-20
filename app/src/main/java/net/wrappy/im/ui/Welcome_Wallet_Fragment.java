package net.wrappy.im.ui;


import android.app.Activity;
import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import net.wrappy.im.GethService.GethLightService;
import net.wrappy.im.GethService.Wallet;
import net.wrappy.im.R;
import net.wrappy.im.util.PopupUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;

import me.ydcool.lib.qrmodule.activity.QrScannerActivity;

/**
 * Created by PCPV on 11/27/2017.
 */

public class Welcome_Wallet_Fragment extends Fragment {

    private Intent serviceIntent;
    private Button CreateNew;
    private Button ImportWallet;

    private  static final int  ADDRESS_LENGTH = 40;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(
                R.layout.activity_welcome_wallet, container, false);

        serviceIntent = new Intent(getActivity(), GethLightService.class);


        CreateNew = (Button) view.findViewById(R.id.buttoncreatenew);
        ImportWallet = (Button) view.findViewById(R.id.buttonimport);

        CreateNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Create_New_Wallet.class);
                startActivity(intent);
               // finish();
                //  presentFragment(new Create_New_Wallet_Activity(),true);

            }
        });

        ImportWallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), QrScannerActivity.class);
                startActivityForResult(intent, QrScannerActivity.QR_REQUEST_CODE);
              //  Intent fileIntent = new Intent(Intent.ACTION_GET_CONTENT);
               // fileIntent.setType("*/*"); // intent type to filter application based on your requirement
               // startActivityForResult(fileIntent, 1000);
            }
        });
       //getActivity().startService(serviceIntent);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isServiceRunning(GethLightService.class)){
          //  getActivity().startService(serviceIntent);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // In fragment class callback
        if( requestCode==QrScannerActivity.QR_REQUEST_CODE && resultCode== Activity.RESULT_OK)
        {
            try {
                    Calendar cal = Calendar.getInstance();
                    String dataKeyStore = data.getExtras().getString(QrScannerActivity.QR_RESULT_STR);
                    if (checkKeystorefile(dataKeyStore)) {
                        String filename ="UTC--" + cal.getTime().toString() +"--"+ getAdressImportKey(dataKeyStore);
                        writeStringAsFile(dataKeyStore,filename);
                    }

            }catch (Exception ex){}
        }
    }

    private String getAdressImportKey(String keystore)
    {
        JSONObject mainObject = null;
        String addressObject = "";
        try {
            mainObject = new JSONObject(keystore);
            addressObject = mainObject.getString("address");
        }
        catch (Exception e)
        {

        }
        return addressObject;
    }

    private boolean checkKeystorefile(String keystore)
    {
        JSONObject mainObject = null;
        String addressObject = "";
        try {
            mainObject = new JSONObject(keystore);
            addressObject = mainObject.getString("address");
            if(addressObject.isEmpty())
            {
                return false;
            }
            else
            {
                if(addressObject.length() != ADDRESS_LENGTH)
                {
                    return false;
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private String readFile(Uri fileUri) throws IOException {

        InputStream inputStream = null;
        try {
            ContentResolver content = getActivity().getContentResolver();
            inputStream = content.openInputStream(fileUri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int i;
        try {
            i = inputStream.read();
            while (i != -1) {
                byteArrayOutputStream.write(i);
                i = inputStream.read();
            }
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return byteArrayOutputStream.toString();
    }


    public  void writeStringAsFile(final String fileContents, String fileName) {
        try {
            FileWriter out = new FileWriter(new File(Wallet.getKeystorePath(getActivity().getFilesDir()), fileName));
            out.write(fileContents);
            out.close();
        } catch (IOException e) {

        }
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }


    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
