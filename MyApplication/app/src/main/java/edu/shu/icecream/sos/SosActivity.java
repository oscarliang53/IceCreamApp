package edu.shu.icecream.sos;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import edu.shu.icecream.MainApplication;
import edu.shu.icecream.R;
import edu.shu.icecream.databinding.ActivitySosBinding;

public class SosActivity extends AppCompatActivity {
    ActivitySosBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sos);

        setListener();
    }

    private void setListener() {
        binding.btHome.setOnClickListener(v -> finish());
        binding.btPolice.setOnClickListener(v -> callPolice());
        binding.btInjure.setOnClickListener(v -> callAmbulance());
        binding.btLost.setOnClickListener(v -> callDanger());
        binding.btDanger.setOnClickListener(v -> sendDanger());
    }

    private void callPolice() {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + "0925788585"));
        startActivity(intent);
    }

    private void callAmbulance() {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + "0963870922"));
        startActivity(intent);
    }

    private void callDanger() {
        String phone = MainApplication.ins().iceUserData.getEmergencyPhone();
        if (phone == null || phone.isEmpty()) {
            Toast.makeText(this, "請至個人設定新增緊急聯絡人電話", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phone));
        startActivity(intent);
    }

    private void sendDanger() {
        String phone = MainApplication.ins().iceUserData.getEmergencyPhone();
        if (phone == null || phone.isEmpty()) {
            Toast.makeText(this, "請至個人設定新增緊急聯絡人電話", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("smsto:" + phone));
        intent.putExtra("sms_body", "我有危難");
        startActivity(intent);

//        Uri uri = Uri.parse("smsto:0800000123");
//        Intent it = new Intent(Intent.ACTION_SENDTO, uri);
//        it.putExtra("sms_body", "The SMS text");   startActivity(it);
    }
}
