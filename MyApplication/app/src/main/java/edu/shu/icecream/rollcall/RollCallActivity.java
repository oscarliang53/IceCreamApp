package edu.shu.icecream.rollcall;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import edu.shu.icecream.MainApplication;
import edu.shu.icecream.R;
import edu.shu.icecream.data.IceUserFriendData;
import edu.shu.icecream.databinding.ActivityRollCallBinding;
import edu.shu.icecream.message.FriendAdapter;
import edu.shu.icecream.route.RouteDetailActivity;

public class RollCallActivity extends AppCompatActivity {
    ActivityRollCallBinding binding;
    FriendAdapter friendAdapter;
    IceUserFriendData iceUserFriendData;
    List<IceUserFriendData.FriendBean> friendList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_roll_call);

        friendAdapter = new FriendAdapter(null, new FriendAdapter.OnFriendClickListener() {
            @Override
            public void onClicked(String data) {
                Log.i("TTT", "get id " + data);
                gotoRollCall(data);
            }
        }, this);
        binding.rvFriend.setLayoutManager(new LinearLayoutManager(this));
        binding.rvFriend.setAdapter(friendAdapter);
        binding.rvFriend.setHasFixedSize(true);
        binding.rvFriend.setNestedScrollingEnabled(false);
        friendAdapter.setTextColor(R.color.purple);
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

    private void gotoRollCall(String data) {
        Intent intent = new Intent(this, RollCallDetailActivity.class);
        intent.putExtra("targetID", data);
        startActivity(intent);
    }
}
