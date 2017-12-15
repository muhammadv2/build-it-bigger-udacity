package com.opensource.worldwide.showjokes;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class DisplayJokes extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_jokes);

        TextView jokeView = findViewById(R.id.joke_tv);

        Intent comingIntent = getIntent();
        String joke = comingIntent.getStringExtra(getString(R.string.joke_key));

        jokeView.setText(joke);

    }
}
