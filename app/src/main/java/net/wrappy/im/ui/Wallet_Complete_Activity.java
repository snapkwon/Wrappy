package net.wrappy.im.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.zxing.WriterException;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import me.ydcool.lib.qrmodule.encoding.QrGenerator;

import net.wrappy.im.GethService.KeyManager;
import net.wrappy.im.GethService.Wallet;
import net.wrappy.im.GethService.WalletInfo;
import net.wrappy.im.R;
import net.wrappy.im.helper.FileUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

public class Wallet_Complete_Activity extends Activity {

    private Button backup;
    private Button noThanks;
    private final static int PICK_FOLDER_RESULT_CODE =1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet__complete);
        backup = (Button) this.findViewById(R.id.buttonbackup);
        noThanks = (Button)this.findViewById(R.id.buttonthanks);

        noThanks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              /*  try {
                    wallet.deployKYC(wallet.accountManager.getAccounts().get(0).getAddress(), UserConfig.getCurrentUser().phone);
                } catch (Exception e) {
                    e.printStackTrace();
                }*/
                //presentFragment(new WalletFragment(), true);
                finish();
            }
        });


        backup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               pickFolder();
            }
        });
    }

    void pickFolder() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        //  startActivityForResult(intent, REQUEST_CODE_OPEN_DIRECTORY);
        try {
            startActivityForResult(intent,PICK_FOLDER_RESULT_CODE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        switch (requestCode) {
            case PICK_FOLDER_RESULT_CODE: {
                if (resultCode== Activity.RESULT_OK && data!=null && data.getData()!=null) {

                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(Wallet_Complete_Activity.this);
                    LayoutInflater inflater = Wallet_Complete_Activity.this.getLayoutInflater();
                    final View dialogView = inflater.inflate(R.layout.dialog_with_edittext, null);
                    dialogBuilder.setView(dialogView);

                    final EditText edt = (EditText) dialogView.findViewById(R.id.etinputpass);
                    edt.setHint(Html.fromHtml("<small><i>" + "Input Password" + "</i></small>"));

                    dialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {

                            Uri treeUri = data.getData();
                            final String theFolderPath = FileUtil.getFullPathFromTreeUri(treeUri,Wallet_Complete_Activity.this);

                            File sourceLocation = new File(Wallet.getKeystorePath(Wallet_Complete_Activity.this.getFilesDir()));
                            KeyManager keyManager = KeyManager.newKeyManager(getApplicationContext().getFilesDir().getAbsolutePath() + WalletInfo.KEYSTORE_PATH);
                            File targetLocation = new File(theFolderPath);

                            try {
                                String jsondata  = null;

                                jsondata = new String(keyManager.getKeystore().exportKey(keyManager.getKeystore().getAccounts().get(0),edt.getText().toString(), edt.getText().toString()), "UTF-8");

                                Bitmap qrCode = null;
                                qrCode = new QrGenerator.Builder()
                                        .content(jsondata)
                                        .qrSize(1024)
                                        .margin(2)
                                        .color(Color.BLACK)
                                        .bgColor(Color.WHITE)
                                        .ecc(ErrorCorrectionLevel.H)
                                        .overlayAlpha(255)
                                        .overlayXfermode(PorterDuff.Mode.SRC)
                                        .encode();


                                FileOutputStream out = null;
                                try {
                                    Calendar cal = Calendar.getInstance();
                                    String filename ="UTC--" + cal.getTime().toString() +"--"+ keyManager.getAccounts().get(0).getAddress().getHex();
                           /* for (File f : sourceLocation.listFiles()) {
                                if (f.isFile())
                                    filename = f.getName();
                            }*/

                                    File dest = new File(targetLocation, filename + ".png");
                                    out = new FileOutputStream(dest);
                                    qrCode.compress(Bitmap.CompressFormat.PNG, 90, out);
                                    out.flush();
                                    out.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                //  FileUtil.copyDirectory(sourceLocation,targetLocation);
                                AlertDialog.Builder builder = new AlertDialog.Builder(Wallet_Complete_Activity.this);
                                LayoutInflater inflater = Wallet_Complete_Activity.this.getLayoutInflater();
                                final View dialogView = inflater.inflate(net.wrappy.im.R.layout.custom_alert_dialog, null);
                                builder.setView(dialogView);

                                final TextView tvTitle = (TextView) dialogView.findViewById(net.wrappy.im.R.id.texttitlealert);
                                tvTitle.setText("Backup Successfully");

                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        //   presentFragment(new WalletFragment(), true);
                                        finish();
                                    }
                                });
                                builder.show();
                            } catch (Exception e) {
                                e.printStackTrace();
                                AlertDialog.Builder builder = new AlertDialog.Builder(Wallet_Complete_Activity.this);
                                LayoutInflater inflater = Wallet_Complete_Activity.this.getLayoutInflater();
                                final View dialogView = inflater.inflate(net.wrappy.im.R.layout.custom_alert_dialog, null);
                                builder.setView(dialogView);

                                final TextView tvTitle = (TextView) dialogView.findViewById(net.wrappy.im.R.id.texttitlealert);
                                tvTitle.setText(e.toString());

                                builder.setPositiveButton("OK", null);
                                builder.show();
                            }

                        }
                    });

                    dialogBuilder.setNegativeButton("Cancel", null);
                    AlertDialog b = dialogBuilder.create();
                    b.show();

                    //  FileUtil.copyfile(Wallet.getKeystorePath(getParentActivity().getFilesDir()),theFolderPath);
                   /* AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                    LayoutInflater inflater = getParentActivity().getLayoutInflater();
                    final View dialogView = inflater.inflate(R.layout.dialog_input_password, null);
                    builder.setView(dialogView);

                    final EditText edtpass = (EditText) dialogView.findViewById(R.id.edtinputpass);
                    final EditText edtnewpass = (EditText) dialogView.findViewById(R.id.edtnewpass);

                    builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String jsondata  = null;
                            try {
                                jsondata = new String(wallet.exportAccount(edtpass.getText().toString(),edtnewpass.getText().toString()), "UTF-8");
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                            writeToFile(jsondata,theFolderPath);
                        }
                    });
                    showDialog(builder.create());*/

                }
                break;
            }
        }
    }

}
