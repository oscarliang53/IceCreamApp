package edu.shu.icecream.rollcall;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.shu.icecream.data.RollCallData;
import edu.shu.icecream.databinding.ItemCalendarBinding;

public class RollCallAdapter extends RecyclerView.Adapter<RollCallAdapter.ViewHolder> {
    private List<RollCallData> friendList;
    private Context context;
    OnFriendClickListener listener;

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    int textColor = 0;

    public RollCallAdapter(List<RollCallData> list, OnFriendClickListener listener, Context context) {
        this.context = context;
        this.friendList = list;
        this.listener = listener;
    }

    public void updateDatas(List<RollCallData> datas) {
        friendList = datas;
        notifyDataSetChanged();
    }

    public interface OnFriendClickListener {
        void onClicked(RollCallData data);
    }

    @NonNull
    @Override
    public RollCallAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ItemCalendarBinding binding = ItemCalendarBinding.inflate(layoutInflater, parent, false);
        return new RollCallAdapter.ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RollCallAdapter.ViewHolder holder, int position) {
        holder.setData(friendList.get(position));
    }

    @Override
    public int getItemCount() {
        return friendList == null ? 0 : friendList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ItemCalendarBinding binding;

        ViewHolder(ItemCalendarBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void setData(RollCallData data) {
            binding.tvDate.setText(data.getDate());
            binding.tvAbsent.setVisibility(data.isCheck() ? View.VISIBLE : View.GONE);
        }
    }
}
