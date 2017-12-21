package net.wrappy.im.ui.qr;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.encode.Contents;
import com.google.zxing.encode.QRCodeEncoder;

import java.lang.ref.WeakReference;

public class QrGenAsyncTask extends AsyncTask<String, Void, Void> {
    private static final String TAG = "QrGenAsyncTask";

    private ImageView view;
    private Bitmap qrBitmap;
    private int qrCodeDimension;
    private WeakReference<Activity> weakReference;

    public QrGenAsyncTask(Activity activity, ImageView view, int qrCodeDimension) {
        this.view = view;
        this.qrCodeDimension = qrCodeDimension;
        weakReference = new WeakReference<>(activity);
    }

    /*
     * The method for getting screen dimens changed, so this uses both the
     * deprecated one and the 13+ one, and supports all Android versions.
     */
    @SuppressWarnings("deprecation")
    @TargetApi(13)
    @Override
    protected Void doInBackground(String... s) {
        String qrData = s[0];
        /*
        //Display display = activity.getWindowManager().getDefaultDisplay();

        Point outSize = new Point();
        int x, y, qrCodeDimension;
        if (Build.VERSION.SDK_INT >= 13) {
            view.getSize(outSize);
            x = outSize.x;
            y = outSize.y;
        } else {
            x = display.getWidth();
            y = display.getHeight();
        }
        if (x < y)
            qrCodeDimension = x;
        else
            qrCodeDimension = y;
        **/

        //      Log.i(TAG, "generating QRCode Bitmap of " + qrCodeDimension + "x" + qrCodeDimension);
        QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(qrData, null,
                Contents.Type.TEXT, BarcodeFormat.QR_CODE.toString(), qrCodeDimension);

        try {
            qrBitmap = qrCodeEncoder.encodeAsBitmap();
        } catch (WriterException e) {
            Log.e(TAG, e.getMessage());
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void v) {

        // If the generation takes too long for whatever reason, then this view, and indeed the entire
        // activity may not be around any more.
        if (weakReference.get() != null && view != null) {
            view.setImageBitmap(qrBitmap);
        }
    }
}