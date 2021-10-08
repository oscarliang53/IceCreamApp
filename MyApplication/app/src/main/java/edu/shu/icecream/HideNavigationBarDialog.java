package edu.shu.icecream;

import android.app.Dialog;
import android.content.Context;
import android.view.WindowManager;

import androidx.annotation.NonNull;

public class HideNavigationBarDialog extends Dialog {

    public HideNavigationBarDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    public void show() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        super.show();
    }
}
