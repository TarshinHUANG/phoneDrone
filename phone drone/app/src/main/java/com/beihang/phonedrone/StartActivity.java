package com.beihang.phonedrone;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class StartActivity extends AppCompatActivity {

    private TextView titleTv;
    private ImageButton returnBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        initUI();
    }

    private void initUI() {
        titleTv = (TextView) findViewById(R.id.titleTv);
        titleTv.setText(R.string.start_activity);
        returnBtn = (ImageButton) findViewById(R.id.returnBtn);
        returnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StartActivity.this.finish();
            }
        });
    }
}
