package edu.shu.icecream;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.ListView;

public class ListPickerDialog {
    private Dialog mDialog;
    private ListView listView;
    private FrameLayout frameLayout;
    private ListAdapter listAdapter;

    public ListPickerDialog(Context context, String[] names, AdapterView.OnItemClickListener listener) {
        mDialog = create(context, names);
        frameLayout = mDialog.findViewById(R.id.fl_dialog);
        frameLayout.setOnClickListener(v -> mDialog.dismiss());

        listView = mDialog.findViewById(R.id.list_view);
        listAdapter = new ArrayAdapter<>(context, R.layout.my_list_dialog_item, names);
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(listener);

    }

    private Dialog create(Context context, String[] names) {
        HideNavigationBarDialog dialog = new HideNavigationBarDialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        View root = LayoutInflater.from(context).inflate(R.layout.dialog_list_picker, null);

        dialog.setContentView(root);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        dialog.getWindow().setAttributes(lp);

        return dialog;
    }

    public void show() {
        mDialog.show();
    }

    public void dismiss() {
        if (mDialog.isShowing() && mDialog != null) {
            mDialog.dismiss();
        }
    }

}
