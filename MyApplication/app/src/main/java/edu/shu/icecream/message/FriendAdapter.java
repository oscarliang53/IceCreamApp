package edu.shu.icecream.message;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.shu.icecream.R;
import edu.shu.icecream.data.IceUserFriendData;
import edu.shu.icecream.databinding.ItemFriendBinding;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.ViewHolder> {
    private List<IceUserFriendData.FriendBean> friendList;
    private Context context;
    OnFriendClickListener listener;

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    int textColor = 0;

    public FriendAdapter(List<IceUserFriendData.FriendBean> list, OnFriendClickListener listener, Context context) {
        this.context = context;
        this.friendList = list;
        this.listener = listener;
    }

    public void updateDatas(List<IceUserFriendData.FriendBean> datas) {
        friendList = datas;
        notifyDataSetChanged();
    }

    public interface OnFriendClickListener {
        void onClicked(String data);
    }

    @NonNull
    @Override
    public FriendAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ItemFriendBinding binding = ItemFriendBinding.inflate(layoutInflater, parent, false);
        return new FriendAdapter.ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendAdapter.ViewHolder holder, int position) {
        holder.setData(friendList.get(position));
    }

    @Override
    public int getItemCount() {
        return friendList == null ? 0 : friendList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ItemFriendBinding binding;

        ViewHolder(ItemFriendBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void setData(IceUserFriendData.FriendBean data) {
            if (textColor > 0) {
                binding.tvName.setTextColor(context.getColor(textColor));
                binding.view.setBackgroundColor(textColor);
            }
            binding.tvName.setText(data.getName());
            binding.llFriend.setOnClickListener(view -> {
                if (listener != null) listener.onClicked(data.getFriendID());
            });

        }
    }
}
