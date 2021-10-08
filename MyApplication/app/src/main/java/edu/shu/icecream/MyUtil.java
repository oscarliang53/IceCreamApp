package edu.shu.icecream;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Html;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MyUtil {
    public static SimpleDateFormat HHMMClientFormat = new SimpleDateFormat("HH:mm");

    public static void hideKeyboard(Activity activity) {
        InputMethodManager inputManager = (InputMethodManager) activity
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        // check if no view has focus:
        View currentFocusedView = activity.getCurrentFocus();
        if (currentFocusedView != null) {
            inputManager.hideSoftInputFromWindow(currentFocusedView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public interface OnOKClickResultListener {
        void OnOKClickResult(String result);
    }
    public static AlertDialog showEditDialog(Activity activity, String title, String positive, OnOKClickResultListener onOKClickResultListener) {
        LinearLayout editLayout = (LinearLayout) activity.getLayoutInflater().inflate(R.layout.edittext_dialog, null);
        EditText editText = editLayout.findViewById(R.id.edit_text);
        AlertDialog alertDialog = new AlertDialog.Builder(activity)
                .setTitle(title)
                .setView(editLayout)
                .setPositiveButton(Html.fromHtml("<font color='#007AFF'>" + positive + "</font>"), null)
                .setNegativeButton(Html.fromHtml("<font color='#007AFF'>取消</font>"), null)
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                    }
                }).create();
        alertDialog.setCanceledOnTouchOutside(false);//不可點外部
        alertDialog.setCancelable(false);//不可用返回鍵
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String res = editText.getText().toString();
                if (res.length() > 0) {
                    if (onOKClickResultListener != null) {
                        onOKClickResultListener.OnOKClickResult(editText.getText().toString());
                    }
                    alertDialog.dismiss();
                } else {
                    Toast.makeText(activity, "請輸入暱稱", Toast.LENGTH_SHORT).show();
                }

            }
        });
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        return alertDialog;
    }
}
