package com.example.hwbluetooth;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {

    private OnItemClick onItemClick;
    private List<ScannedData> arrayList = new ArrayList<>();
    public ItemAdapter(OnItemClick onItemClick) {
        this.onItemClick = onItemClick;
    }
    public void clearDevice(){
        this.arrayList.clear();
        notifyDataSetChanged();
    }
    public void addDevice(List<ScannedData> arrayList){
        this.arrayList = arrayList;
        notifyDataSetChanged();
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAddress,tvRssi;
        Button Detail;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAddress = itemView.findViewById(R.id.textView_Address);
            tvRssi = itemView.findViewById(R.id.textView_Rssi);
            Detail = itemView.findViewById(R.id.detail);
        }
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.scanned_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tvAddress.setText("裝置位址："+arrayList.get(position).getAddress());
        holder.tvRssi.setText("訊號強度："+arrayList.get(position).getRssi());
        holder.Detail.setOnClickListener(v -> {
            onItemClick.onItemClick(arrayList.get(position));
        });
    }
    @Override
    public int getItemCount() {
        return arrayList.size();
    }
    interface OnItemClick{
        void onItemClick(ScannedData currentDevice);//按一下得知當前device
    }



}

