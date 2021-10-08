package edu.shu.icecream.navigation;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.shu.icecream.ListPickerDialog;
import edu.shu.icecream.MainApplication;
import edu.shu.icecream.MyUtil;
import edu.shu.icecream.R;
import edu.shu.icecream.data.IceUserFriendData;
import edu.shu.icecream.databinding.ActivityNavigationBinding;
import edu.shu.icecream.message.FriendAdapter;

public class NavigationActivity extends AppCompatActivity {
    ActivityNavigationBinding binding;
    FriendAdapter friendAdapter;
    IceUserFriendData iceUserFriendData;
    List<IceUserFriendData.FriendBean> friendList = new ArrayList<>();

    //    boolean task1 = false, task2 = false;
    ListPickerDialog listPickerDialog;
    String[] items = new String[]{"設定好友目標", "前往好友設定之目標"};
    String targetID;
    String targetLocation;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_navigation);

        friendAdapter = new FriendAdapter(null, new FriendAdapter.OnFriendClickListener() {
            @Override
            public void onClicked(String data) {
                Log.i("TTT", "get id " + data);
                getData(data);
            }
        }, this);
        binding.rvFriend.setLayoutManager(new LinearLayoutManager(this));
        binding.rvFriend.setAdapter(friendAdapter);
        binding.rvFriend.setHasFixedSize(true);
        binding.rvFriend.setNestedScrollingEnabled(false);
        friendAdapter.setTextColor(R.color.green);

        listPickerDialog = new ListPickerDialog(this, items, new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                actionSelected(position);
                listPickerDialog.dismiss();
            }
        });
        getFriend();
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
                                if (iceUserFriendData.getFriendBeans().size() > 0) {
                                    setData();
                                }
                            } else {
                                Log.d("TAG", "No such document");
                            }
                        } else {
                            Log.d("TAG", "get failed with ", task.getException());
                        }
                    }
                });
    }

    private void setData() {
        friendList = iceUserFriendData.getFriendBeans();
        friendAdapter.updateDatas(friendList);
    }

    private void getData(String data) {
        targetID = data;
        targetLocation = "";
        String key_toTarget = MainApplication.ins().currentUser.getUid() + data;
        String key_fromTarget = data + MainApplication.ins().currentUser.getUid();
        //查詢
//        task1 = false;
//        task2 = false;
//        MainApplication.ins().fireDB.collection("NavigationTarget").document(key_toTarget).get()
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        DocumentSnapshot document = task.getResult();
//                        if (document.exists()) {
//                            Log.d("TAG", "DocumentSnapshot data: " + document.getData());
//                        } else {
//                            Log.d("TAG", "No such document");
//                        }
//                        task1 = true;
//                        checkAndShow();
//                    } else {
//                        Log.d("TAG", "get failed with ", task.getException());
//                    }
//                });
        MainApplication.ins().fireDB.collection("NavigationTarget").document(key_fromTarget).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Log.d("TAG", "DocumentSnapshot data: " + document.getData());
                            Map<String, Object> params = document.getData();
                            targetLocation = (String) params.get("target");
                        } else {
                            Log.d("TAG", "No such document");
                        }
//                        task2 = true;
                        checkAndShow();
                    } else {
                        Log.d("TAG", "get failed with ", task.getException());
                    }
                });

    }

    private void checkAndShow() {
//        if (task2) {
        listPickerDialog.show();
//        }
    }

    private void actionSelected(int position) {
        switch (position) {
            case 0:
                MyUtil.showEditDialog(this, "請輸入目的地", "確定", result -> callSetTarget(result));
                break;
            case 1:
                if (targetLocation == null || targetLocation.isEmpty()) {
                    Toast.makeText(NavigationActivity.this, "對方尚未設定目的地", Toast.LENGTH_SHORT).show();
                } else {
                    //開始導航
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=" + targetLocation));
                    startActivity(intent);
                }
                break;
        }
    }

    private void callSetTarget(String result) {
        if (targetID == null || targetID.isEmpty()) {
            return;
        }
        Map<String, String> params = new HashMap<>();
        params.put("target", result);
        String key_toTarget = MainApplication.ins().currentUser.getUid() + targetID;
        MainApplication.ins().fireDB.collection("NavigationTarget")
                .document(key_toTarget)
                .set(params).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(NavigationActivity.this, "設定完成", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
