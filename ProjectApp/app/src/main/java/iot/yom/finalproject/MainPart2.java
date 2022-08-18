package iot.yom.finalproject;

import android.app.Dialog;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import iot.yom.finalproject.part2.BluetoothDialog;
import iot.yom.finalproject.part2.RecordFragment;
import iot.yom.finalproject.ui.main.SectionsPagerAdapter;
import iot.yom.finalproject.databinding.ActivityMainPart2Binding;

public class MainPart2 extends AppCompatActivity  implements BluetoothDialog.OnInputListener {

    private ActivityMainPart2Binding binding;
    public String device_address;

    public void sendInput(String input)
    {
        device_address = input;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // MYO: binding is an object to connect the code with the XML views, instead of findViewById(...)
        binding = ActivityMainPart2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // set views
        ViewPager viewPager = binding.viewPager;
        TabLayout tabs = binding.tabs;
        FloatingActionButton fab = binding.fab;

        // define tabs manager
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        viewPager.setAdapter(sectionsPagerAdapter);
        tabs.setupWithViewPager(viewPager); // links tabs to the view displays it

        // fab
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BluetoothDialog dialog = new BluetoothDialog();
                dialog.show(getSupportFragmentManager(), "Dialog");

            }
        });
    }
}