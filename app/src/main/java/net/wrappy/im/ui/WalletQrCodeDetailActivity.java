package net.wrappy.im.ui;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import net.wrappy.im.GethService.KeyManager;
import net.wrappy.im.GethService.Wallet;
import net.wrappy.im.GethService.WalletInfo;
import net.wrappy.im.R;
import net.wrappy.im.helper.FileUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

import me.ydcool.lib.qrmodule.encoding.QrGenerator;

public class WalletQrCodeDetailActivity extends AppCompatActivity implements View.OnClickListener  {

    ImageView wallet_qr_img;
    TextView wallet_qr_address_text;
    Button wallet_qr_btn_coppy,wallet_qr_btn_backup;
    String hexAddress;
    private KeyManager keyManager;
    private final static int PICK_FOLDER_RESULT_CODE =1000;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_qr_code_detail);

        wallet_qr_img = (ImageView) findViewById(R.id.wallet_qr_img);
        wallet_qr_address_text = (TextView) findViewById(R.id.wallet_qr_address_text);
        wallet_qr_btn_coppy = (Button) findViewById(R.id.wallet_qr_btn_coppy);
        wallet_qr_btn_coppy.setOnClickListener(this);
        wallet_qr_btn_backup = (Button) findViewById(R.id.wallet_qr_btn_backup);
        wallet_qr_btn_backup.setOnClickListener(this);

        try {
            keyManager = KeyManager.newKeyManager(getApplicationContext().getFilesDir().getAbsolutePath() + WalletInfo.KEYSTORE_PATH);
            hexAddress = keyManager.getKeystore().getAccounts().get(0).getAddress().getHex();

            wallet_qr_address_text.setText(hexAddress);
            Bitmap qrCode = new QrGenerator.Builder()
                    .content(hexAddress)
                    .qrSize(140)
                    .margin(2)
                    .color(Color.BLACK)
                    .bgColor(Color.WHITE)
                    .ecc(ErrorCorrectionLevel.H)
                    .overlayAlpha(255)
                    .overlayXfermode(PorterDuff.Mode.SRC)
                    .encode();

            wallet_qr_img.setImageBitmap(qrCode);
        }catch (Exception ex){
            ex.printStackTrace();
        }
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

    private void writeToFile(String data ,String path) {
        try {
            Calendar cal = Calendar.getInstance();

            String filename ="UTC--" + cal.getTime().toString() +"--"+ hexAddress;

            FileOutputStream fos = new FileOutputStream(path + "/" + filename);
            fos.write(data.getBytes());
            fos.close();
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
            AlertDialog.Builder builder = new AlertDialog.Builder(WalletQrCodeDetailActivity.this);
            LayoutInflater inflater = WalletQrCodeDetailActivity.this.getLayoutInflater();
            final View dialogView = inflater.inflate(R.layout.custom_alert_dialog, null);
            builder.setView(dialogView);

            final TextView tvTitle = (TextView) dialogView.findViewById(R.id.texttitlealert);
            tvTitle.setText(e.toString());

            builder.setPositiveButton("OK", null);
            builder.show();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(WalletQrCodeDetailActivity.this);
        LayoutInflater inflater = WalletQrCodeDetailActivity.this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.custom_alert_dialog, null);
        builder.setView(dialogView);

        final TextView tvTitle = (TextView) dialogView.findViewById(R.id.texttitlealert);
        tvTitle.setText("Backup Successfully");

        builder.setPositiveButton("OK", null);
        builder.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        switch (requestCode) {
            case PICK_FOLDER_RESULT_CODE: {
                if (resultCode== Activity.RESULT_OK && data!=null && data.getData()!=null) {
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(WalletQrCodeDetailActivity.this);
                    LayoutInflater inflater = WalletQrCodeDetailActivity.this.getLayoutInflater();
                    final View dialogView = inflater.inflate(R.layout.dialog_with_edittext, null);
                    dialogBuilder.setView(dialogView);

                    final EditText edt = (EditText) dialogView.findViewById(R.id.etinputpass);
                    edt.setHint(Html.fromHtml("<small><i>" + "Input Password" + "</i></small>"));

                    dialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {

                            Uri treeUri = data.getData();
                            final String theFolderPath = FileUtil.getFullPathFromTreeUri(treeUri,WalletQrCodeDetailActivity.this);

                            File sourceLocation = new File(Wallet.getKeystorePath(WalletQrCodeDetailActivity.this.getFilesDir()));
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
                                AlertDialog.Builder builder = new AlertDialog.Builder(WalletQrCodeDetailActivity.this);
                                LayoutInflater inflater = WalletQrCodeDetailActivity.this.getLayoutInflater();
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
                                AlertDialog.Builder builder = new AlertDialog.Builder(WalletQrCodeDetailActivity.this);
                                LayoutInflater inflater = WalletQrCodeDetailActivity.this.getLayoutInflater();
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


                }
                break;
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == wallet_qr_btn_coppy.getId()) {
            if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
                android.text.ClipboardManager clipboard = (android.text.ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
                clipboard.setText(hexAddress);
            } else {
                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
                android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", hexAddress);
                clipboard.setPrimaryClip(clip);
            }
            Toast.makeText(this,"clip to board",Toast.LENGTH_SHORT).show();
        }
        if (v.getId() ==wallet_qr_btn_backup.getId()) {
            pickFolder();
        }
    }
}
