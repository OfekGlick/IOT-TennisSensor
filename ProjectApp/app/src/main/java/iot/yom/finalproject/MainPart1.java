package iot.yom.finalproject;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.opencsv.CSVReader;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

public class MainPart1 extends AppCompatActivity {

    // consts
    private final String PATH = "/storage/self/primary/Terminal/";

    // android views
    private LineChart lc_load_file;
    private TextView tv_steps_amount;
    private TextView tv_walk_run;
    private EditText et_file_name;
    private Button btn_load;
    private Thread classifiers;

    // chart stuff
    LineDataSet norm;
    ArrayList<ILineDataSet> dataSets = new ArrayList<>();
    LineData data;

    // python
    private PyObject python;
    private final String MODULE_NAME = "main_app";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_part1);

        // set views
        tv_steps_amount = findViewById(R.id.tv_StepsAmount);
        tv_walk_run = findViewById(R.id.tv_WalkRun);
        et_file_name = findViewById(R.id.et_FileName);
        btn_load = findViewById(R.id.btn_Load);
        lc_load_file = findViewById(R.id.lc_LoadFile);

        // python

        Thread classifiers = new Thread(new Runnable() {
            @Override
            public void run() {
                Python py = Python.getInstance();
                python = py.getModule(MODULE_NAME);
            }
        });
        classifiers.start();

        // chart stuff
        norm = new LineDataSet(new ArrayList<Entry>(), "Norm");
        dataSets.add(norm);
        data = new LineData(dataSets);
        lc_load_file.setData(data);
        lc_load_file.invalidate();

        // load btn
        btn_load.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(classifiers.getState()!=Thread.State.TERMINATED){
                    Toast.makeText(view.getContext(), "Classifiers not ready please wait",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                String file_name = et_file_name.getText().toString();
                String full_path = PATH + file_name + ".csv";
                File file = new File(full_path);
                if(!file.exists()) {
                    Toast.makeText(view.getContext(), "File not found!",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                // python stuff
                PyObject result = python.callAttr("main", full_path);
                String[] python_obj = result.toString().split("~");
                tv_walk_run.setText(python_obj[0]);
                tv_steps_amount.setText(python_obj[1]);

                // chart
                ArrayList<String[]> csv_data = new ArrayList<>();
                csv_data = CsvRead(full_path);
                LineDataSet lineDataSet_norm = new LineDataSet(DataValues(csv_data, 6), "Norm");
                lineDataSet_norm.setColor(R.color.purple_500);
                ArrayList<ILineDataSet> dataSets = new ArrayList<>();
                dataSets.add(lineDataSet_norm);
                LineData data = new LineData(dataSets);
                lc_load_file.setData(data);
                lc_load_file.invalidate();
            }
        });
    }

    private ArrayList<String[]> CsvRead(String path) {
        ArrayList<String[]> csv_data = new ArrayList<>();
        try {
            File file = new File(path);
            CSVReader reader = new CSVReader(new FileReader(file));
            String[] nextline;
            while ((nextline = reader.readNext()) != null)
                csv_data.add(nextline);
        } catch (Exception e) {
            System.out.println(e);
        }
        return csv_data;
    }

    private ArrayList<Entry> DataValues(ArrayList<String[]> csvData, int initial) {
        ArrayList<Entry> values = new ArrayList<Entry>();
        float initial_time=-1;
        for (int i = initial; i < csvData.size(); i++) {
            String[] row = csvData.get(i);
            double norm = 0;
            if (i == initial){
                initial_time = Float.parseFloat(row[0]);
            }
            for (int col = 1; col < 4; col++)
                norm += Math.pow(Float.parseFloat(row[col]), 2);
            norm = Math.pow(norm, 0.5);
            values.add(new Entry(Float.parseFloat(row[0])- initial_time, (float) norm));
        }
        return values;
    }

}