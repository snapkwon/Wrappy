package net.wrappy.im.ui;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
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

        // back button at action bar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getResources().getString(R.string.title_change_password_wallet));
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_action_arrow_back);
        }

        // show/hide password
        showHidePassword();
    }

    /**
     * Show/hide password when click visible icon
     */
    private void showHidePassword() {
        oldpassword.setOnTouchListener(new View.OnTouchListener() {

            boolean isShow = false;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if (motionEvent.getRawX() >= (oldpassword.getRight() - oldpassword.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        if (!isShow) {
                            // show password
                            oldpassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                            isShow = true;
                        } else {
                            // hide password
                            oldpassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT);
                            isShow = false;
                        }
                        oldpassword.setSelection(oldpassword.getText().length());
                        return false;
                    }
                }
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
