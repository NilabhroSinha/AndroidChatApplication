package com.example.chatapplication.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.chatapplication.R;
import com.example.chatapplication.Adapter.RecyclerViewAdapter;
import com.example.chatapplication.fragment.LoginTabFragment;
import com.example.chatapplication.model.userModel;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesActivity extends AppCompatActivity {
    FirebaseAuth auth;
    RecyclerView recyclerView;
    ImageView emptyList;
    CircleImageView personalDP;
    RecyclerViewAdapter recyclerViewAdapter;
    FirebaseDatabase database;
    FloatingActionMenu addUser;
    FloatingActionButton addFriends, friendReq, sentReq;
    ArrayList<userModel> arrayListDummy = new ArrayList<>();
    HashSet<userModel> arraySet = new HashSet<>();
    ArrayList<userModel> arrayList = new ArrayList<>();
    DatabaseReference onlineRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        emptyList = findViewById(R.id.emptyList);
        personalDP = findViewById(R.id.personalDP);

        auth = FirebaseAuth.getInstance();

        addUser = findViewById(R.id.addUser);
        addUser.setMenuButtonColorPressed(R.color.that_purple);
        addUser.setMenuButtonColorNormalResId(R.color.that_purple);

        addFriends = findViewById(R.id.fabAddUser);
        friendReq = findViewById(R.id.fabFriendReq);
        sentReq = findViewById(R.id.fabSentReq);

        addFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addUser.toggle(true);
                startActivity(new Intent(MessagesActivity.this, searchUser.class));
            }
        });

        friendReq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addUser.toggle(true);
                startActivity(new Intent(MessagesActivity.this, FriendRequest.class));
            }
        });

        sentReq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addUser.toggle(true);
                Toast.makeText(MessagesActivity.this, "Long press to unsend Friend request", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MessagesActivity.this, PendingRequests.class));
            }
        });

        FirebaseDatabase.getInstance().getReference().child("user").child(auth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String myDP = snapshot.child("imageID").getValue(String.class);
                Glide.with(MessagesActivity.this).load(myDP).into(personalDP);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        personalDP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MessagesActivity.this, EditProfile.class));
            }
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        database = FirebaseDatabase.getInstance();

        DatabaseReference databaseReference =  database.getReference().child("user");
        DatabaseReference chatReference = database.getReference().child("chats");
        DatabaseReference connectedRef = database.getReference(".info/connected");

        onlineRef = database.getReference().child("user").child(auth.getUid()).child("onlineStatus");

        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);

                if(connected){
                    onlineRef.onDisconnect().setValue("Offline");
                    onlineRef.setValue("Online");
                } else {
                    onlineRef.setValue("Offline");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        chatReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                recyclerViewAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                arrayListDummy.clear();

                userModel um;

                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    um = dataSnapshot.getValue(userModel.class);



                    if(!FirebaseAuth.getInstance().getUid().equals(um.getUserID())){
                        arrayListDummy.add(um);
                    }
                }

                databaseReference.child(auth.getUid()).child("friendsList").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        arraySet.clear();
                        arrayList.clear();
                        if(snapshot.exists()){
                            emptyList.setVisibility(View.GONE);
                            for(DataSnapshot ds: snapshot.getChildren()){
                                for(userModel um: arrayListDummy){
                                    if(um.getUserID().equals(ds.getValue(String.class))){
                                        arraySet.add(um);
                                        break;
                                    }
                                }
                            }

                            arrayList.addAll(arraySet);
                            arrayList.sort((a, b) -> b.getTimeStamp().compareTo(a.getTimeStamp()));
                        }
                        else {
                            Toast.makeText(MessagesActivity.this, "Click on the add icon to add new Friends", Toast.LENGTH_LONG).show();
                            emptyList.setVisibility(View.VISIBLE);
                        }
                        recyclerViewAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                recyclerViewAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        recyclerView = findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        recyclerViewAdapter = new RecyclerViewAdapter(MessagesActivity.this, arrayList);
        recyclerView.setAdapter(recyclerViewAdapter);

        auth = FirebaseAuth.getInstance();

    }
}