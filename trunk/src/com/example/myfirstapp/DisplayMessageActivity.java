package com.example.myfirstapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class DisplayMessageActivity extends Activity {
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		//Get the message from the Intent
		Intent intent = getIntent();
		String message = intent.getStringExtra(MyFirstAppActivity.EXTRA_MESSAGE);
		
		//Create the textview
		TextView textView = new TextView(this);
		textView.setTextSize(40);
		textView.setText(message);
		
		setContentView(textView);
	}
}
