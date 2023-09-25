package com.example.chatapplication.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatapplication.Activity.ChatActivity;
import com.example.chatapplication.R;
import com.example.chatapplication.model.AESUtils;
import com.example.chatapplication.model.MessageModel;
import com.example.chatapplication.model.userModel;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    Context context;
    ArrayList<userModel> arrayList;
    AlertDialog.Builder builder;

    public RecyclerViewAdapter(Context context, ArrayList<userModel> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    public RecyclerViewAdapter() {
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chat_item_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        builder = new AlertDialog.Builder(context);

        userModel userModel = arrayList.get(position);

        FirebaseDatabase.getInstance().getReference().child("chats")
                .child(FirebaseAuth.getInstance().getUid() + userModel.getUserID())
                .child("message")
                .orderByChild("timeStamp")
                .limitToLast(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.hasChildren()){
                            for(DataSnapshot snapshot1: snapshot.getChildren()){
                                if(!snapshot1.exists())
                                    break;

                                String msg = snapshot1.child("message").getValue(String.class);
                                try {
                                  msg = AESUtils.decrypt(snapshot1.child("message").getValue(String.class));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                if(msg != null)
                                    holder.message.setText(msg);
                                else
                                    holder.message.setText("Sent a Photo");

                                long timestamp = (long) snapshot1.child("timeStamp").getValue();
                                SimpleDateFormat sfd = new SimpleDateFormat("hh:mm");

                                holder.time.setText(sfd.format(new Date(timestamp)));

                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        holder.name.setText(userModel.getName());
        Glide.with(context).load(arrayList.get(position).getImageID()).into(holder.circleImageView);
        if(userModel.getOnlineStatus() != null && userModel.getOnlineStatus().equals("Online")){
            holder.onlineStatus.setImageResource(R.drawable.online);
        }
        else{
            holder.onlineStatus.setImageResource(R.drawable.offline);
        }

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                removeFriend(userModel);
                return false;
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("name", userModel.getName());
                intent.putExtra("ReceiverImage", userModel.getImageID());
                intent.putExtra("uid", userModel.getUserID());
                intent.putExtra("status", userModel.getStatus());
                intent.putExtra("email", userModel.getEmail());
                intent.putExtra("birthday", userModel.getBirthday());
                context.startActivity(intent);
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

        CircleImageView circleImageView, onlineStatus;
        TextView name, message, time;
        View mView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            circleImageView = itemView.findViewById(R.id.profile_image);
            name = itemView.findViewById(R.id.name);
            message = itemView.findViewById(R.id.message);
            time = itemView.findViewById(R.id.time);
            mView = itemView;
            onlineStatus = itemView.findViewById(R.id.onlineStatus);

        }
    }

    private void removeFriend(userModel userModel) {
        builder.setMessage("Do you want to unfriend "+userModel.getName()+"?")
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        DatabaseReference friendListRef =  FirebaseDatabase.getInstance().getReference().child("user").child(FirebaseAuth.getInstance().getUid()).child("friendsList");
                        DatabaseReference otherRef =  FirebaseDatabase.getInstance().getReference().child("user").child(userModel.getUserID()).child("friendsList");

                        friendListRef.child(userModel.getUserID()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                otherRef.child(FirebaseAuth.getInstance().getUid()).removeValue();
                                Toast.makeText(context, "You just unfriended "+userModel.getName()+" :(", Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert = builder.create();
        alert.setTitle("Unfriend " + userModel.getName());
        alert.show();
    }
}
