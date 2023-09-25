package com.example.chatapplication.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.chatapplication.Adapter.RecyclerViewAdapter;
import com.example.chatapplication.model.AESUtils;
import com.example.chatapplication.model.MessageModel;
import com.example.chatapplication.Adapter.MessagesAdapter;
import com.example.chatapplication.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {
    String receiverImage, receiverName, receiverStatus, receiverUid, senderUid, senderRoom, receiverRoom, email;
    EditText editText;
    TextView name, onlineText;
    CircleImageView dp, onlineImage;
    ImageView backbutton, sendButton, gallery;
    View profileButton;
    ProgressDialog pd;
    FirebaseDatabase database;
    FirebaseAuth auth;
    RecyclerView recyclerView;
    public static String sImage, rImage;
    ArrayList<MessageModel> arrayList = new ArrayList<>();
    MessagesAdapter messagesAdapter;
    RecyclerViewAdapter recyclerViewAdapter;

    String cipherText;

    public ChatActivity() throws NoSuchAlgorithmException {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        database = FirebaseDatabase.getInstance("https://chatapp-ba1ed-default-rtdb.firebaseio.com/");
        auth = FirebaseAuth.getInstance();

        receiverName = getIntent().getStringExtra("name");
        receiverImage = getIntent().getStringExtra("ReceiverImage");
        receiverUid = getIntent().getStringExtra("uid");
        receiverStatus = getIntent().getStringExtra("status");
        email = getIntent().getStringExtra("email");


        senderUid = auth.getUid();

        editText = findViewById(R.id.sendtext);
        sendButton = findViewById(R.id.sendbutton);
        backbutton = findViewById(R.id.backbutton);
        profileButton = findViewById(R.id.profile_tab);

        pd = new ProgressDialog(this);
        pd.setCanceledOnTouchOutside(false);

        dp = findViewById(R.id.dp);
        name = findViewById(R.id.name);
        onlineImage = findViewById(R.id.onlineImage);
        onlineText = findViewById(R.id.onlineText);
        gallery = findViewById(R.id.gallery);
        recyclerView = findViewById(R.id.recyclerV);

        senderRoom = senderUid+receiverUid;
        receiverRoom = receiverUid+senderUid;

        messagesAdapter = new MessagesAdapter(ChatActivity.this, arrayList, senderRoom, receiverRoom, recyclerView);
        recyclerViewAdapter = new RecyclerViewAdapter();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(messagesAdapter);

        name.setText(receiverName);
        Glide.with(this).load(receiverImage).into(dp);

        DatabaseReference reference = database.getReference().child("user");
        DatabaseReference chatReference = database.getReference().child("chats").child(senderRoom).child("message");

        backbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 10);
            }
        });

        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatActivity.this, viewProfile.class);
                intent.putExtra("name", getIntent().getStringExtra("name"));
                intent.putExtra("ReceiverImage", getIntent().getStringExtra("ReceiverImage"));
                intent.putExtra("status", getIntent().getStringExtra("status"));
                intent.putExtra("ReceiverId", receiverUid);
                intent.putExtra("email", email);
                intent.putExtra("birthday", getIntent().getStringExtra("birthday"));
                startActivity(intent);
            }
        });


        chatReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                arrayList.clear();
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){

                    MessageModel messageModel = dataSnapshot.getValue(MessageModel.class);
                    messageModel.setMessageID(dataSnapshot.getKey());


                    String msg = "";
                    String decrypted = "";
                    MessageModel msgModel = null;

                    if(messageModel.getMessage() != null){
                        msg = dataSnapshot.child("message").getValue().toString();
                        try {
                            decrypted = AESUtils.decrypt(msg);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        msgModel = new MessageModel(decrypted, dataSnapshot.child("senderID").getValue().toString(), Long.parseLong(dataSnapshot.child("timeStamp").getValue().toString()),  Integer.parseInt(dataSnapshot.child("reaction").getValue().toString()));
                        msgModel.setMessageID(dataSnapshot.getKey());
                        arrayList.add( msgModel );
                    }
                    else if(messageModel.getChatImageId() != null){
                        String image = dataSnapshot.child("chatImageId").getValue().toString();

                        msgModel = new MessageModel(Long.parseLong(dataSnapshot.child("timeStamp").getValue().toString()), dataSnapshot.child("senderID").getValue().toString(), image, Integer.parseInt(dataSnapshot.child("reaction").getValue().toString()));
                        msgModel.setMessageID(dataSnapshot.getKey());
                        arrayList.add( msgModel );
                    }


                }
                messagesAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("imgcheck", "failed to load data");
            }
        });

        chatReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                recyclerView.scrollToPosition(messagesAdapter.getItemCount());
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                sImage = snapshot.child(auth.getUid()).child("imageID").getValue(String.class);
                rImage = receiverImage;

                String onlineOrNot = snapshot.child(receiverUid).child("onlineStatus").getValue(String.class);

                if (onlineOrNot != null && onlineOrNot.equals("Online")) {
                    onlineText.setText(onlineOrNot);
                    onlineImage.setImageResource(R.drawable.online);
                    onlineImage.setVisibility(View.VISIBLE);
                } else {
                    onlineText.setText("Offline");
                    onlineImage.setImageResource(R.drawable.offline);
                    onlineImage.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                gallery.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(editText.getText().toString().isEmpty())
                    gallery.setVisibility(View.VISIBLE);
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = editText.getText().toString();
                if( !message.isEmpty() ){

                    Date date = new Date();

                    try {
                        cipherText = AESUtils.encrypt(message);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Long time = date.getTime();
                    MessageModel messageModel = new MessageModel(cipherText, senderUid, time, -1);

                    updateTime(senderUid, time);
                    updateTime(receiverUid, time);

                    String randomKey = database.getReference().push().getKey();

                    database.getReference().child("chats").child(senderRoom).child("message").child(randomKey).setValue(messageModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            database.getReference().child("chats").child(receiverRoom).child("message").child(randomKey).setValue(messageModel);
                        }
                    });
                    recyclerViewAdapter.notifyDataSetChanged();
                    sendButton.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
                    recyclerView.scrollToPosition(messagesAdapter.getItemCount()-1);
                    editText.setText("");

                }

            }

        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 10 && data != null){
            pd.show();
            pd.setMessage("Sending picture...");
            Uri imageUri = data.getData();

            if(imageUri == null)
                return;

            String randomKey = database.getReference().push().getKey();
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("chatImages").child(randomKey);


            storageReference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            DatabaseReference recieverReference = database.getReference().child("chats").child(receiverRoom);
                            DatabaseReference senderReference = database.getReference().child("chats").child(senderRoom);
                            Long time = new Date().getTime();

                            updateTime(senderUid, time);
                            updateTime(receiverUid, time);

                            MessageModel msgModel = new MessageModel(time, senderUid, uri.toString(), -1);

                            String rndKey = database.getReference().push().getKey();

                            recieverReference.child("message").child(rndKey).setValue(msgModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    senderReference.child("message").child(rndKey).setValue(msgModel);
                                    pd.dismiss();
                                }
                            });
                            recyclerViewAdapter.notifyDataSetChanged();
                        }
                    });
                }
            });
        }
    }

    void updateTime(String userId, Long time){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("user").child(userId);

        databaseReference.child("timeStamp").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                HashMap<String, Object> map = new HashMap<>();

                map.put("timeStamp", time);

                databaseReference.updateChildren(map);
                messagesAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
