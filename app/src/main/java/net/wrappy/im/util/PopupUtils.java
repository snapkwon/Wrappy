package net.wrappy.im.util;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Html;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import net.wrappy.im.R;

/**
 * Created by hp on 12/18/2017.
 */

public class PopupUtils {
    public static void getSelectionDialog(Context context, String title, ArrayAdapter<String> languagesAdapter, DialogInterface.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setIcon(R.drawable.ic_settings_language);
        builder.setTitle(R.string.KEY_PREF_LANGUAGE_TITLE);
        builder.setAdapter(languagesAdapter, listener);
        builder.show();
    }

    public static void getSelectionDialog(Context context, String title, CharSequence[] options, DialogInterface.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        if (!TextUtils.isEmpty(title))
            builder.setTitle(title);

        builder.setItems(options, listener);

        builder.show();
    }

    public static void getDialog(Context context, String title, String message, int positiveButton, DialogInterface.OnClickListener positiveListener) {
        getDialog(context, title, message, positiveButton, positiveListener, false);
    }

    public static void getDialog(Context context, String title, String message, int positiveButton, DialogInterface.OnClickListener positiveListener, boolean isCancelable) {
        getDialog(context, title, message, positiveButton, -1, -1, positiveListener, null, null, isCancelable);
    }

    public static void getDialog(Context context, String title, String message, int positiveButton, int negativeButton, DialogInterface.OnClickListener positiveListener,
                                 DialogInterface.OnClickListener negativeListener) {
        getDialog(context, title, message, positiveButton, negativeButton, positiveListener, negativeListener, false);
    }

    public static void getDialog(Context context, String title, String message, int positiveButton, int negativeButton, DialogInterface.OnClickListener positiveListener,
                                 DialogInterface.OnClickListener negativeListener, boolean isCancelable) {
        getDialog(context, title, message, positiveButton, negativeButton, -1, positiveListener, negativeListener, null, isCancelable);
    }

    public static void getDialog(Context context, String title, String message, int positiveButton, int negativeButton, int neutralButton, DialogInterface.OnClickListener positiveListener,
                                 DialogInterface.OnClickListener negativeListener, DialogInterface.OnClickListener neutralListener) {
        getDialog(context, title, message, positiveButton, negativeButton, neutralButton, positiveListener,
                negativeListener, neutralListener, false);
    }

    public static void getDialog(Context context, String title, String message, int positiveButton, int negativeButton, int neutralButton, DialogInterface.OnClickListener positiveListener,
                                 DialogInterface.OnClickListener negativeListener, DialogInterface.OnClickListener neutralListener, boolean isCancelable) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(isCancelable);
        builder.setTitle(title);
        builder.setMessage(message);

        if (neutralButton > 0)
            builder.setNeutralButton(neutralButton, neutralListener);
        if (positiveButton > 0)
            builder.setPositiveButton(positiveButton, positiveListener);
        if (negativeButton > 0)
            builder.setNegativeButton(negativeButton, negativeListener);

