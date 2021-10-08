package edu.shu.icecream.message;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.shu.icecream.MyUtil;
import edu.shu.icecream.data.ChatMessage;
import edu.shu.icecream.data.IceUserFriendData;
import edu.shu.icecream.databinding.ItemMsgBinding;
import edu.shu.icecream.databinding.ItemMsgBinding;

public class MsgAdapter extends RecyclerView.Adapter<MsgAdapter.ViewHolder> {
    private List<ChatMessage> friendList;
    private Context context;
    OnFriendClickListener listener;

    MsgAdapter(List<ChatMessage> list, OnFriendClickListener listener, Context context) {
        this.context = context;
        this.friendList = list;
        this.listener = listener;
    }

    void updateDatas(List<ChatMessage> datas) {
        friendList = datas;
        notifyDataSetChanged();
    }

    public interface OnFriendClickListener {
        void onClicked(String data);
    }

    @NonNull
    @Override
    public MsgAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ItemMsgBinding binding = ItemMsgBinding.inflate(layoutInflater, parent, false);
        return new MsgAdapter.ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MsgAdapter.ViewHolder holder, int position) {
        holder.setData(friendList.get(position));
    }

    @Override
    public int getItemCount() {
        return friendList == null ? 0 : friendList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ItemMsgBinding binding;

        ViewHolder(ItemMsgBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void setData(ChatMessage data) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            if(!data.isMe()){
                params.gravity = Gravity.START;
                binding.llMsg.setLayoutParams(params);
                binding.tvTarget.setVisibility(View.VISIBLE);
                binding.tvTarget.setText(data.getMessageText());
                binding.tvMe.setVisibility(View.GONE);
            }else{
                params.gravity = Gravity.END;
                binding.llMsg.setLayoutParams(params);
                binding.tvMe.setVisibility(View.VISIBLE);
                binding.tvMe.setText(data.getMessageText());
                binding.tvTarget.setVisibility(View.GONE);
            }
            binding.tvTime.setText(MyUtil.HHMMClientFormat.format(data.getTimestamp()));
        }
    }
}
