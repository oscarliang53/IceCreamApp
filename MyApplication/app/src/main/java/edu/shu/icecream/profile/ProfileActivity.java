package edu.shu.icecream.profile;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import net.glxn.qrgen.android.QRCode;

import edu.shu.icecream.LoginActivity;
import edu.shu.icecream.MainApplication;
import edu.shu.icecream.MyUtil;
import edu.shu.icecream.R;
import edu.shu.icecream.data.IceUserData;
import edu.shu.icecream.data.IceUserFriendData;
import edu.shu.icecream.databinding.ActivityProfileBinding;

public class ProfileActivity extends AppCompatActivity {
    ActivityProfileBinding binding;
    Bitmap qrCodeBitmap;
    final static int CAMERA_PERMISSION_REQUEST = 382;
    IceUserFriendData iceUserFriendData, targetUserFriendData;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_profile);

        binding.btHome.setOnClickListener(view -> {
            MyUtil.hideKeyboard(this);
            finish();
        });
        binding.btSave.setOnClickListener(v -> {
            MyUtil.hideKeyboard(this);
            callSave();
        });
        binding.btLogout.setOnClickListener(v -> {
            MyUtil.hideKeyboard(this);
            logout();
        });
        binding.btShowQr.setOnClickListener(v -> binding.ivQrCode.setVisibility((binding.ivQrCode.getVisibility() == View.VISIBLE) ? View.GONE : View.VISIBLE));
        binding.btAddFriend.setOnClickListener(v -> checkCameraPermission());

        qrCodeBitmap = QRCode.from(MainApplication.ins().iceUserData.getUserID() + ";" + MainApplication.ins().iceUserData.getName()).bitmap();
        binding.ivQrCode.setImageBitmap(qrCodeBitmap);
        getInfo();
        getFriend();
    }

    private void getInfo() {
        MainApplication.ins().fireDB.collection("UserInfo").document(MainApplication.ins().currentUser.getUid()).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Log.d("TAG", "DocumentSnapshot data: " + document.getData());
                            MainApplication.ins().iceUserData = MainApplication.ins().mGson.fromJson(MainApplication.ins().mGson.toJson(document.getData()), IceUserData.class);
                            setData();
                        } else {
                            Log.d("TAG", "No such document");
                        }
                    } else {
                        Log.d("TAG", "get failed with ", task.getException());
                    }
                });
        String photoUrl = MainApplication.ins().currentUser.getPhotoUrl().toString();
        photoUrl = photoUrl + "?height=500";
        Glide.with(this).load(photoUrl).apply(RequestOptions.bitmapTransform(new CircleCrop()).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE)).into(binding.ivProfile);

        MainApplication.ins().fireDB.collection("UserInfo").document(MainApplication.ins().currentUser.getUid())
                .update("photoUrl", MainApplication.ins().currentUser.getPhotoUrl().toString());
    }

    private void setData() {
        binding.etName.setText(MainApplication.ins().iceUserData.getName());
        binding.etTelephone.setText(MainApplication.ins().iceUserData.getTelephone());
        binding.etAddress.setText(MainApplication.ins().iceUserData.getAddress());
        binding.etEmergency.setText(MainApplication.ins().iceUserData.getEmergency());
        binding.etEmergencyPhone.setText(MainApplication.ins().iceUserData.getEmergencyPhone());
    }

    private void getFriend() {
        MainApplication.ins().fireDB.collection("UserFriend").document(MainApplication.ins().currentUser.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Log.d("TAG", "DocumentSnapshot data: " + document.getData());
                                iceUserFriendData = MainApplication.ins().mGson.fromJson(MainApplication.ins().mGson.toJson(document.getData()), IceUserFriendData.class);
                                Log.d("TAG", "friend size : " + iceUserFriendData.getFriendBeans().size());
                                binding.tvFriendCount.setText(String.valueOf(iceUserFriendData.getFriendBeans().size()));
                            } else {
                                Log.d("TAG", "No such document");
                            }
                        } else {
                            Log.d("TAG", "get failed with ", task.getException());
                        }
                    }
                });
    }

    private void callAddFriend(String targetUid, String name) {
        MyUtil.hideKeyboard(this);
        Log.i("TAG", "get result : " + name);
        boolean iscontain = false;
        for (IceUserFriendData.FriendBean friendBean : iceUserFriendData.getFriendBeans()) {
            if (friendBean.getFriendID().equals(targetUid)) {
                iscontain = true;
            }
        }
        if (iscontain) {
            Toast.makeText(this, "已經是好友", Toast.LENGTH_SHORT).show();
        } else {
            IceUserFriendData.FriendBean newFriend = new IceUserFriendData.FriendBean();
            newFriend.setName(name);
            newFriend.setFriendID(targetUid);
            iceUserFriendData.getFriendBeans().add(newFriend);
            MainApplication.ins().fireDB.collection("UserFriend").document(MainApplication.ins().currentUser.getUid())
                    .set(iceUserFriendData).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(ProfileActivity.this, "好友資料更新成功", Toast.LENGTH_SHORT).show();
                    getFriend();
                    updateTargetFriendList(targetUid);
                }
            });
        }
    }

    private void updateTargetFriendList(String targetUid) {
        MainApplication.ins().fireDB.collection("UserFriend").document(targetUid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Log.d("TAG", "DocumentSnapshot data: " + document.getData());
                                targetUserFriendData = MainApplication.ins().mGson.fromJson(MainApplication.ins().mGson.toJson(document.getData()), IceUserFriendData.class);
                                Log.d("TAG", "friend size : " + targetUserFriendData.getFriendBeans().size());
                                callAddTargetsFriend(targetUid);
                            } else {
                                Log.d("TAG", "No such document");
                            }
                        } else {
                            Log.d("TAG", "get failed with ", task.getException());
                        }
                    }
                });

    }

    private void callAddTargetsFriend(String targetUid) {
        boolean iscontain = false;
        for (IceUserFriendData.FriendBean friendBean : targetUserFriendData.getFriendBeans()) {
            if (friendBean.getFriendID().equals(MainApplication.ins().currentUser.getUid())) {
                iscontain = true;
            }
        }
        if (!iscontain) {
            IceUserFriendData.FriendBean newFriend = new IceUserFriendData.FriendBean();
            newFriend.setName(MainApplication.ins().iceUserData.getName());
            newFriend.setFriendID(MainApplication.ins().iceUserData.getUserID());
            targetUserFriendData.getFriendBeans().add(newFriend);
            MainApplication.ins().fireDB.collection("UserFriend").document(targetUid)
                    .set(targetUserFriendData).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(ProfileActivity.this, "對方好友資料更新成功", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void callSave() {
        MainApplication.ins().fireDB.collection("UserInfo").document(MainApplication.ins().currentUser.getUid())
                .update("telephone", binding.etTelephone.getText().toString(),
                        "address", binding.etAddress.getText().toString(),
                        "name", binding.etName.getText().toString(),
                        "emergency", binding.etEmergency.getText().toString(),
                        "emergencyPhone", binding.etEmergencyPhone.getText().toString())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(ProfileActivity.this, "資料更新成功", Toast.LENGTH_SHORT).show();
                        getInfo();
                    }
                });

    }

    private void logout() {
        MainApplication.ins().mAuth.signOut();
        MainApplication.ins().currentUser = null;
        LoginManager.getInstance().logOut();//fb登出
        setResult(RESULT_OK);
        finish();
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
        } else {
            Intent intent = new Intent(this, AddFriendActivity.class);
            startActivityForResult(intent, 456);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkCameraPermission();
            } else {
                Toast.makeText(this, "取得授權失敗，無法存取相機", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 456) {
            Log.i("Tag", "get id : " + data.getStringExtra("qrcode"));
            try {
                String[] results = data.getStringExtra("qrcode").split(";");
                MyUtil.showEditDialog(this, "請輸入暱稱", "確定", result -> callAddFriend(results[0], result));
            } catch (Exception ignore) {
            }
        }
    }
}