        AlertDialog alert = builder.create();
        alert.show();
    }

    public static void showCustomDialog(Context context, String title, String message, int resOK, final View.OnClickListener onOkListener) {
        showCustomDialog(context, title, message, resOK, -1, onOkListener, null);
    }

    public static void showCustomDialog(Context context, String title, String message, int resOK, int resCancel, final View.OnClickListener onOkListener, final View.OnClickListener onCancelListener) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View dialogView = inflater.inflate(R.layout.custom_alert_dialog, null);
        builder.setView(dialogView);

        TextView tvTitle = (TextView) dialogView.findViewById(R.id.txtTitle);
        if (!TextUtils.isEmpty(title)) {
            tvTitle.setText(title);
            tvTitle.setVisibility(View.VISIBLE);
        } else tvTitle.setVisibility(View.GONE);

        TextView tvMessage = (TextView) dialogView.findViewById(R.id.txtMessage);
        if (!TextUtils.isEmpty(message)) {
            tvMessage.setText(message);
            tvMessage.setVisibility(View.VISIBLE);
        } else tvMessage.setVisibility(View.GONE);

        Button btnOk = (Button) dialogView.findViewById(R.id.btnOk);
        Button btnCancel = (Button) dialogView.findViewById(R.id.btnCancel);

        final Dialog dialog = builder.show();
        if (resOK > 0) {
            btnOk.setText(resOK);
            btnOk.setVisibility(View.VISIBLE);
            btnOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onOkListener != null)
                        onOkListener.onClick(v);
                    dialog.dismiss();
                }
            });
        } else btnOk.setVisibility(View.GONE);
        if (resCancel > 0) {
            btnCancel.setText(resCancel);
            btnCancel.setVisibility(View.VISIBLE);
            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onCancelListener != null)
                        onCancelListener.onClick(v);
                    dialog.dismiss();
                }
            });
        } else btnCancel.setVisibility(View.GONE);
    }

    public static void showCustomInputPasswordDialog(Context context, String message, int resOK, int resCancel, final View.OnClickListener onOkListener, final View.OnClickListener onCancelListener) {
        showCustomEditDialog(context, message, "", resOK, resCancel, onOkListener, onCancelListener, true);
    }

    public static void showCustomEditDialog(Context context, String message, String data, int resOK, int resCancel, final View.OnClickListener onOkListener, final View.OnClickListener onCancelListener) {
        showCustomEditDialog(context, message, data, resOK, resCancel, onOkListener, onCancelListener, false);
    }

    public static void showCustomEditDialog(Context context, String message, String data, int resOK, int resCancel, final View.OnClickListener onOkListener, final View.OnClickListener onCancelListener, boolean isPassword) {
        showCustomEditDialog(context, "", message, data, resOK, resCancel, onOkListener, onCancelListener, isPassword);
    }

    public static void showCustomEditDialog(Context context, String title, String message, String data, int resOK, int resCancel, final View.OnClickListener onOkListener, final View.OnClickListener onCancelListener, boolean isPassword) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View dialogView = inflater.inflate(R.layout.dialog_with_edittext, null);
        builder.setView(dialogView);

        final EditText txtTitle = (EditText) dialogView.findViewById(R.id.txtTitle);
        if (TextUtils.isEmpty(title)) {
            txtTitle.setVisibility(View.GONE);
        } else {
            txtTitle.setVisibility(View.VISIBLE);
            txtTitle.setText(title);
        }
        final EditText edt = (EditText) dialogView.findViewById(R.id.etinputpass);
        if (isPassword) {
            edt.setHint(Html.fromHtml("<small><i>" + "Input Password" + "</i></small>"));
        } else {
            edt.setInputType(InputType.TYPE_CLASS_TEXT);
            edt.setText(data);
        }

        TextView tvMessage = (TextView) dialogView.findViewById(R.id.txtMessage);
        if (!TextUtils.isEmpty(message)) {
            tvMessage.setText(message);
            tvMessage.setVisibility(View.VISIBLE);
        } else tvMessage.setVisibility(View.GONE);

        Button btnOk = (Button) dialogView.findViewById(R.id.btnOk);
        Button btnCancel = (Button) dialogView.findViewById(R.id.btnCancel);

        final Dialog dialog = builder.show();
        if (resOK > 0) {
            btnOk.setText(resOK);
            btnOk.setVisibility(View.VISIBLE);
            btnOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.setTag(edt.getText().toString());
                    if (onOkListener != null)
                        onOkListener.onClick(v);
                    dialog.dismiss();
                }
            });
        } else btnOk.setVisibility(View.GONE);
        if (resCancel > 0) {
            btnCancel.setText(resCancel);
            btnCancel.setVisibility(View.VISIBLE);
            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onCancelListener != null)
                        onCancelListener.onClick(v);
                    dialog.dismiss();
                }
            });
        } else btnCancel.setVisibility(View.GONE);
    }
}
