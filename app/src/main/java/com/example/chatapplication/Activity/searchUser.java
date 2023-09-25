package com.example.chatapplication.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Switch;
import android.widget.Toast;

import com.example.chatapplication.Adapter.RecyclerViewAdapter;
import com.example.chatapplication.Adapter.SearchAdapter;
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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;

import de.hdodenhof.circleimageview.CircleImageView;

public class searchUser extends AppCompatActivity {
    SearchView searchView;
    SwitchMaterial showAll;
    ImageView emptyListID, backbutton;
    RecyclerView recyclerView;
    SearchAdapter searchViewAdapter;
    ArrayList<userModel> arrayList;
    FirebaseDatabase database;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_user);

        showAll = findViewById(R.id.switch1);
        emptyListID = findViewById(R.id.emptyListID);
        searchView = findViewById(R.id.search_bar);
        backbutton = findViewById(R.id.backbutton);
        arrayList = new ArrayList<>();
        HashSet<String> nameSet = new HashSet<>();

//        searchView.setIconified(false);
        backbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        database = FirebaseDatabase.getInstance();

        DatabaseReference databaseReference =  database.getReference().child("user");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                arrayList.clear();
                userModel um;
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    um = dataSnapshot.getValue(userModel.class);

                    if(!FirebaseAuth.getInstance().getUid().equals(um.getUserID())){
                        arrayList.add(um);
                    }
                }

                arrayList.sort(Comparator.comparing(userModel::getName));

                filter(searchView.getQuery().toString());
                emptyListID.setVisibility(View.GONE);

                searchViewAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DatabaseReference friendListRef =  FirebaseDatabase.getInstance().getReference().child("user").child(FirebaseAuth.getInstance().getUid()).child("friendsList");

        HashSet<String> finalNameSet = nameSet;

        friendListRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot ds: snapshot.getChildren()) {

                        String str = ds.getValue(String.class);
                        finalNameSet.add(str);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DatabaseReference pendingListRef =  FirebaseDatabase.getInstance().getReference().child("user").child(FirebaseAuth.getInstance().getUid()).child("pendingRequest");

        HashSet<String> pendingNameSet = new HashSet<>();

        pendingListRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot ds: snapshot.getChildren()) {

                        String str = ds.getValue(String.class);
                        pendingNameSet.add(str);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emptyListID.setVisibility(View.GONE);
                searchView.setIconified(false);
            }
        });

        showAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    filter(searchView.getQuery().toString());
                    searchViewAdapter.notifyDataSetChanged();
                    emptyListID.setVisibility(View.GONE);
                }
                else {
                    if(searchView.getQuery().toString().length()==0)
                        emptyListID.setVisibility(View.VISIBLE);
                    else{
                        emptyListID.setVisibility(View.GONE);
                    }

                    filter(searchView.getQuery().toString());
                    searchViewAdapter.notifyDataSetChanged();

                }
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                searchViewAdapter.notifyDataSetChanged();
                return false;
            }
        });

        recyclerView = findViewById(R.id.recyclerV);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        searchViewAdapter = new SearchAdapter(searchUser.this, finalNameSet, pendingNameSet);
        recyclerView.setAdapter(searchViewAdapter);



    }
    private void filter(String text) {
        // creating a new array list to filter our data.

        ArrayList<userModel> filteredList = new ArrayList<>();

        if(text.length() == 0 && !showAll.isChecked()) {
            searchViewAdapter.filterList(filteredList);
            return;
        }

        // running a for loop to compare elements.
            // checking if the entered string matched with any item of our recycler view.
        for (userModel row : arrayList) {
            String target = row.getName().toLowerCase().trim();
            String str = text.toLowerCase().trim();

            if(str.equals(target.substring(0, str.length())))
                filteredList.add(row);

        }

        if (filteredList.isEmpty()) {
            // if no item is added in filtered list we are
            // displaying a toast message as no data found.
            Toast.makeText(this, "No Data Found..", Toast.LENGTH_SHORT).show();
        }

        searchViewAdapter.filterList(filteredList);

    }

}