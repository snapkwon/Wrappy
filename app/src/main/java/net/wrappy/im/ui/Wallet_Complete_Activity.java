package net.wrappy.im.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import net.wrappy.im.GethService.KeyManager;
import net.wrappy.im.GethService.Wallet;
import net.wrappy.im.GethService.WalletInfo;
import net.wrappy.im.R;
import net.wrappy.im.helper.FileUtil;
import net.wrappy.im.util.PopupUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

import me.ydcool.lib.qrmodule.encoding.QrGenerator;

public class Wallet_Complete_Activity extends BaseActivity {

    private Button backup;
    private Button noThanks;
    private final static int PICK_FOLDER_RESULT_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet__complete);
        backup = (Button) this.findViewById(R.id.buttonbackup);
        noThanks = (Button) this.findViewById(R.id.buttonthanks);

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
                Intent intent = new Intent(Wallet_Complete_Activity.this, PackupWalletAccount.class);
                startActivity(intent);
                //  pickFolder();
            }
        });

        // back button at action bar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getResources().getString(R.string.title_new_wallet_created));
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_action_arrow_back);
        }
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

    void pickFolder() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        //  startActivityForResult(intent, REQUEST_CODE_OPEN_DIRECTORY);
        try {
            startActivityForResult(intent, PICK_FOLDER_RESULT_CODE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        switch (requestCode) {
            case PICK_FOLDER_RESULT_CODE: {
                if (resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
                    PopupUtils.showCustomEditDialog(Wallet_Complete_Activity.this, getString(R.string.sub_title_wallet_dialog), R.string.action_done, R.string.cancel,
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Uri treeUri = data.getData();
                                    final String theFolderPath = FileUtil.getFullPathFromTreeUri(treeUri, Wallet_Complete_Activity.this);

                                    File sourceLocation = new File(Wallet.getKeystorePath(Wallet_Complete_Activity.this.getFilesDir()));
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


                                        FileOutputStream out = null;
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

                                        PopupUtils.showCustomDialog(Wallet_Complete_Activity.this, "", getString(R.string.backup_success), R.string.yes, new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                finish();
                                            }
                                        });
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        PopupUtils.showCustomDialog(Wallet_Complete_Activity.this, "", e.toString(), R.string.yes, null);
                                    }
                                }
                            }, null);
                }
                break;
            }
        }
    }

}
