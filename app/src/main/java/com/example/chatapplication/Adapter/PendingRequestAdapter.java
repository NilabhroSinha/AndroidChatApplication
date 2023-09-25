package com.example.chatapplication.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatapplication.Activity.PendingRequests;
import com.example.chatapplication.R;
import com.example.chatapplication.model.userModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class PendingRequestAdapter extends RecyclerView.Adapter<PendingRequestAdapter.ViewHolder> {
    android.content.Context context;
    ArrayList<userModel> arrayList;
    AlertDialog.Builder builder;

    public PendingRequestAdapter(Context context, ArrayList<userModel> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.pending_request_layout, parent, false);
        return new PendingRequestAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String userName = position <= getItemCount() ? arrayList.get(position).getName() : "";
        String profilePic = position <= getItemCount() ? arrayList.get(position).getImageID() : "";
        String userId = position <= getItemCount() ? arrayList.get(position).getUserID() : "";

        builder = new AlertDialog.Builder(context);

        holder.name.setText(userName);
        Glide.with(context).load(profilePic).into(holder.dp);

        holder.fullrow.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                builder.setMessage("you want to unsend Friend request to "+userName+"?")
                        .setCancelable(true)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("user").child(FirebaseAuth.getInstance().getUid());
                                DatabaseReference friendsRef = FirebaseDatabase.getInstance().getReference().child("user").child(userId);

                                databaseReference.child("pendingRequest").child(userId).removeValue();
                                friendsRef.child("friendRequest").child(FirebaseAuth.getInstance().getUid()).removeValue();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alert = builder.create();
                alert.setTitle("Are you sure");
                alert.show();

                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        if (arrayList != null)
            return arrayList.size();
        else
            return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        CircleImageView dp;
        TextView name;
        LinearLayout fullrow;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            fullrow = itemView.findViewById(R.id.fullrow);
            dp = itemView.findViewById(R.id.dp);
            name = itemView.findViewById(R.id.name);
        }
    }
}
