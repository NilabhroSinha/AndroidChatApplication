package com.example.chatapplication.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.chatapplication.R;

public class ZoomedImage extends AppCompatActivity {
    ImageView dp, back;
    String receiverImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zoomed_image);

        dp = findViewById(R.id.zoomID);
        back = findViewById(R.id.backbutton);

        setActivityBackgroundColor(000000);

        receiverImage = getIntent().getStringExtra("ReceiverImage");

        Glide.with(this).load(receiverImage).into(dp);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    public void setActivityBackgroundColor(int color) {
        Window window = this.getWindow();

        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        window.setStatusBarColor(ContextCompat.getColor(this,R.color.black));
        View view = this.getWindow().getDecorView();
        view.setBackgroundColor(color);
    }
}