package com.example.chatapplication.Adapter;

import android.content.Context;
import android.util.Log;
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
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder>{
    private final Context context;
    private List<userModel> arrayList = new ArrayList<>();
    HashSet<String> finalNameSet, pendingNameSet;

    public SearchAdapter(Context context, HashSet<String> finalNameSet, HashSet<String> pendingNameSet) {
        this.context = context;
        this.finalNameSet = finalNameSet;
        this.pendingNameSet = pendingNameSet;
    }

    public void filterList(ArrayList<userModel> filterlist) {
        // below line is to add our filtered
        // list in our course array list.
        arrayList = filterlist;
        // below line is to notify our adapter
        // as change in recycler view data.
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_chat_item_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String userName = position <= getItemCount() ? arrayList.get(position).getName() : "";
        String profilePic = position <= getItemCount() ? arrayList.get(position).getImageID() : "";
        String userId = position <= getItemCount() ? arrayList.get(position).getUserID() : "";

        holder.name.setText(userName);
        Glide.with(context).load(profilePic).into(holder.dp);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("user").child(FirebaseAuth.getInstance().getUid());
        DatabaseReference friendReqRef = FirebaseDatabase.getInstance().getReference().child("user").child(userId);

        if(finalNameSet.contains(userId)){
            holder.add.setVisibility(View.GONE);
            holder.check.setVisibility(View.VISIBLE);
        }
        else if(pendingNameSet.contains(userId)){
            holder.add.setVisibility(View.VISIBLE);
            holder.check.setVisibility(View.GONE);
            holder.add.setText("Pending");
        }
        else if(!finalNameSet.contains(userId) && !pendingNameSet.contains(userId)){
            holder.check.setVisibility(View.GONE);
            holder.add.setVisibility(View.VISIBLE);
            holder.add.setText("Add");
        }

        holder.check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "You are already friends with "+userName, Toast.LENGTH_SHORT).show();
            }
        });

        holder.add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, Object> map = new HashMap<>();

                map.put(FirebaseAuth.getInstance().getUid(), FirebaseAuth.getInstance().getUid());

                friendReqRef.child("friendRequest").updateChildren(map);

                Toast.makeText(context, "Friend request sent to "+userName, Toast.LENGTH_SHORT).show();

                HashMap<String, Object> map1 = new HashMap<>();

                map1.put(userId, userId);
                databaseReference.child("pendingRequest").updateChildren(map1);

            }
        });


    }

    @Override
    public int getItemCount() {
        if(arrayList != null)
            return arrayList.size();
        else
            return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        CircleImageView dp, check;
        TextView name, add;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            check = itemView.findViewById(R.id.check);
            name = itemView.findViewById(R.id.name);
            dp = itemView.findViewById(R.id.profile_image);
            add = itemView.findViewById(R.id.add);

        }

        @Override
        public void onClick(View v) {

        }
    }
}
