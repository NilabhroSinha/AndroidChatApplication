package com.example.chatapplication.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.chatapplication.Activity.profileActivity;
import com.example.chatapplication.R;
import com.example.chatapplication.model.userModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

public class SignUpTabFragment extends Fragment {
    EditText name, email, password;
    Button signUp;
    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseStorage storage;
    List<String> friendsList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.sign_up_layout, null);

        name = view.findViewById(R.id.name);
        email = view.findViewById(R.id.email);
        password = view.findViewById(R.id.pass);
        signUp = view.findViewById(R.id.signup);
        String validEmail = "[a-zA-Z0-9._-]+@[a-z]+\\\\.+[a-z]+";
        friendsList = new ArrayList<>();

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance("https://chatapp-ba1ed-default-rtdb.firebaseio.com/");
        storage = FirebaseStorage.getInstance();

//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//        if (user != null) {
//            Intent i = new Intent(getContext(), MessagesActivity.class);
//            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//            startActivity(i);
//        }

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(TextUtils.isEmpty(email.getText().toString()) || TextUtils.isEmpty(password.getText().toString()) )
                    Toast.makeText(getContext(), "Enter you email and password", Toast.LENGTH_LONG).show();
//                else if(!(email.getText().toString().matches(validEmail)))
//                    Toast.makeText(getContext(), "Email address not valid", Toast.LENGTH_SHORT).show();
                else {
                    auth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                DatabaseReference databaseReference = database.getReference().child("user").child(auth.getUid());
                                //StorageReference storageReference = storage.getReference().child("upload").child(auth.getUid());

                                userModel userModel = new userModel(auth.getUid(), email.getText().toString(), password.getText().toString(), friendsList);
                                databaseReference.setValue(userModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            startActivity(new Intent(getContext(), profileActivity.class));
                                        }

                                    }
                                });
                            }
                        }
                    });
                }
            }
        });


        return view;
    }
}
