package com.example.chatapplication.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.chatapplication.Adapter.PopupActivityAdapter;
import com.example.chatapplication.Adapter.RecyclerViewAdapter;
import com.example.chatapplication.R;
import com.example.chatapplication.model.PopupModel;
import com.example.chatapplication.model.userModel;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.wasabeef.glide.transformations.BlurTransformation;

public class viewProfile extends AppCompatActivity {
    String receiverImage, receiverName, receiverStatus, id, rEmail, birthday;
    LinearLayout unfriend;
    TextView name, status, message, email, friendsNO, mutualFriendsNO, dob;
    CircleImageView dp, onlineImage;
    ImageView back, backgroundDP;
    CardView mutualFriend;
    AlertDialog.Builder builder;
    Set<String> setOfMutualFrineds = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);

        unfriend = findViewById(R.id.unfriend);
        name = findViewById(R.id.userName);
        dp = findViewById(R.id.userDP);
        status = findViewById(R.id.status);
        onlineImage = findViewById(R.id.onlineImage);
        backgroundDP = findViewById(R.id.backgroundDP);
        message = findViewById(R.id.message);
        mutualFriend = findViewById(R.id.mutualFriends);
        email = findViewById(R.id.email);
        mutualFriendsNO = findViewById(R.id.mutualfriendsNO);
        friendsNO = findViewById(R.id.friendsNO);
        dob = findViewById(R.id.dob);

        back = findViewById(R.id.backbutton);

        builder = new AlertDialog.Builder(viewProfile.this);

        receiverName = getIntent().getStringExtra("name");
        receiverImage = getIntent().getStringExtra("ReceiverImage");
        receiverStatus = getIntent().getStringExtra("status");
        id = getIntent().getStringExtra("ReceiverId");
        rEmail = getIntent().getStringExtra("email");
        birthday = getIntent().getStringExtra("birthday");

        getMutualFriends(id, mutualFriendsNO);

        email.setText(rEmail);
        dob.setText(birthday);

        unfriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unfriend.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
                removeFriend(receiverName, id);
            }
        });

        FirebaseDatabase.getInstance().getReference().child("user").child(id).child("friendsList").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalFriends = 0;
                for(DataSnapshot snapshot1: snapshot.getChildren()){
                    totalFriends++;
                }

                friendsNO.setText(String.valueOf(totalFriends));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        friendsNO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                friendsNO.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
                createMutualFriendsDialogue(false, id);
            }
        });

        mutualFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mutualFriend.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
                createMutualFriendsDialogue(true, id);
            }
        });

        message.setOnClickListener(v -> finish());

        Glide.with(this).load(receiverImage).into(dp);
        Glide.with(this).load(receiverImage).apply(RequestOptions.bitmapTransform(new BlurTransformation(5, 3))).into(backgroundDP);
        name.setText(receiverName);
        status.setText(receiverStatus);

        backgroundDP.setColorFilter(ContextCompat.getColor(viewProfile.this, R.color.my_purple), android.graphics.PorterDuff.Mode.MULTIPLY);

        FirebaseDatabase.getInstance().getReference().child("user").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String onlineOrNot = snapshot.child(id).child("onlineStatus").getValue(String.class);

                if (onlineOrNot != null && onlineOrNot.equals("Online")) {
                    onlineImage.setImageResource(R.drawable.online);
                } else {
                    onlineImage.setImageResource(R.drawable.offline);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        dp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(viewProfile.this, ZoomedImage.class);
                intent.putExtra("ReceiverImage", receiverImage);
                startActivity(intent);
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void getMutualFriends(String id, TextView mutualFriendsNO) {

        DatabaseReference friendListRef =  FirebaseDatabase.getInstance().getReference().child("user").child(FirebaseAuth.getInstance().getUid()).child("friendsList");
        DatabaseReference otherRef =  FirebaseDatabase.getInstance().getReference().child("user").child(id).child("friendsList");


        Set<String> set = new HashSet<>();

        friendListRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                        set.add(dataSnapshot.getValue(String.class));
                    }

                    otherRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                for(DataSnapshot snapshot1: snapshot.getChildren()){
                                    String str = snapshot1.getValue(String.class);

                                    if(set.contains(str)){
                                        setOfMutualFrineds.add(str);
                                    }
                                }
                                mutualFriendsNO.setText(String.valueOf(setOfMutualFrineds.size()));
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void removeFriend(String name, String userid) {
        builder.setMessage("Do you want to unfriend "+name+"?")
                .setCancelable(true)
                .setPositiveButton("Yes", (dialog, id) -> {
                    DatabaseReference friendListRef =  FirebaseDatabase.getInstance().getReference().child("user").child(FirebaseAuth.getInstance().getUid()).child("friendsList");
                    DatabaseReference otherRef =  FirebaseDatabase.getInstance().getReference().child("user").child(userid).child("friendsList");

                    friendListRef.child(userid).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            otherRef.child(FirebaseAuth.getInstance().getUid()).removeValue();
                            Toast.makeText(viewProfile.this, "You just unfriended "+name+" :(", Toast.LENGTH_SHORT).show();
                        }
                    });
                    startActivity(new Intent(viewProfile.this, MessagesActivity.class));
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert = builder.create();
        alert.setTitle("Unfriend " + name);
        alert.show();
    }

    private void createMutualFriendsDialogue(boolean mutualFriends, String id){
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.pop_up_activity);



        ArrayList<PopupModel> arrayList = new ArrayList<>();

        if(mutualFriends){
            FirebaseDatabase.getInstance().getReference().child("user").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for(DataSnapshot snapshot1: snapshot.getChildren()){
                        if(setOfMutualFrineds.contains(snapshot1.child("userID").getValue(String.class))){
                            arrayList.add(new PopupModel(snapshot1.child("name").getValue(String.class), snapshot1.child("imageID").getValue(String.class)));
                        }
                    }

                    RecyclerView recyclerView;
                    PopupActivityAdapter popupActivityAdapter;

                    recyclerView = dialog.findViewById(R.id.recyclerpop);
                    popupActivityAdapter = new PopupActivityAdapter(viewProfile.this, arrayList);

                    recyclerView.setLayoutManager(new LinearLayoutManager(viewProfile.this));
                    recyclerView.setAdapter(popupActivityAdapter);


                    dialog.show();
                    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    dialog.getWindow().setGravity(Gravity.BOTTOM);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        else{
            TextView friendID = dialog.findViewById(R.id.friendID);
            friendID.setText("Friends");
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("user");
            FirebaseDatabase.getInstance().getReference().child("user").child(id).child("friendsList").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        for(DataSnapshot snapshot1: snapshot.getChildren()){
                            PopupModel popupModel = new PopupModel();

                            ref.child(snapshot1.getValue(String.class)).child("name").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    popupModel.setName(snapshot.getValue(String.class));
                                    ref.child(snapshot1.getValue(String.class)).child("imageID").addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            popupModel.setImage(snapshot.getValue(String.class));
                                            arrayList.add(popupModel);

                                            RecyclerView recyclerView;
                                            PopupActivityAdapter popupActivityAdapter;

                                            recyclerView = dialog.findViewById(R.id.recyclerpop);
                                            popupActivityAdapter = new PopupActivityAdapter(viewProfile.this, arrayList);

                                            recyclerView.setLayoutManager(new LinearLayoutManager(viewProfile.this));
                                            recyclerView.setAdapter(popupActivityAdapter);


                                            dialog.show();
                                            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                            dialog.getWindow().setGravity(Gravity.BOTTOM);
                                            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }


    }
}