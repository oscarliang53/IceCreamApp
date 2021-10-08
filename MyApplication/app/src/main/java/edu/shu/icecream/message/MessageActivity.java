package edu.shu.icecream.message;

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

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

import edu.shu.icecream.MainApplication;
import edu.shu.icecream.R;
import edu.shu.icecream.data.IceUserFriendData;
import edu.shu.icecream.databinding.ActivityMessageBinding;

public class MessageActivity extends AppCompatActivity {
    ActivityMessageBinding binding;
    FriendAdapter friendAdapter;
    IceUserFriendData iceUserFriendData;
    List<IceUserFriendData.FriendBean> friendList = new ArrayList<>();
    char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_message);

        friendAdapter = new FriendAdapter(null, new FriendAdapter.OnFriendClickListener() {
            @Override
            public void onClicked(String data) {
                Log.i("TTT", "get id " + data);
                gotoChat(data);
            }
        }, this);
        binding.rvFriend.setLayoutManager(new LinearLayoutManager(this));
        binding.rvFriend.setAdapter(friendAdapter);
        binding.rvFriend.setHasFixedSize(true);
        binding.rvFriend.setNestedScrollingEnabled(false);
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

    private void gotoChat(String data) {
        Intent intent = new Intent(this, MessageChatRoomActivity.class);
        intent.putExtra("RoomID", getRoomID(data));
        startActivity(intent);
    }

    private String getRoomID(String targetID) {
        String myID = MainApplication.ins().currentUser.getUid();
        String str = targetID.compareTo(myID) >= 0 ? targetID + myID : myID + targetID;
        Log.i("TTT", "str = " + str);
        StringBuffer sb = new StringBuffer();
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(str.getBytes("UTF-8"));
            //Hash計算, 產生128位的長整數
            byte[] bytes = messageDigest.digest();
            sb = new StringBuffer(bytes.length * 2);
            for (Byte b : bytes) {
                //右移四位, 取字節中前四位轉換
                sb.append(hexDigits[(b >> 4) & 0x0f]);
                //取字節中後四位轉換
                sb.append(hexDigits[b & 0x0f]);
            }
            //輸出 602965cf9dd0e80ca28269257a6aba87
            System.out.println(sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return sb.toString();
    }

}
