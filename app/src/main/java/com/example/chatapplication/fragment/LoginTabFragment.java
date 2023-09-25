package com.example.chatapplication.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.example.chatapplication.Activity.MainActivity;
import com.example.chatapplication.Activity.MessagesActivity;
import com.example.chatapplication.Activity.profileActivity;
import com.example.chatapplication.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginTabFragment extends Fragment {
    EditText email, pass;
    TextView signup;
    Button button;
    FirebaseAuth auth;
    ProgressDialog pd;
    String validEmail = "[a-zA-Z0-9._-]+@[a-z]+\\\\.+[a-z]+";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login_layout, null);

        email = view.findViewById(R.id.emailET);
        pass = view.findViewById(R.id.passET);
        button = view.findViewById(R.id.login);

        auth = FirebaseAuth.getInstance();

        pd = new ProgressDialog(getContext());
        pd.setCanceledOnTouchOutside(false);

        signup = view.findViewById(R.id.signuphere);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Intent i = new Intent(getContext(), MessagesActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        }

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), profileActivity.class));
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(TextUtils.isEmpty(email.getText().toString()) || TextUtils.isEmpty(pass.getText().toString()) )
                    Toast.makeText(getContext(), "Enter you email and password", Toast.LENGTH_LONG).show();
//                else if(!(email.getText().toString().matches(validEmail)))
//                    Toast.makeText(getContext(), "Email address not valid", Toast.LENGTH_SHORT).show();
                else{
                    pd.show();
                    pd.setMessage("Signing in...");
                    auth.signInWithEmailAndPassword(email.getText().toString(), pass.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()) {
                                startActivity(new Intent(getContext(), MessagesActivity.class));
                                pd.dismiss();
                                getActivity().finish();
                            }
                            else
                                Toast.makeText(getContext(), "Incorrect Email or Password", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            }
        });

        return view;
    }
}
