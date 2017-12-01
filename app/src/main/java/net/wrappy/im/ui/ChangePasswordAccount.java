package net.wrappy.im.ui;

import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import net.wrappy.im.GethService.KeyManager;
import net.wrappy.im.GethService.WalletInfo;
import net.wrappy.im.R;

public class ChangePasswordAccount extends AppCompatActivity {

    private EditText oldpassword;
    private EditText newpassword;
    private EditText renewpassword;
    private Button changepass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password_account);

        oldpassword = (EditText) this.findViewById(net.wrappy.im.R.id.edtoldpassword);
        newpassword = (EditText) this.findViewById(net.wrappy.im.R.id.edtnewpassword);
        renewpassword = (EditText) this.findViewById(net.wrappy.im.R.id.edtnewrepassword);
        changepass = (Button)this.findViewById(net.wrappy.im.R.id.btchangepass);

        changepass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!newpassword.getText().toString().isEmpty())
                {
                    if(newpassword.getText().toString().equals(renewpassword.getText().toString())) {
                        //  EthereumService.createAccount(password.getText().toString());
                        KeyManager keyManager = KeyManager.newKeyManager(ChangePasswordAccount.this.getFilesDir().getAbsolutePath() + WalletInfo.KEYSTORE_PATH);
                        try {
                          //  keyManager.newAccount(password.getText().toString());
                            byte[] keystoredata;
                            keystoredata = keyManager.getKeystore().exportKey(keyManager.getKeystore().getAccounts().get(0),oldpassword.getText().toString(),newpassword.getText().toString());

                            if(keystoredata!=null)
                            {
                                keyManager.deleteAccount(keyManager.getKeystore().getAccounts().get(0),oldpassword.getText().toString());
                                keyManager.getKeystore().importKey(keystoredata,newpassword.getText().toString(),newpassword.getText().toString());
                            }

                            AlertDialog.Builder builder = new AlertDialog.Builder(ChangePasswordAccount.this);
                            LayoutInflater inflater = ChangePasswordAccount.this.getLayoutInflater();
                            final View dialogView = inflater.inflate(R.layout.custom_alert_dialog, null);
                            builder.setView(dialogView);

                            final TextView tvTitle = (TextView) dialogView.findViewById(R.id.texttitlealert);
                            tvTitle.setText("Change Password successful");

                            builder.setPositiveButton("OK", null);
                            builder.show();

                        } catch (Exception e) {
                            e.printStackTrace();
                            AlertDialog.Builder builder = new AlertDialog.Builder(ChangePasswordAccount.this);
                            LayoutInflater inflater = ChangePasswordAccount.this.getLayoutInflater();
                            final View dialogView = inflater.inflate(R.layout.custom_alert_dialog, null);
                            builder.setView(dialogView);

                            final TextView tvTitle = (TextView) dialogView.findViewById(R.id.texttitlealert);
                            tvTitle.setText("Old Password mismatch");

                            builder.setPositiveButton("OK", null);
                            builder.show();
                        }
                    }
                    else
                    {
                        AlertDialog.Builder builder = new AlertDialog.Builder(ChangePasswordAccount.this);
                        LayoutInflater inflater = ChangePasswordAccount.this.getLayoutInflater();
                        final View dialogView = inflater.inflate(R.layout.custom_alert_dialog, null);
                        builder.setView(dialogView);

                        final TextView tvTitle = (TextView) dialogView.findViewById(R.id.texttitlealert);
                        tvTitle.setText("Repeat Password mismatch");

                        builder.setPositiveButton("OK", null);
                        builder.show();
                    }
                }
                else
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ChangePasswordAccount.this);
                    LayoutInflater inflater = ChangePasswordAccount.this.getLayoutInflater();
                    final View dialogView = inflater.inflate(net.wrappy.im.R.layout.custom_alert_dialog, null);
                    builder.setView(dialogView);

                    final TextView tvTitle = (TextView) dialogView.findViewById(net.wrappy.im.R.id.texttitlealert);
                    tvTitle.setText("Enter a password");

                    builder.setPositiveButton("OK", null);
                    builder.show();
                }
            }
        });

    }
}
