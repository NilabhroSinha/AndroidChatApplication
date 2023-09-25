package com.example.chatapplication.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.autofill.AutofillValue;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.chatapplication.R;
import com.example.chatapplication.model.userModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class profileActivity extends AppCompatActivity {
    EditText name, email, password, status, dob;
    Button cont;
    FirebaseAuth auth;
    Uri imageURI = Uri.parse("android.resource://com.example.chatapplication/drawable/default_user");
    ImageView profileImage, dobPicker;
    ProgressDialog pd;
    FirebaseDatabase database;
    FirebaseStorage storage;
    ConstraintLayout googlebtn, fbbtn;
    List<String> friendsList;
    public static String birthday = "", image = "https://firebasestorage.googleapis.com/v0/b/chatapp-ba1ed.appspot.com/o/user%20(1).png?alt=media&token=75030572-2e24-4b8b-bed9-8c607f5ca372";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        pd = new ProgressDialog(this);
        pd.setCanceledOnTouchOutside(false);

        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        password = findViewById(R.id.pass);
        status = findViewById(R.id.status);
        cont = findViewById(R.id.cont);
        profileImage = findViewById(R.id.profile_image);
        dobPicker = findViewById(R.id.dobPicker);
        dob = findViewById(R.id.dob);

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
                        profileActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                // on below line we are setting date to our text view.
                                birthday = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
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

        auth = FirebaseAuth.getInstance();

        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        googlebtn = findViewById(R.id.googleFab);
        fbbtn = findViewById(R.id.Fbfab);
        friendsList = new ArrayList<>();


        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 10);


            }
        });


        cont.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pd.show();
                pd.setMessage("Creating Account...");
                if(TextUtils.isEmpty(email.getText().toString()) || TextUtils.isEmpty(password.getText().toString()) )
                    Toast.makeText(profileActivity.this, "Enter you email and password", Toast.LENGTH_LONG).show();
                else{
                    auth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                DatabaseReference databaseReference = database.getReference().child("user").child(auth.getUid());
                                StorageReference storageReference = storage.getReference().child("upload").child(auth.getUid());

                                if(imageURI != null){
                                    storageReference.putFile(imageURI).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                            if(task.isSuccessful()){
                                                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                    @Override
                                                    public void onSuccess(Uri uri) {
                                                        image = uri.toString();

                                                        userModel um = new userModel(auth.getUid(),name.getText().toString(), email.getText().toString(), password.getText().toString(), image, status.getText().toString(), friendsList, 0L, "", dob.getText().toString());
                                                        databaseReference.setValue(um).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful()){
                                                                    startActivity(new Intent(profileActivity.this, MessagesActivity.class));
                                                                    pd.dismiss();
                                                                    finish();
                                                                }
                                                            }
                                                        });
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                     @Override
                                                    public void onFailure(@NonNull Exception e) {

                                                    }
                                                });
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    });
                }

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 10){
            if(data != null){
                imageURI = data.getData();
                profileImage.setImageURI(imageURI);
            }

        }
    }

}