package net.wrappy.im.ui.qr;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;

import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.BeepManager;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import net.wrappy.im.ImApp;
import net.wrappy.im.R;
import net.wrappy.im.ui.onboarding.OnboardingManager;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Custom Scannner Activity extending from Activity to display a custom layout form scanner view.
 */
public class QrScanFriendActivity extends Activity {

    @BindView(R.id.camera_box)
    DecoratedBarcodeView barcodeScannerView;
    @BindView(R.id.layout_main)
    View layoutMain;
    @BindView(R.id.qr_box_image)
    ImageView qrCodeView;
    private final static int MY_PERMISSIONS_REQUEST_CAMERA = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA);

        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA)) {
                finish();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        MY_PERMISSIONS_REQUEST_CAMERA);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            init();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    init();

                } else {
                    finish();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void init() {
        setContentView(R.layout.awesome_activity_scan);
        ButterKnife.bind(this);

        barcodeScannerView.decodeContinuous(callback);

        beepManager = new BeepManager(this);
        String qrData = getIntent().getStringExtra(Intent.EXTRA_TEXT);

        new QrGenAsyncTask(this, qrCodeView, 100).executeOnExecutor(ImApp.sThreadPoolExecutor, qrData);
    }

    private BeepManager beepManager;
    private String lastText;

    private BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (result.getText() == null || result.getText().equals(lastText)) {
                // Prevent duplicate scans
                return;
            }

            lastText = result.getText();
            barcodeScannerView.setStatusText(result.getText());
            beepManager.playBeepSoundAndVibrate();
            handleResult(result);

            //Added preview of scanned barcode
//            qrCodeView.setImageBitmap(result.getBitmapWithResultPoints(Color.YELLOW));
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        barcodeScannerView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        barcodeScannerView.pause();
    }

    public void pause(View view) {
        barcodeScannerView.pause();
    }

    public void resume(View view) {
        barcodeScannerView.resume();
    }

    public void triggerScan(View view) {
        barcodeScannerView.decodeSingle(callback);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return barcodeScannerView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }

    private Intent dataResult = new Intent();
    ArrayList<String> resultStrings = new ArrayList<String>();

    public void handleResult(final BarcodeResult result) {
        String resultString = result.getText();

        if (!resultStrings.contains(resultString)) {

            resultStrings.add(resultString);
            dataResult.putStringArrayListExtra("result", resultStrings);

            OnboardingManager.DecodedInviteLink diLink = OnboardingManager.decodeInviteLink(resultString);

            String message = null;

            if (diLink != null) {

                message = getString(R.string.add_contact_success, diLink.username);
            }

            if (message != null) {
                Snackbar.make(layoutMain, message, Snackbar.LENGTH_LONG).show();
            }

            setResult(RESULT_OK, dataResult);
            finish();
        }
//        runOnUiThread(new Runnable() {
//            public void run() {
//
//
//
//            }
//        });
    }
}
