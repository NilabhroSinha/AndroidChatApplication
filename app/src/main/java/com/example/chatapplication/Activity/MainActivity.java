package com.example.chatapplication.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;

import com.example.chatapplication.Adapter.PagerAdapter;
import com.example.chatapplication.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    FloatingActionButton googleFab;
    FloatingActionButton facebookFab;
    PagerAdapter pagerAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

//        googleFab = findViewById(R.id.googleFab);
//        facebookFab = findViewById(R.id.Fbfab);

        ViewPager viewPager = findViewById(R.id.viewPager);

        pagerAdapter = new PagerAdapter(getSupportFragmentManager(), 1);

        viewPager.setAdapter(pagerAdapter);

    }
}