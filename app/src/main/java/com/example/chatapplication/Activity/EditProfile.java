package com.example.chatapplication.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.chatapplication.R;
import com.example.chatapplication.model.userModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfile extends AppCompatActivity {
    CircleImageView profileImage, editIcon;
    ImageView backbutton, dobPicker;
    EditText userName, status, dob;
    TextView saveButton;
    ProgressDialog pd;
    String dp;
    FirebaseAuth auth;
    FirebaseUser firebaseCurrentUser;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        profileImage = findViewById(R.id.userDP);
        editIcon = findViewById(R.id.editIcon);
        userName = findViewById(R.id.editName);
        status = findViewById(R.id.editStatus);
        saveButton = findViewById(R.id.saveButton);
        pd = new ProgressDialog(this);
        pd.setCanceledOnTouchOutside(false);
        backbutton = findViewById(R.id.backbutton);
        dob = findViewById(R.id.dob);
        dobPicker = findViewById(R.id.dobPicker);

        auth = FirebaseAuth.getInstance();
        firebaseCurrentUser = auth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("user");

        dobPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();

                // on below line we are getting
                // our day, month and year.
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);

                // on below line we are creating a variable for date picker dialog.
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        // on below line we are passing context.
                        EditProfile.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                // on below line we are setting date to our text view.
                                String birthday = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
                                dob.setText(birthday);
                            }
                        },
                        // on below line we are passing year,
                        // month and day for selected date in our date picker.
                        year, month, day);
                // at last we are calling show to
                // display our date picker dialog.
                datePickerDialog.show();
            }
        });

        databaseReference.child(auth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userModel userModel = snapshot.getValue(userModel.class);
                dp = userModel.getImageID();
                Glide.with(EditProfile.this).load(dp).into(profileImage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditProfile.this, ZoomedImage.class);
                intent.putExtra("ReceiverImage", dp);
                startActivity(intent);
            }
        });

        Query query = databaseReference.orderByChild("email").equalTo(firebaseCurrentUser.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {

                    String image = dataSnapshot1.child("imageID").getValue(String.class);
                    String name = dataSnapshot1.child("name").getValue(String.class);
                    String status1 = dataSnapshot1.child("status").getValue(String.class);
                    String birthday = dataSnapshot1.child("birthday").getValue(String.class);

                    try {
                        if(image != null)
                            Glide.with(EditProfile.this).load(image).into(profileImage);
                        userName.setText(name);
                        status.setText(status1);
                        dob.setText(birthday);
                    } catch (Exception e) {
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        backbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {finish();}});

        editIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 10);
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, Object> map = new HashMap<>();

                map.put("name", userName.getText().toString());
                map.put("status", status.getText().toString());
                map.put("birthday", dob.getText().toString());

                databaseReference.child(auth.getUid()).updateChildren(map);

                Toast.makeText(EditProfile.this, "Profile Updated", Toast.LENGTH_SHORT).show();
            }
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 10) {
            if(data != null) {
                pd.show();
                pd.setMessage("Changing Profile Picture...");
                Uri imageUri = data.getData();
                Glide.with(EditProfile.this).load(imageUri).into(profileImage);

                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("upload").child(auth.getUid());

                if(imageUri != null){
                    storageReference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    databaseReference.child(auth.getUid()).child("imageID").setValue(uri.toString());
                                    pd.dismiss();
                                    Toast.makeText(EditProfile.this, "Profile Picture Updated", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.logout){
            auth.signOut();
            finish();
            startActivity(new Intent(EditProfile.this, MainActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }
}