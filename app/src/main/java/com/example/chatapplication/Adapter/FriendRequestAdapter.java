package com.example.chatapplication.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatapplication.R;
import com.example.chatapplication.model.userModel;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendRequestAdapter extends RecyclerView.Adapter<FriendRequestAdapter.ViewHolder>{
    android.content.Context context;
    ArrayList<userModel> arrayList;
    AlertDialog.Builder builder;

    public FriendRequestAdapter(Context context, ArrayList<userModel> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public FriendRequestAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.friend_request_layout, parent, false);
        return new FriendRequestAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String userName = position <= getItemCount() ? arrayList.get(position).getName() : "";
        String profilePic = position <= getItemCount() ? arrayList.get(position).getImageID() : "";
        String userId = position <= getItemCount() ? arrayList.get(position).getUserID() : "";
        builder = new AlertDialog.Builder(context);

        holder.name.setText(userName);
        Glide.with(context).load(profilePic).into(holder.dp);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("user").child(FirebaseAuth.getInstance().getUid());
        DatabaseReference friendsRef = FirebaseDatabase.getInstance().getReference().child("user").child(userId);

        holder.check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> ls = new ArrayList<>();

                HashMap<String, Object> map = new HashMap<>();

                map.put(FirebaseAuth.getInstance().getUid(), FirebaseAuth.getInstance().getUid());
                friendsRef.child("friendsList").updateChildren(map);

                HashMap<String, Object> map1 = new HashMap<>();

                map1.put(userId, userId);
                databaseReference.child("friendsList").updateChildren(map1);

                friendsRef.child("pendingRequest").child(FirebaseAuth.getInstance().getUid()).removeValue();

                removeFromFriendsList(userId);
            }
        });

        holder.cross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeFriend(arrayList.get(position));
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
        CircleImageView dp, check, cross;
        TextView name;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            dp = itemView.findViewById(R.id.dp);
            check = itemView.findViewById(R.id.check);
            cross = itemView.findViewById(R.id.cross);
            name = itemView.findViewById(R.id.name);
        }
    }

    private void removeFriend(userModel userModel) {
        builder.setMessage("you want to reject "+userModel.getName()+"'s friend request?")
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        DatabaseReference friendListRef =  FirebaseDatabase.getInstance().getReference().child("user").child(FirebaseAuth.getInstance().getUid());
                        DatabaseReference friendsRef = FirebaseDatabase.getInstance().getReference().child("user").child(userModel.getUserID());

                        friendListRef.child("friendRequest").child(userModel.getUserID()).removeValue();
                        friendsRef.child("pendingReq").child(FirebaseAuth.getInstance().getUid()).removeValue();

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
    }

    void removeFromFriendsList(String userID){
        DatabaseReference friendListRef =  FirebaseDatabase.getInstance().getReference().child("user").child(FirebaseAuth.getInstance().getUid());

        friendListRef.child("friendRequest").child(userID).removeValue();

    }
}
