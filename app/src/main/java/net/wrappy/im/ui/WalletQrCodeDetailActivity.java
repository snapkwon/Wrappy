package net.wrappy.im.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import net.wrappy.im.GethService.KeyManager;
import net.wrappy.im.GethService.Wallet;
import net.wrappy.im.GethService.WalletInfo;
import net.wrappy.im.R;
import net.wrappy.im.helper.AppFuncs;
import net.wrappy.im.helper.FileUtil;
import net.wrappy.im.util.PopupUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

import me.ydcool.lib.qrmodule.encoding.QrGenerator;

public class WalletQrCodeDetailActivity extends AppCompatActivity implements View.OnClickListener {

    ImageView wallet_qr_img;
    TextView wallet_qr_address_text;
    Button wallet_qr_btn_coppy, wallet_qr_btn_backup;
    String hexAddress;
    private final static int PICK_FOLDER_RESULT_CODE = 1000;

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
            KeyManager keyManager = KeyManager.newKeyManager(getApplicationContext().getFilesDir().getAbsolutePath() + WalletInfo.KEYSTORE_PATH);
            hexAddress = keyManager.getKeystore().getAccounts().get(0).getAddress().getHex();

            wallet_qr_address_text.setText(hexAddress);
            Bitmap qrCode = new QrGenerator.Builder()
                    .content(hexAddress)
                    .qrSize(140)
                    .margin(2)
                    .color(Color.BLACK)
                    .bgColor(Color.WHITE)
                    .ecc(ErrorCorrectionLevel.L)
                    .overlayAlpha(255)
                    .overlayXfermode(PorterDuff.Mode.SRC)
                    .encode();

            wallet_qr_img.setImageBitmap(qrCode);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // back button at action bar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getResources().getString(R.string.title_wallet_address));
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_action_arrow_back);
        }
    }

    void pickFolder() {
       /* Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        //  startActivityForResult(intent, REQUEST_CODE_OPEN_DIRECTORY);
        try {
            startActivityForResult(intent,PICK_FOLDER_RESULT_CODE);
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        Intent intent = new Intent(this, PackupWalletAccount.class);
        startActivity(intent);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_wallet_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.removewallet) {
            PopupUtils.showCustomInputPasswordDialog(WalletQrCodeDetailActivity.this, getString(R.string.sub_title_wallet_dialog), R.string.action_done, R.string.cancel,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            KeyManager keyManager = KeyManager.newKeyManager(getApplicationContext().getFilesDir().getAbsolutePath() + WalletInfo.KEYSTORE_PATH);

                            try {
                                keyManager.getKeystore().deleteAccount(keyManager.getKeystore().getAccounts().get(0), String.valueOf(v.getTag()));
                                //  FileUtil.copyDirectory(sourceLocation,targetLocation);
                                PopupUtils.showCustomDialog(WalletQrCodeDetailActivity.this, "", getString(R.string.delete_success), R.string.yes, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        finish();
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                                PopupUtils.showCustomDialog(WalletQrCodeDetailActivity.this, "", e.toString(), R.string.yes, null);
                            }

                        }
                    }, null);
        } else if (id == R.id.changepasswallet) {
            Intent intent = new Intent(this, ChangePasswordAccount.class);
            this.startActivity(intent);
        } else if (id == android.R.id.home) {
            this.finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        switch (requestCode) {
            case PICK_FOLDER_RESULT_CODE: {
                if (resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
                    PopupUtils.showCustomInputPasswordDialog(WalletQrCodeDetailActivity.this, getString(R.string.sub_title_wallet_dialog), R.string.action_done, R.string.cancel,
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Uri treeUri = data.getData();
                                    final String theFolderPath = FileUtil.getFullPathFromTreeUri(treeUri, WalletQrCodeDetailActivity.this);

                                    File sourceLocation = new File(Wallet.getKeystorePath(WalletQrCodeDetailActivity.this.getFilesDir()));
                                    KeyManager keyManager = KeyManager.newKeyManager(getApplicationContext().getFilesDir().getAbsolutePath() + WalletInfo.KEYSTORE_PATH);
                                    File targetLocation = new File(theFolderPath);

                                    try {
                                        String jsondata = new String(keyManager.getKeystore().exportKey(keyManager.getKeystore().getAccounts().get(0), String.valueOf(v.getTag()), String.valueOf(v.getTag())), "UTF-8");

                                        Bitmap qrCode = new QrGenerator.Builder()
                                                .content(jsondata)
                                                .qrSize(1024)
                                                .margin(2)
                                                .color(Color.BLACK)
                                                .bgColor(Color.WHITE)
                                                .ecc(ErrorCorrectionLevel.L)
                                                .overlayAlpha(255)
                                                .overlayXfermode(PorterDuff.Mode.SRC)
                                                .encode();


                                        FileOutputStream out;
                                        try {
                                            Calendar cal = Calendar.getInstance();
                                            String filename = "UTC--" + cal.getTime().toString() + "--" + keyManager.getAccounts().get(0).getAddress().getHex();

                                            File dest = new File(targetLocation, filename + ".png");
                                            out = new FileOutputStream(dest);
                                            qrCode.compress(Bitmap.CompressFormat.PNG, 90, out);
                                            out.flush();
                                            out.close();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }

                                        //  FileUtil.copyDirectory(sourceLocation,targetLocation);
                                        PopupUtils.showCustomDialog(WalletQrCodeDetailActivity.this, "", getString(R.string.backup_success), R.string.yes, new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                finish();
                                            }
                                        });
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        PopupUtils.showCustomDialog(WalletQrCodeDetailActivity.this, "", e.toString(), R.string.yes, null);
                                    }

                                }
                            }, null);
                }
                break;
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == wallet_qr_btn_coppy.getId()) {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText(getString(R.string.copied_text), hexAddress);
            clipboard.setPrimaryClip(clip);
            AppFuncs.alert(this, R.string.copy_clipboard, false);
        }
        if (v.getId() == wallet_qr_btn_backup.getId()) {
            pickFolder();
        }
    }
}
