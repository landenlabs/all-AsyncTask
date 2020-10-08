package com.landenlabs;

import android.os.Bundle;
import android.view.View;

import androidx.fragment.app.FragmentActivity;

import static androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE;


public class MainActivity extends FragmentActivity
        implements View.OnClickListener {

    View fragHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.withAllAsyncTask).setOnClickListener(this);
        findViewById(R.id.withRxJava).setOnClickListener(this);
        fragHolder = findViewById(R.id.fragHolder);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.withAllAsyncTask:
                WithAllAsyncTask withAllAsyncTask = new WithAllAsyncTask();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragHolder, withAllAsyncTask)
                        .setTransition(TRANSIT_FRAGMENT_FADE)
                        .addToBackStack(null)
                        .commit();
                break;
            case R.id.withRxJava:
                WithRxJava withRxJava = new WithRxJava();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragHolder, withRxJava)
                        .setTransition(TRANSIT_FRAGMENT_FADE)
                        .addToBackStack(null)
                        .commit();
                break;
        }
    }

}
