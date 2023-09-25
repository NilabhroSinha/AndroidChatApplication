package com.example.chatapplication.Adapter;

import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatapplication.Activity.viewProfile;
import com.example.chatapplication.R;
import com.example.chatapplication.model.PopupModel;

import java.util.ArrayList;
import java.util.Set;

import javax.sql.StatementEvent;

import de.hdodenhof.circleimageview.CircleImageView;

public class PopupActivityAdapter extends RecyclerView.Adapter<PopupActivityAdapter.ViewHolder> {

    Context context;
    ArrayList<PopupModel> arrayList;

    public PopupActivityAdapter(Context context, ArrayList<PopupModel> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public PopupActivityAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.pending_request_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PopupModel details = arrayList.get(position);
        Glide.with(context).load(details.getImage()).into(holder.dp);
        holder.name.setText(details.getName());

        holder.pending.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        CircleImageView dp;
        TextView name, pending;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            pending = itemView.findViewById(R.id.pending);
            dp = itemView.findViewById(R.id.dp);
            name = itemView.findViewById(R.id.name);

        }
    }
}
