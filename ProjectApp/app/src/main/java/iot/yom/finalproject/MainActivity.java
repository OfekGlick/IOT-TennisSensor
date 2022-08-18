package iot.yom.finalproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

public class MainActivity extends AppCompatActivity {

    private Button btn_toPart1;
    private Button btn_toPart2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if ( !Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }

        // Permission to write to storage
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }

        // Set views
        btn_toPart1 = findViewById(R.id.btn_toPart1);
        btn_toPart2 = findViewById(R.id.btn_toPart2);

        //set listeners
        btn_toPart1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent tnt_toPart1 = new Intent(view.getContext(), MainPart1.class);
                startActivity(tnt_toPart1);
            }
        });

        btn_toPart2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent tnt_toPart2 = new Intent(view.getContext(), MainPart2.class);
                startActivity(tnt_toPart2);
            }
        });


    }
}