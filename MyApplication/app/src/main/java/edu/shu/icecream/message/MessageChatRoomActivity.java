package edu.shu.icecream.message;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import edu.shu.icecream.MainApplication;
import edu.shu.icecream.MyUtil;
import edu.shu.icecream.R;
import edu.shu.icecream.data.ChatMessage;
import edu.shu.icecream.databinding.ActivityMessageChatRoomBinding;

public class MessageChatRoomActivity extends AppCompatActivity {
    ActivityMessageChatRoomBinding binding;
    List<DocumentSnapshot> msgList = new ArrayList<>();
    String RoomID = "";
    List<ChatMessage> chatMessageList = new ArrayList<>();
    MsgAdapter msgAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_message_chat_room);
        try {
            RoomID = getIntent().getStringExtra("RoomID");
        } catch (Exception ignore) {
        }
        msgAdapter = new MsgAdapter(null, null, this);
        binding.rvMsg.setLayoutManager(new LinearLayoutManager(this));
        binding.rvMsg.setHasFixedSize(true);
        binding.rvMsg.setNestedScrollingEnabled(false);
        binding.rvMsg.setAdapter(msgAdapter);

        if (RoomID != null && RoomID.length() > 0) {
            MainApplication.ins().fireDB.collection("Chat")
                    .document(RoomID)
                    .collection("messages")
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                            if (error != null) Log.i("TTT", "snapshot error " + error.getMessage());
                            if (value != null) {
                                if (value.size() > 0) {
                                    updateMsg(value);
                                }
                            }
                        }
                    });
            binding.btSend.setOnClickListener(v -> sendCheck());
        } else {
            Toast.makeText(this, "發生錯誤", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void updateMsg(QuerySnapshot value) {
        msgList = value.getDocuments();
        chatMessageList.clear();
        for (DocumentSnapshot snapshot : msgList) {
            Log.i("ttt", "get msg : " + snapshot.get("messageText"));
            Log.i("ttt", " : " + snapshot.get("messageUser"));
            Log.i("ttt", " : " + snapshot.get("timestamp"));
            ChatMessage msg = new ChatMessage((String) snapshot.get("messageText"),
                    (String) snapshot.get("messageUser"),
                    (Long) snapshot.get("timestamp"));
            if (msg.getMessageUser().equals(MainApplication.ins().currentUser.getUid())) {
                msg.setMe(true);
            } else {
                msg.setMe(false);
            }
            chatMessageList.add(msg);
        }
        Collections.sort(chatMessageList, (o1, o2) -> (int) (o1.getTimestamp() - o2.getTimestamp()));
        msgAdapter.updateDatas(chatMessageList);
        binding.rvMsg.scrollToPosition(msgAdapter.getItemCount() - 1);
    }

    private void sendCheck() {
        if (binding.input.getText().toString().isEmpty()) {
            return;
        }
        String msg = binding.input.getText().toString();
        binding.input.setText("");
        MyUtil.hideKeyboard(this);
        MainApplication.ins().fireDB.collection("Chat")
                .document(RoomID)
                .collection("messages")
                .add(new ChatMessage(msg,
                        MainApplication.ins().currentUser.getUid(),
                        Calendar.getInstance().getTimeInMillis()))
                .addOnSuccessListener(aVoid -> Log.i("TTT", "send msg ok"))
                .addOnFailureListener(e -> {
                    Log.i("TTT", "send msg fail");
                    Toast.makeText(MessageChatRoomActivity.this, "訊息發送失敗", Toast.LENGTH_SHORT).show();
                });
    }
}
