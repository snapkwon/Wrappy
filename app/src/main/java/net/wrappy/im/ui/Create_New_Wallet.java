package net.wrappy.im.ui;

import android.content.Context;
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

public class Create_New_Wallet extends BaseActivity {

    private EditText password;
    private EditText repassword;
    private Button createnew;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create__new__wallet);

        password = (EditText) this.findViewById(net.wrappy.im.R.id.edtpassword);
        repassword = (EditText) this.findViewById(net.wrappy.im.R.id.edtrepassword);
        createnew = (Button)this.findViewById(net.wrappy.im.R.id.btcreatenew);

        createnew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!password.getText().toString().isEmpty())
                {
                    if(password.getText().toString().equals(repassword.getText().toString())) {
                        //  EthereumService.createAccount(password.getText().toString());
                        KeyManager keyManager = KeyManager.newKeyManager(Create_New_Wallet.this.getFilesDir().getAbsolutePath() + WalletInfo.KEYSTORE_PATH);
                        try {
                            keyManager.newAccount(password.getText().toString());
                            Intent intent = new Intent(Create_New_Wallet.this, Wallet_Complete_Activity.class);
                            startActivity(intent);
                            finish();
                        //    presentFragment(new Wallet_Complete_Activity(), true);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    else
                    {
                        AlertDialog.Builder builder = new AlertDialog.Builder(Create_New_Wallet.this);
                        LayoutInflater inflater = Create_New_Wallet.this.getLayoutInflater();
                        final View dialogView = inflater.inflate(R.layout.custom_alert_dialog, null);
                        builder.setView(dialogView);

                        final TextView tvTitle = (TextView) dialogView.findViewById(R.id.texttitlealert);
                        tvTitle.setText("Password mismatch");

                        builder.setPositiveButton("OK", null);
                        builder.show();
                    }
                }
                else
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(Create_New_Wallet.this);
                    LayoutInflater inflater = Create_New_Wallet.this.getLayoutInflater();
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
