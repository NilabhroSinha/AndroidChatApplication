package com.example.chatapplication.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.example.chatapplication.Adapter.FriendRequestAdapter;
import com.example.chatapplication.Adapter.PendingRequestAdapter;
import com.example.chatapplication.R;
import com.example.chatapplication.model.userModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;

public class PendingRequests extends AppCompatActivity {
    ImageView back;
    RecyclerView recyclerView;
    FirebaseAuth auth;
    PendingRequestAdapter recyclerViewAdapter;
    HashSet<userModel> arraySet = new HashSet<>();
    ArrayList<userModel> arrayListDummy = new ArrayList<>();
    ArrayList<userModel> arrayList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_requests);

        back = findViewById(R.id.backbutton);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("user");
        auth = FirebaseAuth.getInstance();

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                arrayListDummy.clear();

                userModel um;

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    um = dataSnapshot.getValue(userModel.class);


                    if (!FirebaseAuth.getInstance().getUid().equals(um.getUserID())) {
                        arrayListDummy.add(um);
                    }
                }

                databaseReference.child(auth.getUid()).child("pendingRequest").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        arraySet.clear();
                        arrayList.clear();
                        if (snapshot.exists()) {
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                for (userModel um : arrayListDummy) {
                                    if (um.getUserID().equals(ds.getValue(String.class))) {
                                        arraySet.add(um);
                                        break;
                                    }
                                }
                            }

                            arrayList.addAll(arraySet);
                            arrayList.sort((a, b) -> b.getName().compareTo(a.getName()));
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

        recyclerViewAdapter = new PendingRequestAdapter(PendingRequests.this, arrayList);
        recyclerView.setAdapter(recyclerViewAdapter);
    }
}